package com.capgemini.testhz.train

import com.capgemini.avro.SeatsStateAvroRecord
import com.capgemini.avro.State
import org.apache.avro.file.DataFileStream
import org.apache.avro.file.DataFileWriter
import org.apache.avro.io.DatumReader
import org.apache.avro.io.DatumWriter
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificDatumWriter
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneOffset


class TestAvroStreams {

    @Test
    fun testAvroBytes() {
        val inputStream = createAvroInputStream()
        val seatsDatumReader: DatumReader<SeatsStateAvroRecord> = SpecificDatumReader(
            SeatsStateAvroRecord::class.java)
        val dataFileStream: DataFileStream<SeatsStateAvroRecord> = DataFileStream(inputStream, seatsDatumReader)

        val filteredList = dataFileStream.filter { it.state == State.RESERVED }
        println(filteredList.size)
    }

    private fun createAvroInputStream(): InputStream {
        val writer: DatumWriter<SeatsStateAvroRecord> = SpecificDatumWriter(
            SeatsStateAvroRecord::class.java)
        val dataFileWriter: DataFileWriter<SeatsStateAvroRecord> = DataFileWriter(writer)
        val outStream = ByteArrayOutputStream()
        dataFileWriter.create(SeatsStateAvroRecord.getClassSchema(), outStream)

        val baseStart = LocalDateTime.of(2021, 5, 10, 0, 0)
        repeat(1_000_000) {
            val record = SeatsStateAvroRecord.newBuilder()
                .setRoute((123 + it).toLong())
                .setStart(baseStart.plusHours((it % 10).toLong()).toInstant(ZoneOffset.UTC).toEpochMilli())
                .setRailroadCar(it.toLong())
                .setPlace("my-place-" + ('A' + it % 5))
                .setState(if (it % 1000 == 0) State.RESERVED else if (it % 2000 == 0) State.OCCUPIED else State.AVAILABLE)
                .setStation("my-station-" + ('A' + it % 2))
                .build()
            dataFileWriter.append(record)
        }
        dataFileWriter.flush()
        val bytes = outStream.toByteArray()
        dataFileWriter.close()
        return ByteArrayInputStream(bytes)
    }

}