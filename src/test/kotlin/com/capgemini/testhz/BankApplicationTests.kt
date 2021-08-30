package com.capgemini.testhz

import com.capgemini.mdao.AddAmountTask
import com.capgemini.rest.AddAmountRequest
import com.fasterxml.jackson.databind.json.JsonMapper
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.core.HazelcastInstance
import io.restassured.RestAssured
import io.restassured.config.ObjectMapperConfig
import io.restassured.http.ContentType
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.text.DateFormat
import java.time.Duration
import java.time.Instant
import kotlin.random.Random


class BankApplicationTests {

    companion object {

        private var hzclient: HazelcastInstance
        private const val nAccounts = 10
        private const val clientsPerAccount = 2
        val accountMap: Map<Int, List<Int>>
        private val mapper: JsonMapper = JsonMapper.builder().findAndAddModules()
            .defaultDateFormat(DateFormat.getDateInstance())
            .build()

        init {
            RestAssured.config.objectMapperConfig(ObjectMapperConfig().jackson2ObjectMapperFactory { _, _ ->
                return@jackson2ObjectMapperFactory mapper
            })

            val clientConfig = ClientConfig()
            clientConfig.clusterName = "bank"
            clientConfig.networkConfig
                .addAddress("127.0.0.1:5701")
                .addAddress("127.0.0.1:5702")
                .addAddress("127.0.0.1:5703")
            hzclient = HazelcastClient.newHazelcastClient(clientConfig)


            val nCombinations = nAccounts * clientsPerAccount
            accountMap = (0 until nCombinations).groupBy { it % (nCombinations / clientsPerAccount) }
            cassandraTest.session.execute("truncate table client;")
            cassandraTest.session.execute("truncate table account;")
            cassandraTest.session.execute("truncate table request;")
            cassandraTest.session.execute("truncate table idx_client_account;")

            accountMap.forEach {
                for (clientId in it.value) {
                    cassandraTest.clientDao.addAccountPermission(clientId, it.key)
                    cassandraTest.clientDao.create(ClientData.rndClient(clientId))
                }
                cassandraTest.accountDao.create(it.key, it.value.toSet(), 0L)
            }

        }
    }

    private fun callAddAmountService(httpPort: Int, clientData: ClientData, amount: Long): String {
        val start = Instant.now()
        val result = clientData.addAmount(amount = amount, serverPort = httpPort)
        val end = Instant.now()
        return String.format(
            "%4d|%3d_%4d|%10d|%10d|%4s|%30s|%30s|%10s",
            httpPort,
            clientData.account,
            clientData.clientId,
            amount,
            result.balance,
            if (result.error == null) "no" else "yes",
            start.toEpochMilli(),
            end.toEpochMilli(),
            Duration.between(start, end).toMillis()
        )
    }

    private fun callAddAmountServiceHZ(clientData: ClientData, amount: Long): String {

        val start = Instant.now()
        val result = hzclient.getExecutorService(BankConstants.ACCOUNT_AMOUNT)
            .submitToKeyOwner(AddAmountTask(clientData.account, amount), clientData.account)
            .get()

        val end = Instant.now()
        return String.format(
            "%3d_%4d|%10d|%10d|%4s|%30s|%30s|%10s",
            clientData.account,
            clientData.clientId,
            amount,
            result.key,
            if (result.value == null) "no" else "yes",
            start.toEpochMilli(),
            end.toEpochMilli(),
            Duration.between(start, end).toMillis()
        )
    }


    private fun callDummyService(httpPort: Int): String {
        val start = Instant.now()

        val request = AddAmountRequest(0, 0, 0)

        val statusCode = RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(request)
            .post("http://localhost:${httpPort}/dummy-post")
            .andReturn().statusCode()
        Assert.assertEquals(200, statusCode)

        val end = Instant.now()
        return String.format(
            "%4d|%30s|%30s|%10s",
            httpPort,
            start,
            end,
            Duration.between(start, end).toMillis()
        )
    }

    private fun callWithMeasures(linesAsync: List<() -> String>, file: String) {
        File(file).printWriter().use { writer ->
            val start = Instant.now()
            runBlocking {
                linesAsync.map { fn -> async { fn() } }
                    .forEach { writer.println(it.await()) }
            }
            val end = Instant.now()
            val duration = Duration.between(start, end).toMillis()
            writer.printf(
                "%s - %s = %s millis with the average of %s milis",
                start,
                end,
                duration,
                duration / linesAsync.size
            )
        }
    }

    private fun callRemovePermission(httpPort: Int, account: Int, client: Int) {
        val statusCode = RestAssured
            .with()
            .param("client", client)
            .param("account", account)
            .delete("http://localhost:${httpPort}/remove-permission")
            .andReturn().statusCode
        Assert.assertEquals(200, statusCode)
    }

    private fun callAddPermission(httpPort: Int, account: Int, client: Int) {
        val statusCode = RestAssured
            .with()
            .param("client", client)
            .param("account", account)
            .get("http://localhost:${httpPort}/add-permission")
            .andReturn().statusCode
        Assert.assertEquals(200, statusCode)
    }

    private fun testHttpCallsDummy() {
        val linesAsync: List<() -> String> = List(400) {
            { callDummyService(defaultServerPort()) }
        }

        callDummyService(defaultServerPort())
        callWithMeasures(linesAsync, "summary/dummy.txt")
    }

    @Test
    fun testHttpCallsLoadFromDB() {
        testHttpCallsDummy()
        val clients: List<ClientData> = accountMap.filter { it.key > 0 }.flatMap { entry ->
            entry.value.map {
                ClientData(it, entry.key)
            }
        }

        val times = 20
        val linesAsync: List<() -> String> = clients.flatMap { client: ClientData -> List(times) { client } }
            .map { { callAddAmountService(defaultServerPort(), it, Random.nextLong(-50, 51)) } }

        callDummyService(defaultServerPort())

        callWithMeasures(linesAsync, "summary/add-amount.txt")
    }

    @Test
    fun testHttpCallLoadFromDB() {
        callAddAmountService(defaultServerPort(), ClientData(0, 0), 10)
    }

    @Test
    fun removePermission() {
        runBlocking {
            callRemovePermission(defaultServerPort(), 0, 0)
            callAddPermission(defaultServerPort(), 0, 0)
        }
    }

    @Test
    fun testClient() {
        val clients: List<ClientData> = accountMap.filter { it.key > 0 }.flatMap { entry ->
            entry.value.map {
                ClientData(it, entry.key)
            }
        }

        val times = 20
        val linesAsync: List<() -> String> = clients.flatMap { client: ClientData -> List(times) { client } }
            .map { { callAddAmountServiceHZ(it, Random.nextLong(-50, 51)) } }

        callDummyService(defaultServerPort())

        callWithMeasures(linesAsync, "summary/add-amount-hz.txt")
    }

}