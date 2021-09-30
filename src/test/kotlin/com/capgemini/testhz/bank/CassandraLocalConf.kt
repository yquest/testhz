package com.capgemini.testhz.bank

import com.datastax.oss.driver.api.core.CqlSession
import java.net.InetSocketAddress

class CassandraLocalConf(val host: String = "localhost", val datacenter: String = "datacenter1", val port: Int = 9042) {
    fun createSession(keyspace: String): CqlSession {
        return CqlSession.builder()
            .addContactPoint(InetSocketAddress(host, port))
            .withLocalDatacenter(datacenter)
            .withKeyspace(keyspace)
            .build()
    }
}