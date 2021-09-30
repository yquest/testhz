package com.capgemini.testhz.train;

import com.capgemini.cdao.train.RailroadCarTravelCDAO;
import com.capgemini.cdao.train.SeatState;
import com.capgemini.entity.train.*;
import com.capgemini.mdao.train.CallableCountSeatsAvailable;
import com.capgemini.mdao.train.CallableFFSeatsAvailable;
import com.capgemini.mdao.train.TicketChecker;
import com.capgemini.rest.GenericResponse;
import com.capgemini.rest.train.AddRailroadCarTravelRequest;
import com.capgemini.rest.train.SetRouteRequest;
import com.capgemini.rest.train.TravelRequest;
import com.capgemini.rest.train.rc.FFacingPlaceRequest;
import com.capgemini.rest.train.rc.delete.RailroadCarsDelete;
import com.capgemini.rest.train.seat.OnConflict;
import com.capgemini.rest.train.ticket.TicketPayedRequest;
import com.capgemini.rest.train.ticket.TicketRequest;
import com.capgemini.store.train.RailroadCarTravelKey;
import com.capgemini.testhz.TestHZConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.scheduledexecutor.TaskUtils;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionalMap;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.capgemini.rest.GenericResponse.createNok;
import static com.capgemini.rest.GenericResponse.createOk;

@RestController
@RequestMapping(value = "train", produces = MediaType.APPLICATION_JSON_VALUE)
public class TrainController {
    @Autowired
    private DataManager dataManager;

    @Autowired
    private HazelcastInstance hz;

    @PostMapping("add-railroad-car")
    Long addRailroadCar(@RequestBody RailroadCar railroadCar) {
        long id = dataManager.getIdGenRailroadCar().newId();
        dataManager.getRailroadCarCDAO().insert(id, railroadCar.getTravelType(), railroadCar.getSeats());
        dataManager.getRailroadCarMap().set(id, railroadCar, 5, TimeUnit.SECONDS);
        return id;
    }

    @PostMapping("add-railroad-car-travel")
    GenericResponse<Long> addRailroadCarTravel(@RequestBody AddRailroadCarTravelRequest railroadCarTravel) {
        final TravelKey travelKey = new TravelKey(railroadCarTravel.getRoute(), railroadCarTravel.getStart());
        TravelState travelState = Optional.ofNullable(dataManager.getTravelMap().get(travelKey)).orElse(TravelState.PREPARING);

        if (travelState != TravelState.PREPARING && travelState != TravelState.MAINTENANCE) {
            return new GenericResponse<>("invalid state " + travelKey + ":" + travelState, null);
        }
        if (!dataManager.getRouteStationsMap().containsKey(railroadCarTravel.getRoute())) {
            return new GenericResponse<>("there is no route with id:" + railroadCarTravel.getRoute(), null);
        }

        final IMap<Long, RailroadCar> railroadCarMap = dataManager.getRailroadCarMap();
        Map<Long, RailroadCar> mapToApplyTravel = new HashMap<>();
        for (Long railroadCarId : railroadCarTravel.getRailroadCars()) {
            railroadCarMap.lock(railroadCarId);
            RailroadCar railroadCar = railroadCarMap.get(railroadCarId);
            if (!railroadCarMap.containsKey(railroadCarId)) {
                return new GenericResponse<>("there is no railroadCar with id:" + railroadCarTravel.getRailroadCars(), null);
            }
            mapToApplyTravel.put(railroadCarId, railroadCar);
        }
        try {
            for (Map.Entry<Long, RailroadCar> entry : mapToApplyTravel.entrySet()) {
                final RailroadCar railroadCar = entry.getValue();
                railroadCarMap.set(entry.getKey(), new RailroadCar(travelKey, railroadCar.getTravelType(), railroadCar.getSeats()));
            }
            dataManager.getRailroadCarCDAO().updateTravels(mapToApplyTravel.keySet(), travelKey);
            dataManager.getRailroadCarTravelCDAO().insert(railroadCarTravel.getRoute(), railroadCarTravel.getStart(), railroadCarTravel.getRailroadCars());
            dataManager.getRailroadCarTravelMap().set(travelKey, railroadCarTravel.getRailroadCars());
        } finally {
            mapToApplyTravel.forEach((k, v) -> dataManager.getRailroadCarMap().unlock(k));
        }
        return new GenericResponse<>("ok", null);
    }

    @PostMapping("add-railroad-cars")
    List<Long> addRailroadCars(@RequestBody List<RailroadCar> addRailroadCars) {
        List<Long> ids = new ArrayList<>();
        for (RailroadCar railroadCar : addRailroadCars) {
            long id = dataManager.getIdGenRailroadCar().newId();
            dataManager.getRailroadCarCDAO().insert(id, railroadCar.getTravelType(), railroadCar.getSeats());
            dataManager.getRailroadCarMap().set(id, railroadCar, 5, TimeUnit.SECONDS);
            ids.add(id);
        }
        return ids;
    }

    @PostMapping("add-stations")
    String addStations(@RequestBody Map<String, String> stations) {
        for (Map.Entry<String, String> entry : stations.entrySet()) {
            dataManager.getStationsCDAO().insert(entry.getKey(), entry.getValue());
        }
        dataManager.getStationsMap().putAll(stations);
        return "ok";
    }

    @PostMapping("add-travel")
    GenericResponse<String> addTravel(@RequestBody TravelRequest travelRequest) {
        IMap<Long, List<String>> routeMap = dataManager.getRouteStationsMap();

        if (!routeMap.containsKey(travelRequest.getRoute())) {
            return new GenericResponse<>("missing the route in db:" + travelRequest.getRoute(), null);
        }

        final TravelKey travelKey = new TravelKey(travelRequest.getRoute(), travelRequest.getStart());
        List<Long> railroadCars = dataManager.getRailroadCarTravelMap().get(travelKey);

        if (!Optional.ofNullable(railroadCars).filter(e -> !e.isEmpty()).isPresent()) {
            return new GenericResponse<>("no railroadCars in travel:" + travelKey, null);
        }

        IMap<TravelKey, TravelState> travelMap = dataManager.getTravelMap();
        if (travelMap.containsKey(travelKey)) {
            return new GenericResponse<>("already exists:" + travelKey, null);
        }
        dataManager.getTravelCDAO().insert(travelRequest.getRoute(), travelRequest.getStart(), travelRequest.getTrainType());
        travelMap.set(travelKey, TravelState.PREPARING);
        return new GenericResponse<>("ok", travelRequest.getTrainType());
    }

    @PostMapping("create-user")
    GenericResponse<Long> createUser(@RequestBody User user) {
        long id = dataManager.getIdGenUser().newId();
        dataManager.getUserCDAO().insert(id, user.getName(), user.getBirthDate(), user.getAddress());
        return new GenericResponse<>("ok", id);
    }

    @PostMapping("delete-railroad-cars")
    String deleteRailroadCars(@RequestBody RailroadCarsDelete railroadCarsDelete) {
        final IMap<Long, RailroadCar> railroadCarMap = dataManager.getRailroadCarMap();
        IMap<RailroadCarTravelKey, Set<SeatPlace>> seatStatesMap = dataManager.getSeatsByRailroadCarMap();
        List<Map.Entry<Long, TravelKey>> carsInTravel = new ArrayList<>();
        List<RailroadCarTravelKey> seatStatePartitionsToDelete = new ArrayList<>();
        for (Long railroadCarId : railroadCarsDelete.getRailroadCarMap().keySet()) {
            railroadCarMap.lock(railroadCarId);
            RailroadCar current = railroadCarMap.get(railroadCarId);
            if (current.getTravelKey() != null) {
                carsInTravel.add(Pair.of(railroadCarId, current.getTravelKey()));
            }
        }
        try {
            final IMap<TravelKey, TravelState> travelMap = dataManager.getTravelMap();
            if (!carsInTravel.isEmpty()) {
                for (Map.Entry<Long, TravelKey> carEntry : carsInTravel) {

                    final TravelKey travelKey = carEntry.getValue();

                    final TravelState travelState = travelMap.get(travelKey);
                    System.out.printf("travelKey %s is %s%n", travelKey, travelState);
                    final boolean validTravelState = !railroadCarsDelete.isIgnoreTravelState() && (
                            Optional.ofNullable(travelState)
                                    .map(state ->
                                            state == TravelState.PREPARING || state == TravelState.MAINTENANCE)
                                    .orElse(false)
                    );
                    if (!validTravelState) {
                        return String.format("travel state of %s invalid", travelKey);
                    }
                    OnConflict onConflict = Optional.ofNullable(railroadCarsDelete.getRailroadCarMap().get(carEntry.getKey()))
                            .map(e -> railroadCarsDelete.getOnConflict())
                            .orElse(OnConflict.ERROR);

                    final RailroadCarTravelKey railroadCarTravelKey = new RailroadCarTravelKey(
                            travelKey.getRoute(),
                            travelKey.getStart(),
                            carEntry.getKey()
                    );
                    if (onConflict == OnConflict.ERROR) {
                        Set<SeatPlace> seats = seatStatesMap.get(railroadCarTravelKey);
                        boolean allSeatsAvailable = seats.stream()
                                .allMatch(seatPlace -> seatPlace.getSeatState() == SeatState.AVAILABLE);
                        if (allSeatsAvailable) {
                            seatStatePartitionsToDelete.add(railroadCarTravelKey);
                        } else {
                            return String.format("dependency %s not marked as force on conflict", railroadCarTravelKey);
                        }
                    } else {
                        seatStatePartitionsToDelete.add(railroadCarTravelKey);
                    }
                }
            }
            RailroadCarTravelCDAO railroadCarTravelCDAO = dataManager.getRailroadCarTravelCDAO();
            final IMap<TravelKey, List<Long>> railroadCarTravelMap = dataManager.getRailroadCarTravelMap();
            for (RailroadCarTravelKey key : seatStatePartitionsToDelete) {
                railroadCarTravelCDAO.delete(key);
                dataManager.getSeatMultiStore().deleteCarOnTravel(key);
                TravelKey travelKey = new TravelKey(key.getRoute(), key.getStart());
                railroadCarTravelMap.lock(travelKey);
                try {
                    List<Long> listRailRoadCars = railroadCarTravelMap.get(travelKey);
                    listRailRoadCars.remove(key.getRailroadCar());
                    if (listRailRoadCars.isEmpty()) {
                        railroadCarTravelMap.delete(travelKey);
                    } else {
                        railroadCarTravelMap.set(travelKey, listRailRoadCars);
                    }
                } finally {
                    railroadCarTravelMap.unlock(travelKey);
                }
            }
            for (Long railroadCarId : railroadCarsDelete.getRailroadCarMap().keySet()) {
                dataManager.getRailroadCarCDAO().delete(railroadCarId);
                railroadCarMap.remove(railroadCarId);
            }
        } finally {
            for (Long railroadCarId : railroadCarsDelete.getRailroadCarMap().keySet()) {
                railroadCarMap.unlock(railroadCarId);
            }
        }
        return "ok";
    }

    @GetMapping("dummy-get")
    Map<String, String> dummyGet() {
        long id = hz.getFlakeIdGenerator("test").newId();
        Map<String, String> map = new HashMap<>();
        long after2018 = id >> (16 + 6);// 16 bits from nodeId and 6 for sequence
        long before2018 = LocalDateTime.of(2018, 1, 1, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
        map.put("current-millis", Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toString());
        map.put("id-time-extracted", Instant.ofEpochMilli(before2018 + after2018).atZone(ZoneId.systemDefault()).toString());
        return map;
    }

    @PostMapping("find-forward-facing")
    GenericResponse<List<SeatKey>> findFF(@RequestBody FFacingPlaceRequest request) {
        List<String> stations = dataManager.getRouteStationsMap().get(request.getRoute());
        int idxStart = stations.indexOf(request.getStartStation());
        if (idxStart == -1) {
            new GenericResponse<>("the start station doesn't in the list", null);
        }
        int idxEnd = stations.indexOf(request.getEndStation());
        if (idxEnd == -1) {
            new GenericResponse<>("the end station doesn't in the list", null);
        }

        List<Future<List<SeatKey>>> futures = new ArrayList<>();
        final TravelKey travelKey = new TravelKey(request.getRoute(), request.getStart());
        for (Long car : dataManager.getRailroadCarTravelMap().get(travelKey)) {
            final RailroadCarTravelKey railroadCarTravelKey = travelKey.createRailroadCarTravelKey(car);
            final List<String> subList = stations.subList(idxStart, idxEnd);
            Future<List<SeatKey>> result = hz.getExecutorService(TestHZConstants.DEFAULT)
                    .submitToKeyOwner(
                            new CallableFFSeatsAvailable(railroadCarTravelKey, new ArrayList<>(subList)),
                            railroadCarTravelKey.hashCode()
                    );
            futures.add(result);
        }
        List<SeatKey> seatKeyList = new ArrayList<>();
        for (Future<List<SeatKey>> future : futures) {
            try {
                seatKeyList.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return new GenericResponse<>("unexpeced error", null);
            }
        }
        return new GenericResponse<>("ok", seatKeyList);
    }

    @PostMapping("get-seat-states")
    List<SeatState> getSeatStates(@RequestBody List<SeatKey> seatKeyList) {
        final IMap<SeatKey, SeatState> seatStateMap = dataManager.getSeatStateMap();
        Map<SeatKey, SeatState> map = seatStateMap.getAll(new HashSet<>(seatKeyList));
        List<SeatState> states = new ArrayList<>(seatKeyList.size());
        for (SeatKey seatKey : seatKeyList) {
            states.add(map.get(seatKey));
        }
        return states;
    }

    @PostMapping("reset-selling-travel")
    String resetTravelMaintenance(@RequestBody TravelKey travelKey) {
        final IMap<TravelKey, TravelState> travelMap = dataManager.getTravelMap();
        if (!travelMap.replace(travelKey, TravelState.MAINTENANCE, TravelState.SELLING)) {
            return String.format("change to %s is invalid for %s", TravelState.SELLING, travelKey);
        }
        return "ok";
    }

    @PostMapping("set-seat-occupied")
    String setOccupied(@RequestBody List<SeatKey> seats) {
        TravelKey travelKey = new TravelKey(seats.get(0).getRoute(), seats.get(0).getStart());
        for (int i = 1; i < seats.size(); i++) {
            if (!travelKey.equals(new TravelKey(seats.get(i).getRoute(), seats.get(i).getStart()))) {
                return "only one travel key is allowed";
            }
        }
        TravelState travelState = dataManager.getTravelMap().get(travelKey);
        if (travelState != TravelState.SELLING) {
            return String.format("invalid state %s for state key %s", travelKey, travelState);
        }
        return dataManager.getSeatMultiStore().replaceAllSeatState(seats, SeatState.OCCUPIED);
    }

    @PostMapping("set-seat-reserved")
    String setReserved(@RequestBody List<SeatKey> seats) {
        TravelKey travelKey = new TravelKey(seats.get(0).getRoute(), seats.get(0).getStart());
        for (int i = 1; i < seats.size(); i++) {
            if (!travelKey.equals(new TravelKey(seats.get(i).getRoute(), seats.get(i).getStart()))) {
                return "only one travel key is allowed";
            }
        }
        TravelState travelState = dataManager.getTravelMap().get(travelKey);
        if (travelState != TravelState.SELLING) {
            return String.format("invalid state %s for state key %s", travelKey, travelState);
        }
        return dataManager.getSeatMultiStore().replaceAllSeatState(seats, SeatState.RESERVED);
    }

    @PostMapping(value = "set-selling-travel")
    String setSellingTravel(@RequestBody TravelKey request) {
        TravelKey travelKey = new TravelKey(request.getRoute(), request.getStart());
        final IMap<TravelKey, TravelState> travelMap = dataManager.getTravelMap();
        travelMap.lock(travelKey);
        IMap<TravelKey, List<Long>> railroadCarTravelMap = dataManager.getRailroadCarTravelMap();
        railroadCarTravelMap.lock(travelKey);
        IMap<Long, RailroadCar> railroadCarMap = dataManager.getRailroadCarMap();
        final IMap<Long, List<String>> routeStationsMap = dataManager.getRouteStationsMap();
        routeStationsMap.lock(request.getRoute());
        final IMap<SeatKey, SeatState> seatStateMap = dataManager.getSeatStateMap();
        IMap<RailroadCarTravelKey, Set<SeatPlace>> seatsByRailroadCar = dataManager.getSeatsByRailroadCarMap();
        try {
            final List<Long> idsCar = Optional.ofNullable(railroadCarTravelMap.get(travelKey)).orElse(Collections.emptyList());
            List<String> list = routeStationsMap.get(travelKey.getRoute());
            for (Long idCar : idsCar) {
                railroadCarMap.lock(idCar);
                try {
                    RailroadCar railroadCar = railroadCarMap.get(idCar);
                    Map<SeatKey, SeatState> mapToUpdate = new HashMap<>();
                    Set<SeatPlace> seatPlaces = new HashSet<>();
                    for (String station : list) {
                        for (String place : railroadCar.getSeats()) {
                            SeatKey seatKey = new SeatKey(travelKey.getRoute(), travelKey.getStart(), idCar, place, station);
                            seatPlaces.add(new SeatPlace(station, place, SeatState.AVAILABLE));
                            mapToUpdate.put(seatKey, SeatState.AVAILABLE);
                        }
                    }
                    seatsByRailroadCar.set(new RailroadCarTravelKey(request.getRoute(), request.getStart(), idCar), seatPlaces);
                    seatStateMap.putAll(mapToUpdate);
                } finally {
                    railroadCarMap.unlock(idCar);
                }
            }

            dataManager.getTravelCDAO().updateState(travelKey.getStart(), travelKey.getRoute(), TravelState.SELLING);
            travelMap.set(travelKey, TravelState.SELLING);
            return "ok";
        } finally {
            routeStationsMap.unlock(travelKey.getRoute());
            travelMap.unlock(travelKey);
            railroadCarTravelMap.unlock(travelKey);
        }
    }

    @PostMapping("set-travel-maintenance")
    String setTravelMaintenance(@RequestBody TravelKey travelKey) {
        final IMap<TravelKey, TravelState> travelMap = dataManager.getTravelMap();
        if (!travelMap.replace(travelKey, TravelState.SELLING, TravelState.MAINTENANCE)) {
            return String.format("change to %s is invalid for %s", TravelState.MAINTENANCE, travelKey);
        }
        System.out.printf("passed travel key %s to maintenance%n", travelKey);
        return "ok";
    }

    @PostMapping("setup-route")
    GenericResponse<Long> setupRoute(@RequestBody SetRouteRequest request) {
        long id;
        TransactionContext tx = hz.newTransactionContext();
        try {
            tx.beginTransaction();
            TransactionalMap<String, String> stationsMap = tx.getMap(TrainMapConstants.STATIONS);
            TransactionalMap<Long, List<String>> routeStationsMap = tx.getMap(TrainMapConstants.ROUTE_STATIONS);
            for (String station : request.getStations()) {
                if (!stationsMap.containsKey(station)) {
                    tx.rollbackTransaction();
                    return new GenericResponse<>("station name doesn't exist:" + station, null);
                }
            }
            id = dataManager.getIdGenRoute().newId();
            routeStationsMap.set(id, request.getStations());
            tx.commitTransaction();
        } catch (Exception ex) {
            tx.rollbackTransaction();
            return GenericResponse.createNok("unexpected error");
        }
        dataManager.getRouteCDAO().insert(id, request.getStations(), request.getDelays(), request.getPrices());
        dataManager.getRouteDelaysMap().set(id, request.getDelays());
        dataManager.getRoutePricesMap().set(id, request.getPrices());
        return new GenericResponse<>("ok", id);
    }

    @PostMapping("ticket-payed")
    String ticketPayed(@RequestBody TicketPayedRequest request) {
        List<String> stations = dataManager.getRouteStationsMap().get(request.getRoute());
        int idxStart = stations.indexOf(request.getStartStation());
        if (idxStart == -1) {
            return String.format("start station %s invalid", request.getStartStation());
        }
        int idxEndStation = stations.indexOf(request.getEndStation());
        if (idxEndStation == -1) {
            return String.format("start station %s invalid", request.getEndStation());
        }
        if (idxStart >= idxEndStation) {
            return ("invalid stations order");
        }
        TravelKey travelKey = new TravelKey(request.getRoute(), request.getStart());
        TravelState travelState = dataManager.getTravelMap().get(travelKey);
        if (travelState != TravelState.SELLING) {
            return String.format("invalid state %s for state key %s", travelKey, travelState);
        }
        List<String> stationsTicket = new ArrayList<>(stations.subList(idxStart, idxEndStation));
        List<SeatKey> seatKeys = stationsTicket.stream().map(station ->
                new SeatKey(
                        travelKey.getRoute(),
                        travelKey.getStart(),
                        request.getRailroadCar(),
                        request.getSeat(),
                        station
                )
        ).collect(Collectors.toList());
        String result = dataManager.getSeatMultiStore().replaceAllSeatState(seatKeys, SeatState.OCCUPIED);
        if (!result.equals("ok")) {
            return result;
        }
        final IMap<TicketKey, Ticket> ticketMap = dataManager.getTicketMap();
        Ticket ticket = ticketMap.get(request.clone());
        if (ticket == null) {
            return "unexpected null ticket " + request;
        }
        int price = dataManager.getRidePrice(request.getRoute(), request.getRailroadCar(), idxStart, idxEndStation);
        ticketMap.set(request.clone(), ticket.createClone(TicketState.PAYED, price));
        return "ok";
    }

    @PostMapping("ticket-request")
    GenericResponse<Integer> ticketRequest(@RequestBody TicketRequest request) {
        List<String> stations = dataManager.getRouteStationsMap().get(request.getRoute());
        int idxStart = stations.indexOf(request.getStartStation());
        if (idxStart == -1) {
            return createNok(String.format("start station %s invalid", request.getStartStation()));
        }
        int idxEndStation = stations.indexOf(request.getEndStation());
        if (idxEndStation == -1) {
            return createNok(String.format("start station %s invalid", request.getEndStation()));
        }
        if (idxStart >= idxEndStation) {
            return createNok("invalid stations order");
        }
        TravelKey travelKey = new TravelKey(request.getRoute(), request.getStart());
        TravelState travelState = dataManager.getTravelMap().get(travelKey);
        if (travelState != TravelState.SELLING) {
            return createNok(String.format("invalid state %s for state key %s", travelKey, travelState));
        }
        List<String> stationsTicket = new ArrayList<>(stations.subList(idxStart, idxEndStation));
        List<SeatKey> seatKeys = new ArrayList<>();
        for (String station : stationsTicket) {
            SeatKey seatKey = new SeatKey(
                    travelKey.getRoute(),
                    travelKey.getStart(),
                    request.getRailroadCar(),
                    request.getSeatPlace(),
                    station
            );
            seatKeys.add(seatKey);
        }
        String result = dataManager.getSeatMultiStore().replaceAllSeatState(seatKeys, SeatState.RESERVED);
        if (!result.equals("ok")) {
            return createNok(result);
        }
        TicketKey ticketKey = new TicketKey(
                travelKey.getRoute(),
                travelKey.getStart(),
                request.getRailroadCar(),
                request.getStartStation(),
                request.getSeatPlace()
        );
        Ticket ticket = new Ticket(request.getUserId(), TicketState.WAITING_PAYMENT, request.getEndStation(), 0);
        dataManager.getTicketMap().set(ticketKey, ticket);
        System.out.println("schedule the autoDisposable TicketChecker at:" + System.currentTimeMillis());
        Callable<Void> ticketChecker = TaskUtils.autoDisposable(new TicketChecker(ticketKey, seatKeys));
        hz.getScheduledExecutorService(TrainMapConstants.TICKET)
                .scheduleOnKeyOwner(ticketChecker, ticketKey.getPartitionKey(), 3, TimeUnit.SECONDS);

        return createOk(dataManager.getRidePrice(request.getRoute(), request.getRailroadCar(), idxStart, idxEndStation));
    }

    @GetMapping("travel-available-railroad-car-seats")
    GenericResponse<Map<Integer, List<Long>>> getTravelAvailableRailroadCarSeats(
            @RequestParam(name = "station") String station,
            @RequestParam(name = "route") Long route,
            @RequestParam(name = "start") Long start
    ) throws ExecutionException, InterruptedException {
        IMap<RailroadCarTravelKey, Set<SeatPlace>> seatsByRailroadCarMap = dataManager.getSeatsByRailroadCarMap();
        final IMap<TravelKey, List<Long>> railroadCarTravelMap = dataManager.getRailroadCarTravelMap();
        final TravelKey key = new TravelKey(route, Instant.ofEpochMilli(start));
        List<Long> railroadCars = railroadCarTravelMap.get(key);

        List<Pair<Long,Future<Integer>>> countSeatsFuture = new ArrayList<>();
        for (Long railroadCar : railroadCars) {
            final RailroadCarTravelKey railroadCarTravelKey = key.createRailroadCarTravelKey(railroadCar);
            Future<Integer> result = hz.getExecutorService("default")
                    .submitToKeyOwner(
                            new CallableCountSeatsAvailable(railroadCarTravelKey, station),
                            railroadCarTravelKey.hashCode()
                    );
            countSeatsFuture.add(Pair.of(railroadCar,result));
        }

        Map<Integer,List<Long>> countSeats = new HashMap<>();
        for(Pair<Long,Future<Integer>> future: countSeatsFuture){
            Integer count = future.getValue().get();
            countSeats.computeIfAbsent(count,k-> new ArrayList<>()).add(future.getKey());
        }

        System.out.println("count seats:"+countSeats);
        return createOk(countSeats);
    }
}