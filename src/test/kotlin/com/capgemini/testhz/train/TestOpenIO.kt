package com.capgemini.testhz.train

import com.capgemini.avro.SeatsStateAvroRecord
import com.capgemini.avro.State
import com.capgemini.testhz.Shared
import okhttp3.*
import org.apache.avro.file.DataFileReader
import org.apache.avro.file.DataFileStream
import org.apache.avro.file.DataFileWriter
import org.apache.avro.file.SeekableByteArrayInput
import org.apache.avro.io.DatumReader
import org.apache.avro.io.DatumWriter
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificDatumWriter
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Paths
import java.time.Instant
import java.util.*
import kotlin.io.path.exists


@Suppress("SameParameterValue")
class TestOpenIO {

    private val client = OkHttpClient.Builder().build()
    private val token: String by lazy {
        val properties = Properties()
        if (Paths.get("auth.properties").exists()) {
            properties.load(FileReader("auth.properties"))
        }
        val current = properties.getProperty("token")
        if (current == null) {
            val request = Request.Builder()
                .get()
                .addHeader("X-Auth-User", "demo:demo")
                .addHeader("X-Auth-Key", "DEMO_PASS")
                .url(URL_AUTH)
                .build()

            val response = client.newCall(request).execute()
            val token = response.header("X-Auth-Token") ?: throw IllegalStateException("no auth token")
            properties["token"] = token
            properties.store(FileWriter("auth.properties"), "Auth token")
            token
        } else {
            current
        }
    }

    @Test
    fun testInfo() {
        val url = "http://127.0.0.1:6007/v1/endpoints"
        val request = Request.Builder()
            .get()
            .addHeader("X-Auth-Token", token)
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        println(response.body()?.string())
    }


    @Test
    fun downloadAvroFile() {
        val request = Request.Builder()
            .get()
            .addHeader("X-Auth-Token", token)
            .url("${URL}/container3/test1/test2/test1")
            .build()

        val response = client.newCall(request).execute()
        val body: ResponseBody = response.body() ?: throw IllegalStateException("expected a body")
        val allBytes = body.byteStream().readAllBytes()
        val sis = SeekableByteArrayInput(allBytes)

        val userDatumReader: DatumReader<SeatsStateAvroRecord> = SpecificDatumReader(
            SeatsStateAvroRecord::class.java)
        val dataFileReader: DataFileReader<SeatsStateAvroRecord> = DataFileReader(sis, userDatumReader)

        while (dataFileReader.hasNext()) {
            val seateStateAvroRecord = dataFileReader.next()
            System.out.println(seateStateAvroRecord)
        }
    }

    private fun loadAvroFile(
        container: String,
        file: String,
        filter: (SeatsStateAvroRecord) -> Boolean = { true },
    ): List<SeatsStateAvroRecord> {
        val request = Request.Builder()
            .get()
            .addHeader("X-Auth-Token", token)
            .url("${URL}/$container/$file")
            .build()

        val response = client.newCall(request).execute()
        val body: ResponseBody = response.body() ?: throw IllegalStateException("expected a body")

        return body.byteStream().use { stream ->
            val seatsDatumReader: DatumReader<SeatsStateAvroRecord> = SpecificDatumReader(
                SeatsStateAvroRecord::class.java)
            val dataFileStream: DataFileStream<SeatsStateAvroRecord> = DataFileStream(stream, seatsDatumReader)

            dataFileStream.filter(filter)
        }
    }

    @Test
    fun loadReservedSeatsFromExport(){
        val container = "data-export"
        val filtered = getFiles(container).flatMap { name->
            loadAvroFile(container, name){
                it.state == State.RESERVED
            }
        }
        println(filtered.size)
    }

    @Test
    fun loadAllSeatsFromExport(){
        val container = "data-export"
        val filtered = getFiles(container).flatMap { name->
            loadAvroFile(container, name)
        }
        println(filtered.size)
    }

    @Test
    fun downloadFile() {
        val request = Request.Builder()
            .get()
            .addHeader("X-Auth-Token", token)
            .url("${URL}/container3/test1/test2/test")
            .build()

        val response = client.newCall(request).execute()
        val body: ResponseBody = response.body() ?: throw IllegalStateException("expected a body")

        val filtered: List<SeatsStateAvroRecord> = body.byteStream().use { stream ->
            val seatsDatumReader: DatumReader<SeatsStateAvroRecord> = SpecificDatumReader(
                SeatsStateAvroRecord::class.java)
            val dataFileStream: DataFileStream<SeatsStateAvroRecord> = DataFileStream(stream, seatsDatumReader)

            dataFileStream.filter { it.state == State.RESERVED || it.state == State.OCCUPIED }
        }

        println(filtered)
    }

    @Test
    fun uploadFile() {
        val userDatumWriter: DatumWriter<SeatsStateAvroRecord> = SpecificDatumWriter(
            SeatsStateAvroRecord::class.java)
        val dataFileWriter: DataFileWriter<SeatsStateAvroRecord> = DataFileWriter(userDatumWriter)
        val outStream = ByteArrayOutputStream()
        dataFileWriter.create(SeatsStateAvroRecord.getClassSchema(), outStream)

        val mediaType = MediaType.parse("application/octet-stream")
        repeat(100) {
            val record = SeatsStateAvroRecord.newBuilder()
                .setRoute(123)
                .setStart(Instant.now().toEpochMilli())
                .setRailroadCar(it.toLong())
                .setPlace("my-place")
                .setState(State.AVAILABLE)
                .setStation("my-station")
                .build()
            dataFileWriter.append(record)
        }

        val requestBody: RequestBody = RequestBody.create(mediaType, outStream.toByteArray())
        dataFileWriter.close()

        val request = Request.Builder()
            .put(requestBody)
            .addHeader("X-Auth-Token", token)
            .addHeader("X-Delete-At", Instant.now().plusSeconds(30).epochSecond.toString())
            .url("${URL}/container3/test/test2")
            .build()

        val response = client.newCall(request).execute()
        println(response.toString())
    }

    @Test
    fun listContainers() {
        val request = Request.Builder()
            .get()
            .addHeader("X-Auth-Token", token)
            .url("$URL/?format=json")
            .build()

        val response = client.newCall(request).execute()
        println(response.body()?.string())
    }

    private fun getFiles(container: String): List<String> {
        val request = Request.Builder()
            .get()
            .addHeader("X-Auth-Token", token)
            .url("$URL/$container?format=json")
            .build()
        val response = client.newCall(request).execute()
        return Shared.mapper.readTree(response.body()?.byteStream()).map { it.get("name").asText() }
    }

    @Test
    fun deleteDataExportFiles() {
        getFiles("data-export").forEach {
            val request = Request.Builder()
                .delete()
                .addHeader("X-Auth-Token", token)
                .url("$URL/data-export/$it")
                .build()

            val response = client.newCall(request).execute()
            println(response.body()?.string())
        }
    }

    @Test
    fun readDataExportFiles() {
        getFiles("data-export").forEach {
            val request = Request.Builder()
                .delete()
                .addHeader("X-Auth-Token", token)
                .url("$URL/data-export/$it")
                .build()

            val response = client.newCall(request).execute()
            println(response.body()?.string())
        }
    }


    @Test
    fun listObjects() {
        val request = Request.Builder()
            .get()
            .addHeader("X-Auth-Token", token)
            .url("$URL/data-export?format=json")
            .build()

        val response = client.newCall(request).execute()
        println(Shared.mapper.readTree(response.body()?.string()).toPrettyString())
    }

    @Test
    fun deleteObject() {
        val request = Request.Builder()
            .delete()
            .addHeader("X-Auth-Token", token)
            .url("$URL/data-export")
            .build()

        val response = client.newCall(request).execute()
        println(response.body()?.string())
    }

    @Test
    fun headObject() {
        val request = Request.Builder()
            .head()
            .addHeader("X-Auth-Token", token)
            .url("$URL/container3/test/test2?format=json")
            .build()

        val response = client.newCall(request).execute()
        println(response.code())
        println(response.body()?.string())
    }


    companion object {
        const val URL = "http://127.0.0.1:6007/v1/AUTH_demo"
        const val URL_AUTH = "http://127.0.0.1:6007/auth/v1.0"
    }
}