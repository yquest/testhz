package com.capgemini.testhz.bank

import com.capgemini.cdao.bank.AccountCDAO
import com.capgemini.cdao.bank.ClientCDAO
import com.datastax.oss.driver.api.core.CqlSession
import java.net.InetSocketAddress

class CassandraTest(
    val host: String = "127.0.0.1",
    val port: Int = 9042,
    val keyspace: String = "bank",
    val datacenter: String = "datacenter1",
) {
    val accountDao: AccountCDAO
    val clientDao: ClientCDAO
    val session: CqlSession

    init {
        session = CqlSession.builder()
            .addContactPoint(InetSocketAddress(host, port))
            .withLocalDatacenter(datacenter)
            .withKeyspace(keyspace)
            .build()
        accountDao = AccountCDAO(session)
        clientDao = ClientCDAO(session)
    }
}