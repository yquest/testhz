package com.capgemini.testhz.bank

import com.capgemini.cdao.bank.AccountCDAO
import com.capgemini.cdao.bank.ClientCDAO
import com.capgemini.dto.bank.client.AccountException
import com.capgemini.mdao.account.AddAmountTask
import com.capgemini.mdao.account.TransferAmountTask
import com.capgemini.rest.bank.AddAmountRequest
import com.capgemini.testhz.TestHZConstants
import com.capgemini.testhz.defaultServerPort
import com.datastax.oss.driver.api.core.CqlSession
import com.fasterxml.jackson.databind.json.JsonMapper
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.core.HazelcastInstance
import io.restassured.RestAssured
import io.restassured.config.ObjectMapperConfig
import io.restassured.http.ContentType
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
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
        private lateinit var cassandraTestBank:CqlSession
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
            cassandraTestBank.execute("truncate table client;")
            cassandraTestBank.execute("truncate table account;")
            cassandraTestBank.execute("truncate table request;")
            cassandraTestBank.execute("truncate table idx_client_account;")

            val clientCDao = ClientCDAO(cassandraTestBank)
            val accountCdao = AccountCDAO(cassandraTestBank)
            accountMap.forEach {
                for (clientId in it.value) {
                    clientCDao.addAccountPermission(clientId, it.key)
                    clientCDao.create(ClientData.rndClient(clientId))
                }
                accountCdao.create(it.key, it.value.toSet(), 0L)
            }

        }

        @JvmStatic
        @BeforeClass
        fun beforeTests(){
            cassandraTestBank = CassandraLocalConf().createSession("bank")
        }
        @JvmStatic
        @AfterClass
        fun afterTests(){
            cassandraTestBank.close()
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
        val result = hzclient.getMap<Int,Long>(BankConstants.ACCOUNT_AMOUNT)
            .submitToKey(clientData.account,AddAmountTask(amount))
            .toCompletableFuture().get()

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
            .post("http://localhost:${httpPort}/bank/dummy-post")
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
            .delete("http://localhost:${httpPort}/bank/remove-permission")
            .andReturn().statusCode
        Assert.assertEquals(200, statusCode)
    }

    private fun callAddPermission(httpPort: Int, account: Int, client: Int) {
        val statusCode = RestAssured
            .with()
            .param("client", client)
            .param("account", account)
            .get("http://localhost:${httpPort}/bank/add-permission")
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
    fun testHttpTransfer(){
        val amountMap = hzclient.getMap<Int, Long>(BankConstants.ACCOUNT_AMOUNT)
        amountMap[0] = 100
        amountMap[1] = 0

        val response = AccountData.transferAmount(accountSrc = 0, accountDst = 1, client = 0, amount = 50)

        Assert.assertNull(response.error)
        Assert.assertEquals(50L,amountMap[0])
        Assert.assertEquals(50L,amountMap[1])
        Assert.assertEquals(50L,response.destAmount)
        Assert.assertEquals(50L,response.sourceAmount)

        val responseError = AccountData.transferAmount(accountSrc = 0, accountDst = 1, client = 0, amount = 60)

        Assert.assertEquals(AccountException.Code.NEGATIVE_AMOUNT.name, responseError.error)
        Assert.assertNull(responseError.sourceAmount)
        Assert.assertNull(responseError.destAmount)
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

        callWithMeasures(linesAsync, "summary/add-amount-hz.txt")

        val amountMap = hzclient.getMap<Int, Long>(BankConstants.ACCOUNT_AMOUNT)
        amountMap[0] = 100
        amountMap[1] = 0

        val response = hzclient.getExecutorService(TestHZConstants.DEFAULT)
            .submitToKeyOwner(TransferAmountTask(0,1,50),0).get()

        Assert.assertNull(response.error)
        Assert.assertEquals(50L,amountMap[0])
        Assert.assertEquals(50L,amountMap[1])
        Assert.assertEquals(50L,response.destAmount)
        Assert.assertEquals(50L,response.sourceAmount)

        val responseError = hzclient.getExecutorService(TestHZConstants.DEFAULT)
            .submitToKeyOwner(TransferAmountTask(0,1,60),0).get()

        Assert.assertEquals(AccountException.Code.NEGATIVE_AMOUNT.name, responseError.error)
        Assert.assertNull(responseError.sourceAmount)
        Assert.assertNull(responseError.destAmount)
    }

}