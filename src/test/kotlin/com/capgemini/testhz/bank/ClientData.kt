package com.capgemini.testhz.bank

import com.capgemini.dto.Address
import com.capgemini.dto.bank.client.Client
import com.capgemini.rest.bank.AddAmountResponse
import com.capgemini.testhz.defaultServerPort
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.apache.commons.lang3.RandomStringUtils
import java.time.LocalDate
import java.util.*

class ClientData(val clientId: Int, val account: Int) {
    companion object {
        fun rndClient(id: Int): Client = Client(
            id,
            RandomStringUtils.randomAlphabetic(10),
            LocalDate.of(2021, 1, 2),
            Address(
                123,
                "street",
                "door"
            )
        )

        fun createClient(httpPort: Int = defaultServerPort(), id: Int, client: Client = rndClient(id)): UUID {
            return RestAssured
                .with()
                .contentType(ContentType.JSON)
                .body(client)
                .post("http://localhost:${httpPort}/bank/create-client")
                .andReturn().`as`(UUID::class.java)
        }
    }

    override fun toString(): String {
        return "ClientData(clientId=$clientId, account=$account)"
    }

    fun addAmount(serverPort: Int, amount: Long): AddAmountResponse {
        return AccountData.addAmount(serverPort, account, clientId, amount)
    }
}
