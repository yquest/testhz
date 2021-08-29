package com.capgemini.testhz;

import com.capgemini.cdao.AccountCDAO;
import com.capgemini.cdao.ClientCDAO;
import com.capgemini.mdao.AccountMDAO;
import com.capgemini.store.ClientAccount;
import com.capgemini.store.MapAccountClientsStore;
import com.capgemini.store.MapAccountStore;
import com.datastax.oss.driver.api.core.CqlSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.MapStoreFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.net.InetSocketAddress;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class BankApplication {

    public static void main(String[] args) throws JsonProcessingException {

        String options = args[0];
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(options);

        SpringApplication app = new SpringApplication(BankApplication.class);

        Map<String,Object> props = new HashMap<>();
        props.put("server.port", jsonNode.get("http").get("port").asInt());
        props.put("server.options", options);
        app.setDefaultProperties(props);
        app.run();
    }

    @Bean
    @Scope("singleton")
    public AccountMDAO getClientMDAO(HazelcastInstance hazelcast, @Value("${server.options}") String options) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(options);
        return AccountMDAO.Selector.valueOf(jsonNode.get("accountMDAO").asText()).createAccountMDAO(hazelcast);
    }

    @Bean
    @Scope("singleton")
    public ClientCDAO getClientCDAO(CqlSession session) {
        return new ClientCDAO(session);
    }

    @Bean
    @Scope("singleton")
    public AccountCDAO getAccountCDAO(CqlSession session) {
        return new AccountCDAO(session);
    }

    @Bean
    @Scope("singleton")
    public CqlSession getCqlSession(@Value("${server.options}") String options) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(options);
        final JsonNode cassandra = jsonNode.get("cassandra");
        int port = cassandra.get("port").asInt();
        String host = cassandra.get("host").asText();
        String keyspace = cassandra.get("keyspace").asText();
        String datacenter = cassandra.get("datacenter").asText();

        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress(host, port))
                .withLocalDatacenter(datacenter)
                .withKeyspace(keyspace)
                .build();
    }

    @Bean
    @Scope("singleton")
    public HazelcastInstance getHazlecastInstance(
            AccountCDAO accountCDAO,
            ClientCDAO clientCDAO
    ) {
        Config config = new Config();
        config.setClusterName("bank");
        HazelcastInstance hazlecast;

        int saveAfter = 6;//seconds
        int idleMax = 3;//seconds
        MapConfig balanceConfigMap = config.getMapConfig(BankConstants.ACCOUNT_AMOUNT);
        balanceConfigMap.setMaxIdleSeconds(idleMax);
        MapStoreConfig mapBalanceStoreConfig = new MapStoreConfig();
        mapBalanceStoreConfig.setEnabled(true);
        mapBalanceStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapBalanceStoreConfig.setWriteDelaySeconds(saveAfter);
        mapBalanceStoreConfig.setWriteCoalescing(true);
        balanceConfigMap.setMapStoreConfig(mapBalanceStoreConfig);

        mapBalanceStoreConfig.setFactoryImplementation((MapStoreFactory<Integer, Long>) (mapName, properties) ->
                new MapAccountStore(accountCDAO)
        );
        balanceConfigMap.setMapStoreConfig(mapBalanceStoreConfig);
        balanceConfigMap.setBackupCount(2);

        MapConfig accountClientsConfigMap = config.getMapConfig(BankConstants.ACCOUNT_CLIENTS);
        accountClientsConfigMap.setMaxIdleSeconds(idleMax);
        MapStoreConfig mapAccountClientsStoreConfig = new MapStoreConfig();
        mapAccountClientsStoreConfig.setEnabled(true);
        mapAccountClientsStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapAccountClientsStoreConfig.setWriteDelaySeconds(saveAfter);
        mapAccountClientsStoreConfig.setWriteCoalescing(true);
        mapAccountClientsStoreConfig
                .setFactoryImplementation((MapStoreFactory<ClientAccount, Boolean>) (mapName, properties) ->
                        new MapAccountClientsStore(clientCDAO, accountCDAO)
                );
        accountClientsConfigMap.setMapStoreConfig(mapAccountClientsStoreConfig);
        accountClientsConfigMap.setBackupCount(2);
        hazlecast = Hazelcast.newHazelcastInstance(config);
        return hazlecast;
    }

}
