package com.capgemini.testhz.train;

import okhttp3.*;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class StreamToOpenIO<T extends SpecificRecordBase> implements Closeable {
    private final String name;
    private final ByteArrayOutputStream out;
    private final DataFileWriter<T> dataFileWriter;

    public StreamToOpenIO(String name, Class<T> type, Schema schema) throws IOException {
        this.name = name;
        this.out = new ByteArrayOutputStream();

        DatumWriter<T> writer = new SpecificDatumWriter<>(type);
        dataFileWriter = new DataFileWriter<>(writer);
        dataFileWriter.create(schema, out);
    }

    public DataFileWriter<T> getDataFileWriter() {
        return dataFileWriter;
    }

    @Override
    public void close() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader("auth.properties"));
        String token = properties.getProperty("token");
        OkHttpClient client = new OkHttpClient();
        dataFileWriter.flush();
        RequestBody body = RequestBody.create(
                MediaType.parse("application/octet-stream"),
                out.toByteArray()
        );

        Request request = new Request.Builder()
                .url(String.format("http://127.0.0.1:6007/v1/AUTH_demo/data-export/%s", name))
                .addHeader("X-Auth-Token", token)
                .put(body)
                .build();
        System.out.printf("updating in openio file %s with %d bytes%n", name, out.size());
        client.newCall(request).execute();

        out.close();
    }
}
