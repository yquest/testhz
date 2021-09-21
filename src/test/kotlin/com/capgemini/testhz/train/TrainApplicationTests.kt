package com.capgemini.testhz.train

import com.capgemini.cdao.train.SeatState
import com.capgemini.dto.Address
import com.capgemini.entity.train.*
import com.capgemini.rest.GenericResponse
import com.capgemini.rest.train.AddRailroadCarTravelRequest
import com.capgemini.rest.train.SetRouteRequest
import com.capgemini.rest.train.TravelRequest
import com.capgemini.rest.train.rc.FFacingPlaceRequest
import com.capgemini.rest.train.rc.delete.RailroadCarsDelete
import com.capgemini.rest.train.seat.OnConflict
import com.capgemini.rest.train.ticket.TicketPayedRequest
import com.capgemini.rest.train.ticket.TicketRequest
import com.capgemini.testhz.defaultServerPort
import com.capgemini.testhz.options
import com.fasterxml.jackson.databind.json.JsonMapper
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.core.HazelcastInstance
import io.restassured.RestAssured
import io.restassured.common.mapper.TypeRef
import io.restassured.config.ObjectMapperConfig
import io.restassured.http.ContentType
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.RandomUtils.nextInt
import org.apache.commons.lang3.StringUtils
import org.junit.Assert
import org.junit.Test
import java.lang.Thread.sleep
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class TrainApplicationTests {
    companion object {

        private val hzclient: HazelcastInstance
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
        }
    }
    private val listTimes = mutableListOf<Pair<Long, String>>()
    private fun setupRoute(setRouteRequest: SetRouteRequest): GenericResponse<*> {
        val httpPort = defaultServerPort()

        return RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(setRouteRequest)
            .post("http://localhost:${httpPort}/train/setup-route")
            .andReturn().`as`(GenericResponse::class.java)
    }

    private fun getSeatStates(list: List<SeatKey>): List<SeatState> {
        val httpPort = defaultServerPort()

        return RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(list)
            .post("http://localhost:${httpPort}/train/get-seat-states")
            .andReturn().`as`(object : TypeRef<List<SeatState>>() {})
    }

    private fun addRailroadCarTravel(railroadCar: AddRailroadCarTravelRequest): GenericResponse<*> {
        val httpPort = defaultServerPort()
        val value = RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(railroadCar)
            .post("http://localhost:${httpPort}/train/add-railroad-car-travel")
            .andReturn().`as`(GenericResponse::class.java)
        return value
    }

    private fun addStations(stations: Map<String, String>): String {
        val httpPort = defaultServerPort()
        val value = RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(stations)
            .post("http://localhost:${httpPort}/train/add-stations")
            .andReturn().asString()
        return value
    }

    private fun addTravel(railroadCar: TravelRequest) {
        lateinit var response: GenericResponse<*>
        for (i in (0 until 10)) {
            val httpPort = defaultServerPort()
            response = RestAssured
                .with()
                .contentType(ContentType.JSON)
                .body(railroadCar)
                .post("http://localhost:${httpPort}/train/add-travel")
                .andReturn().`as`(GenericResponse::class.java)
            if (response.result == "ok") {
                break
            }
            sleep(1000)
        }
        Assert.assertEquals("ok", response.result)
    }

    private fun deleteRailroadCars(railroadCarsDelete: RailroadCarsDelete): String {
        val httpPort = defaultServerPort()
        return RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(railroadCarsDelete)
            .post("http://localhost:${httpPort}/train/delete-railroad-cars")
            .andReturn().asString()
    }

    private fun createUser(userRequest: User): GenericResponse<Long> {
        val httpPort = defaultServerPort()
        return RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(userRequest)
            .post("http://localhost:${httpPort}/train/create-user")
            .andReturn().`as`(object : TypeRef<GenericResponse<Long>>() {})
    }

    private fun setSellingTravelState(updateTravelStateRequest: TravelKey): String {
        val httpPort = defaultServerPort()
        return RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(updateTravelStateRequest)
            .post("http://localhost:${httpPort}/train/set-selling-travel")
            .andReturn().asString()
    }

    private fun setMaintenanceTravelState(updateTravelStateRequest: TravelKey): String {
        val httpPort = defaultServerPort()
        return RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(updateTravelStateRequest)
            .post("http://localhost:${httpPort}/train/set-travel-maintenance")
            .andReturn().asString()
    }

    private fun resetSellingTravelState(travelKey: TravelKey): String {
        val httpPort = defaultServerPort()
        return RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(travelKey)
            .post("http://localhost:${httpPort}/train/reset-selling-travel")
            .andReturn().asString()
    }

    private fun addRailroadCars(addRailroadCars: List<RailroadCar>): List<*> {
        val httpPort = defaultServerPort()
        val list = RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(addRailroadCars)
            .post("http://localhost:${httpPort}/train/add-railroad-cars")
            .andReturn().`as`(List::class.java)
        return list as List<*>
    }

    private fun setSeatReserved(seatKeys: List<SeatKey>): String {
        val httpPort = defaultServerPort()
        return RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(seatKeys)
            .post("http://localhost:${httpPort}/train/set-seat-reserved")
            .andReturn().asString()
    }

    private fun setSeatOccupied(seatKeys: List<SeatKey>): String {
        val httpPort = defaultServerPort()
        return RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(seatKeys)
            .post("http://localhost:${httpPort}/train/set-seat-occupied")
            .andReturn().asString()
    }

    private fun ticketRequest(ticketRequest: TicketRequest): GenericResponse<Int> {
        val httpPort = defaultServerPort()
        return RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(ticketRequest)
            .post("http://localhost:${httpPort}/train/ticket-request")
            .andReturn().`as`(object : TypeRef<GenericResponse<Int>>() {})
    }

    private fun ticketPayed(ticketRequest: TicketPayedRequest): String {
        val httpPort = defaultServerPort()
        return RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(ticketRequest)
            .post("http://localhost:${httpPort}/train/ticket-payed")
            .andReturn().asString()
    }

    private fun findFacingFacePlace(request: FFacingPlaceRequest): GenericResponse<List<SeatKey>> {
        val httpPort = defaultServerPort()
        return RestAssured
            .with()
            .contentType(ContentType.JSON)
            .body(request)
            .post("http://localhost:${httpPort}/train/find-forward-facing")
            .andReturn().`as`(object : TypeRef<GenericResponse<List<SeatKey>>>() {})
    }

    private fun calls(stationsLabels: List<String>, prices: List<Int>, delays: List<Int>, startTravelTime: Instant) {
        val stations = stationsLabels.map { StringUtils.stripAccents(it.uppercase(Locale.getDefault())) }
        val stationsMap = stationsLabels.indices.associate { stations[it] to stationsLabels[it] }

        var startTime = System.currentTimeMillis()
        Assert.assertEquals("ok", addStations(stationsMap))
        var endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "addStations"
        val routeRequest = SetRouteRequest(delays, prices, stationsMap.keys.toList())


        startTime = System.currentTimeMillis()
        val routeResponse = setupRoute(routeRequest)
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "setupRoute"
        Assert.assertEquals(routeResponse.result, "ok")

        val route = routeResponse.value as Long
        val defaultSeats = setOf("LF1", "RF1", "LB1", "RB1", "LF2", "RF2", "LB2", "RB2")

        startTime = System.currentTimeMillis()
        val railroadCars = addRailroadCars(listOf(
            RailroadCar(null, "economic", defaultSeats),
            RailroadCar(null, "economic", defaultSeats),
            RailroadCar(null, "executive", defaultSeats),
            RailroadCar(null, "executive", defaultSeats)
        )).map { it as Long }
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "addRailroadCars"

        startTime = System.currentTimeMillis()
        val railroadCarTravelOK = addRailroadCarTravel(AddRailroadCarTravelRequest(
            startTravelTime,
            route,
            railroadCars
        ))
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "addRailroadCarTravel"
        Assert.assertEquals("ok", railroadCarTravelOK.result)

        addTravel(TravelRequest(startTravelTime, route, "alpha"))

        val travelKey = TravelKey(route, startTravelTime)
        startTime = System.currentTimeMillis()
        setSellingTravelState(travelKey)
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "setSellingTravelState"

        startTime = System.currentTimeMillis()
        val railroadCarTravelNOK = addRailroadCarTravel(AddRailroadCarTravelRequest(
            startTravelTime,
            route,
            railroadCars
        ))
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "addRailroadCarTravel"

        Assert.assertEquals(
            "invalid state TravelKey{route=%s, start=%s}:SELLING".format(route, startTravelTime),
            railroadCarTravelNOK.result
        )
        val firstRailroadCar = railroadCars[0]
        val threeFirstStations = (0 until 3).map { stations[it] }

        val seatKeys = threeFirstStations
            .map { station -> SeatKey(route, startTravelTime, firstRailroadCar, "RF1", station) }

        startTime = System.currentTimeMillis()
        val resultReservedOK = setSeatReserved(seatKeys)
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "setSeatReservedOK"
        Assert.assertEquals("ok", resultReservedOK)

        startTime = System.currentTimeMillis()
        Assert.assertEquals("ok", setMaintenanceTravelState(travelKey))
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "setMaintenanceTravelState"

        startTime = System.currentTimeMillis()
        Assert.assertEquals(
            "invalid state %s for state key %s".format(travelKey, TravelState.MAINTENANCE),
            setSeatReserved(seatKeys)
        )
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "setSeatReservedNOKTravelState"

        startTime = System.currentTimeMillis()
        Assert.assertEquals(
            "ok",
            deleteRailroadCars(RailroadCarsDelete(OnConflict.ERROR, mapOf(railroadCars[2] to null), false))
        )
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "deleteRailroadCars"

        startTime = System.currentTimeMillis()
        Assert.assertEquals("ok", resetSellingTravelState(travelKey))
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "resetSellingTravelState"

        startTime = System.currentTimeMillis()
        Assert.assertEquals(
            "to update the state to RESERVED the old state must be AVAILABLE",
            setSeatReserved(seatKeys)
        )
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "setSeatReservedNOKSeatNotAvailable"

        startTime = System.currentTimeMillis()
        Assert.assertEquals(
            "ok",
            setSeatOccupied(seatKeys)
        )
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "setSeatOccupied"

        startTime = System.currentTimeMillis()
        for (seatState in getSeatStates(seatKeys)) {
            Assert.assertEquals(SeatState.OCCUPIED, seatState)
        }
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "getSeatStates"

        startTime = System.currentTimeMillis()
        val resultFindFacingPlace = findFacingFacePlace(FFacingPlaceRequest(
            threeFirstStations[0], //PORTO
            threeFirstStations[1], //COIMBRA
            startTravelTime,
            route
        ))
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "findFacingFacePlace"

        Assert.assertEquals("ok", resultFindFacingPlace.result)
        for (found in resultFindFacingPlace.value) {
            for (seatKey in seatKeys) {
                Assert.assertNotEquals(seatKey, found)
            }
        }

        startTime = System.currentTimeMillis()
        val userResponse = createUser(User(
            Address(nextInt(), randomAlphabetic(10), randomAlphabetic(10)),
            null,
            LocalDate.now().minusYears(10).minusDays(nextInt(0,50).toLong()),
            randomAlphabetic(10)
        ))
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "createUser"

        val ticketRequest1 = TicketRequest(startTravelTime,
            route,
            railroadCars[1],
            stations[1],
            stations[2],
            userResponse.value,
            "LB1")
        val ticketRequest2 = TicketRequest(startTravelTime,
            route,
            railroadCars[1],
            stations[1],
            stations[2],
            userResponse.value,
            "LB2")
        val mapResolver = MapResolver { hzclient }
        val seatStateMap = mapResolver.seatStateMap

        startTime = System.currentTimeMillis()
        ticketRequest(ticketRequest1)
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "ticketRequestOK"

        startTime = System.currentTimeMillis()
        Assert.assertEquals(
            "to update the state to RESERVED the old state must be AVAILABLE",
            ticketRequest(ticketRequest1).result
        )
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "ticketRequestNOK"


        Assert.assertEquals(
            SeatState.RESERVED,
            seatStateMap[
                    SeatKey(route,
                        startTravelTime,
                        ticketRequest1.railroadCar,
                        ticketRequest1.seatPlace,
                        ticketRequest1.startStation
                    )
            ]
        )
        ticketRequest(ticketRequest2)
        Assert.assertEquals(
            SeatState.RESERVED,
            seatStateMap[
                    SeatKey(route,
                        startTravelTime,
                        ticketRequest2.railroadCar,
                        ticketRequest2.seatPlace,
                        ticketRequest2.startStation
                    )
            ]
        )

        startTime = System.currentTimeMillis()
        val resultPayed = ticketPayed(TicketPayedRequest(
            ticketRequest1.route,
            ticketRequest1.start,
            ticketRequest1.railroadCar,
            ticketRequest1.startStation,
            ticketRequest1.seatPlace,
            ticketRequest1.endStation
        ))
        endTime = System.currentTimeMillis()
        listTimes += (endTime - startTime) to "ticketPayed"

        Assert.assertEquals("ok", resultPayed)
        sleep(2000)
        Assert.assertEquals(
            SeatState.OCCUPIED,
            seatStateMap[
                    SeatKey(route,
                        startTravelTime,
                        ticketRequest1.railroadCar,
                        ticketRequest1.seatPlace,
                        ticketRequest1.startStation
                    )
            ]
        )

    }

    @Test
    fun testTrainNTimes() {
        testDummyGet()
        runBlocking {
            val tasks = (0 until 20).map {
                async {
                    calls(
                        stationsLabels = (0 until 10).map { randomAlphabetic(10) },
                        delays = (0 until 9).map { nextInt(10, 50) },
                        prices = (0 until 9).map { nextInt(10, 50) },
                        startTravelTime = Instant.now().toEpochMilli().let { now ->
                            Instant.ofEpochMilli(RandomUtils.nextLong(
                                now,
                                now + 1000 * 60 * 60 * 24 * 365.toLong()
                            ))
                        }
                    )
                }
            }
            tasks.forEach { it.await() }
        }
        listTimes.groupBy { it.second }
            .mapValues { it.value.map { pair -> pair.first }.average() }
            .forEach { entry ->
                println("average time of ${entry.key.padStart(40)} is ${entry.value}")
            }
    }

    @Test
    fun testTrain() {
        val stations = listOf("Porto", "Coimbrões", "Coimbra", "Lisboa")
        val stationsKeys = stations.map { StringUtils.stripAccents(it.uppercase(Locale.getDefault())) }
        Assert.assertEquals("ok", addStations((stationsKeys.indices).associate { stationsKeys[it] to stations[it] }))
        val routeRequest = SetRouteRequest(listOf(20, 20, 50), listOf(20, 20, 30), stationsKeys)
        val routeResponse = setupRoute(routeRequest)
        Assert.assertEquals(routeResponse.result, "ok")
        val route = routeResponse.value as Long
        val defaultSeats = setOf("LF1", "RF1", "LB1", "RB1", "LF2", "RF2", "LB2", "RB2")
        val railroadCars = addRailroadCars(listOf(
            RailroadCar(null, "economic", defaultSeats),
            RailroadCar(null, "economic", defaultSeats),
            RailroadCar(null, "executive", defaultSeats),
            RailroadCar(null, "executive", defaultSeats)
        )).map { it as Long }
        val start = LocalDateTime.of(2021, 10, 3, 2, 42).toInstant(ZoneOffset.UTC)
        val railroadCarTravelOK = addRailroadCarTravel(AddRailroadCarTravelRequest(
            start,
            route,
            railroadCars
        ))
        Assert.assertEquals("ok", railroadCarTravelOK.result)
        addTravel(TravelRequest(start, route, "alpha"))
        val travelKey = TravelKey(route, start)
        setSellingTravelState(travelKey)
        val railroadCarTravelNOK = addRailroadCarTravel(AddRailroadCarTravelRequest(
            start,
            route,
            railroadCars
        ))
        Assert.assertEquals(
            "invalid state TravelKey{route=%s, start=%s}:SELLING".format(route, start),
            railroadCarTravelNOK.result
        )
        val firstRailroadCar = railroadCars[0]
        val threeFirstStations = (0..3).map { stationsKeys[it] }
        val seatKeys = threeFirstStations
            .map { station -> SeatKey(route, start, firstRailroadCar, "RF1", station) }

        //reserve three first stations in the first railroadCar
        val resultReservedOK = setSeatReserved(seatKeys)
        Assert.assertEquals("ok", resultReservedOK)

        Assert.assertEquals("ok", setMaintenanceTravelState(travelKey))
        Assert.assertEquals(
            "invalid state %s for state key %s".format(travelKey, TravelState.MAINTENANCE),
            setSeatReserved(seatKeys)
        )

        println("Trying to delete car %s".format(railroadCars[2]))
        val railRoadCarsDeleteResult = deleteRailroadCars(
            RailroadCarsDelete(OnConflict.ERROR,
                mapOf(railroadCars[2] to null),
                false
            ))
        Assert.assertEquals("ok", railRoadCarsDeleteResult)
        Assert.assertEquals("ok", resetSellingTravelState(travelKey))
        Assert.assertEquals(
            "to update the state to RESERVED the old state must be AVAILABLE",
            setSeatReserved(seatKeys)
        )
        //mark in the three first stations in the first railroadCar the seat places as occupied
        Assert.assertEquals(
            "ok",
            setSeatOccupied(seatKeys)
        )

        for (seatState in getSeatStates(seatKeys)) {
            Assert.assertEquals(SeatState.OCCUPIED, seatState)
        }

        val resultFindFacingPlace = findFacingFacePlace(FFacingPlaceRequest(
            threeFirstStations[0], //PORTO
            threeFirstStations[1], //COIMBRA
            start,
            route
        ))

        Assert.assertEquals("ok", resultFindFacingPlace.result)
        for (found in resultFindFacingPlace.value) {
            Assert.assertFalse(seatKeys.contains(found))
        }

        val userResponse = createUser(User(
            Address(6300, "my street", "40"),
            null,
            LocalDate.of(1080, 5, 20),
            "Francisco"
        ))

        val ticketRequest1 = TicketRequest(start,
            route,
            railroadCars[1],
            stationsKeys[1],
            stationsKeys[2],
            userResponse.value,
            "LB1")
        val ticketRequest2 = TicketRequest(start,
            route,
            railroadCars[1],
            stationsKeys[1],
            stationsKeys[2],
            userResponse.value,
            "LB2")
        val seatStateMap = MapResolver { hzclient }.seatStateMap
        val result1 = ticketRequest(ticketRequest1)
        Assert.assertEquals(
            "to update the state to RESERVED the old state must be AVAILABLE",
            ticketRequest(ticketRequest1).result
        )
        Assert.assertEquals(
            SeatState.RESERVED,
            seatStateMap[
                    SeatKey(route,
                        start,
                        ticketRequest1.railroadCar,
                        ticketRequest1.seatPlace,
                        ticketRequest1.startStation
                    )
            ]
        )
        val result2 = ticketRequest(ticketRequest2)
        Assert.assertEquals(
            SeatState.RESERVED,
            seatStateMap[
                    SeatKey(route,
                        start,
                        ticketRequest2.railroadCar,
                        ticketRequest2.seatPlace,
                        ticketRequest2.startStation
                    )
            ]
        )

        Assert.assertEquals(GenericResponse.createOk(18), result1)
        Assert.assertEquals(GenericResponse.createOk(18), result2)

        val resultPayed = ticketPayed(TicketPayedRequest(
            ticketRequest1.route,
            ticketRequest1.start,
            ticketRequest1.railroadCar,
            ticketRequest1.startStation,
            ticketRequest1.seatPlace,
            ticketRequest1.endStation
        ))
        Assert.assertEquals("ok", resultPayed)
        sleep(2000)
            Assert.assertEquals(
                SeatState.OCCUPIED,
                seatStateMap[
                        SeatKey(route,
                            start,
                            ticketRequest1.railroadCar,
                            ticketRequest1.seatPlace,
                            ticketRequest1.startStation
                        )
                ]
            )
    }

    private fun testDummyGet() {
        val httpPort = defaultServerPort()
        val map = RestAssured
            .get("http://localhost:${httpPort}/train/dummy-get")
            .andReturn().`as`(Map::class.java)
        println(map)
    }

    @Test
    fun printJsonEntry(){
        println(options(8080))
    }
}