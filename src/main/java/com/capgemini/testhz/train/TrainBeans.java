package com.capgemini.testhz.train;

import com.datastax.oss.driver.api.core.CqlSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.net.InetSocketAddress;

@Configuration
public class TrainBeans {

    @Bean(name = "train-session")
    @Scope("singleton")
    public CqlSession getCqlSession(@Value("${server.options}") String options) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(options);
        final JsonNode cassandra = jsonNode.get("cassandra");
        int port = cassandra.get("port").asInt();
        String host = cassandra.get("host").asText();
        String keyspace = "train";
        String datacenter = cassandra.get("datacenter").asText();

        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress(host, port))
                .withLocalDatacenter(datacenter)
                .withKeyspace(keyspace)
                .build();
    }

}
