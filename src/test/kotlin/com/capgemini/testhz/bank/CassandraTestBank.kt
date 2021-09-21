package com.capgemini.testhz.bank

import com.capgemini.cdao.bank.AccountCDAO
import com.capgemini.cdao.bank.ClientCDAO
import com.datastax.oss.driver.api.core.CqlSession

class CassandraTestBank(val session: CqlSession) {
    val accountDao: AccountCDAO
    val clientDao: ClientCDAO

    init {
        accountDao = AccountCDAO(session)
        clientDao = ClientCDAO(session)
    }
}