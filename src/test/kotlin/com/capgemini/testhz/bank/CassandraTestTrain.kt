package com.capgemini.testhz.bank

import com.capgemini.cdao.train.RailroadCarCDAO
import com.capgemini.cdao.train.RouteCDAO
import com.datastax.oss.driver.api.core.CqlSession

class CassandraTestTrain(session: CqlSession) {
    val railroadCarCDAO = RailroadCarCDAO(session)
    val routeCDAO: RouteCDAO = RouteCDAO(session)
}