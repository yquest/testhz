package com.capgemini.testhz

import com.capgemini.mdao.account.AccountMDAO
import com.capgemini.testhz.bank.CassandraTest
import org.json.JSONObject

const val serversNumber = 3
private var currentServerIdx = 0

fun defaultServerPort(): Int {
    if (currentServerIdx < serversNumber) {
        return 8080 + currentServerIdx++
    }
    currentServerIdx = 0
    return 8080 + currentServerIdx++
}

val cassandraTest = CassandraTest()
fun options(httpPort: Int): String = JSONObject().put(
    "cassandra", JSONObject()
        .put("datacenter", cassandraTest.datacenter)
        .put("keyspace", cassandraTest.keyspace)
        .put("host", cassandraTest.host)
        .put("port", cassandraTest.port)
).put("http", JSONObject()
    .put("port", httpPort)
).put("accountMDAO", AccountMDAO.Selector.LOCK.name)
    .toString()

