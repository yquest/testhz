package com.capgemini.testhz.bank

import com.capgemini.mdao.account.TransferResponse
import com.capgemini.rest.bank.AddAmountRequest
import com.capgemini.rest.bank.AddAmountResponse
import com.capgemini.rest.bank.NewAccountRequest
import com.capgemini.rest.bank.TransferAmountRequest
import com.capgemini.testhz.defaultServerPort
import io.restassured.RestAssured
import io.restassured.http.ContentType
import java.util.*

class AccountData(val id: Int, val clients: List<Int>) {
    companion object {
        fun createAccountData(
            idx: Int = defaultServerPort(),
            id: Int,
            clients: Collection<Int> = emptyList()
        ): AccountData {
            val newAccountRequest =
                NewAccountRequest(id, 0L, clients.toSet())
            RestAssured
                .with()
                .contentType(ContentType.JSON)
                .body(newAccountRequest)
                .post("http://localhost:808${idx}/add-new-account")
                .andReturn().`as`(UUID::class.java)

            return AccountData(id, clients.toList())
        }

        fun addAmount(
            httpPort: Int = defaultServerPort(),
            account: Int,
            client: Int,
            amount: Long
        ): AddAmountResponse {
            val request = AddAmountRequest(account, client, amount)
            return RestAssured
                .with()
                .contentType(ContentType.JSON)
                .body(request)
                .post("http://localhost:${httpPort}/add-amount")
                .andReturn().`as`(AddAmountResponse::class.java)
        }

        fun transferAmount(
            httpPort: Int = defaultServerPort(),
            accountSrc: Int,
            accountDst: Int,
            client: Int,
            amount: Long,
        ): TransferResponse {
            val request =
                TransferAmountRequest(accountSrc, accountDst, client, amount)
            return RestAssured
                .with()
                .contentType(ContentType.JSON)
                .body(request)
                .post("http://localhost:${httpPort}/transfer-amount")
                .andReturn().`as`(TransferResponse::class.java)
        }

    }

    fun transferAmount(serverIdx: Int = defaultServerPort(), client: Int, desAccount: Int, amount: Long) =
        transferAmount(serverIdx, client, this.id, desAccount, amount)

    fun addAmount(serverIdx: Int = defaultServerPort(), client: Int, amount: Long): AddAmountResponse =
        addAmount(serverIdx, this.id, client, amount)

    fun toClientDataList() = clients.map { ClientData(it, id) }

    override fun toString(): String {
        return "AccountData(id=$id, clients=$clients)"
    }


}