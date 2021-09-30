package com.capgemini.testhz

import com.capgemini.mdao.account.AccountMDAO
import com.capgemini.testhz.bank.CassandraLocalConf
import com.capgemini.testhz.bank.CassandraTestBank
import com.datastax.oss.driver.api.core.config.DefaultDriverOption
import com.datastax.oss.driver.api.core.config.DriverConfigLoader
import org.json.JSONObject
import java.time.Duration

const val serversNumber = 3
private var currentServerIdx = 0

fun defaultServerPort(): Int {
    if (currentServerIdx < serversNumber) {
        return 8080 + currentServerIdx++
    }
    currentServerIdx = 0
    return 8080 + currentServerIdx++
}

private val cassandraLocalConf = CassandraLocalConf()
fun options(httpPort: Int): String = JSONObject().put(
    "cassandra", JSONObject()
        .put("datacenter", cassandraLocalConf.datacenter)
        .put("host", cassandraLocalConf.host)
        .put("port", cassandraLocalConf.port)
).put("http", JSONObject()
    .put("port", httpPort)
).put("accountMDAO", AccountMDAO.Selector.LOCK.name)
    .toString()

