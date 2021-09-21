package com.capgemini.testhz.train;

import com.capgemini.cdao.train.*;
import com.capgemini.store.train.*;
import com.datastax.oss.driver.api.core.CqlSession;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ConfMaps implements MapResolver {
    private static final int MAX_IDLE = 3;
    private static final int WRITE_DELAY = 3;
    private static final int BACKUP_COUNT = 1;
    @Autowired
    @Qualifier("train-session")
    private CqlSession session;
    @Autowired
    private HazelcastInstance hazelcast;
    private RouteCDAO routeCDAO;
    private RailroadCarCDAO railroadCarCDAO;
    private RailroadCarTravelCDAO railroadCarTravelCDAO;
    private StationsCDAO stationsCDAO;
    private TravelCDAO travelCDAO;
    private FlakeIdGenerator idGenRoute;
    private FlakeIdGenerator idGenRailroadCar;
    private SeatStateCDAO seatStateCDAO;
    private UserCDAO userCDAO;
    private SeatMultiStore seatMultiStore;
    private TicketCDAO ticketDAO;

    @PostConstruct
    private void init() {
        routeCDAO = new RouteCDAO(session);
        railroadCarCDAO = new RailroadCarCDAO(session);
        railroadCarTravelCDAO = new RailroadCarTravelCDAO(session);
        travelCDAO = new TravelCDAO(session);
        stationsCDAO = new StationsCDAO(session);
        seatStateCDAO = new SeatStateCDAO(session);
        userCDAO = new UserCDAO(session);
        ticketDAO = new TicketCDAO(session);

        configureRouteStations();
        configureRouteDelays();
        configureRailroadCar();
        configureRailroadCarTravel();
        configureTravel();
        confStations();
        confSeatState();
        confSeatsByRailroadCar();
        configureTicket();
        configureRoutePrices();
        seatMultiStore = new SeatMultiStore(hazelcast);
    }

    private void configureRouteDelays() {
        MapConfig mapConfig = hazelcast.getConfig().getMapConfig(TrainMapConstants.ROUTE_DELAYS);
        final RouteLoaderDelays routeStore = new RouteLoaderDelays(routeCDAO);
        final MapStoreConfig mapStoreConfig = mapConfig.getMapStoreConfig();
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapStoreConfig.setImplementation(routeStore);
        mapStoreConfig.setEnabled(true);
        mapConfig.setBackupCount(BACKUP_COUNT);
        mapConfig.setMaxIdleSeconds(MAX_IDLE);
    }

    private void confStations() {
        MapConfig mapConfig = hazelcast.getConfig().getMapConfig(TrainMapConstants.STATIONS);
        final StationsLoader stationsLoader = new StationsLoader(stationsCDAO);
        final MapStoreConfig mapStoreConfig = mapConfig.getMapStoreConfig();
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapStoreConfig.setImplementation(stationsLoader);
        mapStoreConfig.setEnabled(true);
        mapConfig.setBackupCount(BACKUP_COUNT);
        mapConfig.setMaxIdleSeconds(MAX_IDLE);
    }

    private void confSeatState() {
        MapConfig mapConfig = hazelcast.getConfig().getMapConfig(TrainMapConstants.SEAT_STATE);
        final SeatStateStore seatStateStore = new SeatStateStore(seatStateCDAO);
        final MapStoreConfig mapStoreConfig = mapConfig.getMapStoreConfig();
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapStoreConfig.setImplementation(seatStateStore);
        mapStoreConfig.setEnabled(true);
        mapStoreConfig.setWriteBatchSize(100);
        mapStoreConfig.setWriteDelaySeconds(WRITE_DELAY);
        mapConfig.setBackupCount(BACKUP_COUNT);
        mapConfig.setMaxIdleSeconds(MAX_IDLE);
    }

    private void confSeatsByRailroadCar() {
        MapConfig mapConfig = hazelcast.getConfig().getMapConfig(TrainMapConstants.SEATS_BY_RAILROAD_CAR);
        final RailroadCarSeatsStatesLoader seatStateStore = new RailroadCarSeatsStatesLoader(seatStateCDAO);
        final MapStoreConfig mapStoreConfig = mapConfig.getMapStoreConfig();
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapStoreConfig.setImplementation(seatStateStore);
        mapStoreConfig.setEnabled(true);
        mapConfig.setBackupCount(BACKUP_COUNT);
        mapConfig.setMaxIdleSeconds(MAX_IDLE);
    }

    private void configureRailroadCarTravel() {
        MapConfig mapConfig = hazelcast.getConfig().getMapConfig(TrainMapConstants.RAILROAD_CAR_TRAVEL);
        final RailroadCarLoaderByTravelLoader routeStore = new RailroadCarLoaderByTravelLoader(railroadCarTravelCDAO);
        final MapStoreConfig mapStoreConfig = mapConfig.getMapStoreConfig();
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapStoreConfig.setImplementation(routeStore);
        mapStoreConfig.setEnabled(true);
        mapConfig.setBackupCount(BACKUP_COUNT);
        mapConfig.setMaxIdleSeconds(MAX_IDLE);
    }

    private void configureTravel() {
        MapConfig mapConfig = hazelcast.getConfig().getMapConfig(TrainMapConstants.TRAVEL);
        final TravelStateLoader routeStore = new TravelStateLoader(travelCDAO);
        final MapStoreConfig mapStoreConfig = mapConfig.getMapStoreConfig();
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapStoreConfig.setImplementation(routeStore);
        mapStoreConfig.setEnabled(true);
        mapConfig.setBackupCount(BACKUP_COUNT);
        mapConfig.setMaxIdleSeconds(MAX_IDLE);
    }

    private void configureRailroadCar() {
        MapConfig mapConfig = hazelcast.getConfig().getMapConfig(TrainMapConstants.RAILROAD_CAR);
        final RailroadCarLoader routeStore = new RailroadCarLoader(railroadCarCDAO);
        final MapStoreConfig mapStoreConfig = mapConfig.getMapStoreConfig();
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapStoreConfig.setImplementation(routeStore);
        mapStoreConfig.setEnabled(true);
        mapConfig.setBackupCount(BACKUP_COUNT);
        mapConfig.setMaxIdleSeconds(MAX_IDLE);
        idGenRailroadCar = hazelcast.getFlakeIdGenerator(TrainMapConstants.RAILROAD_CAR);
    }

    private void configureRoutePrices() {
        MapConfig mapConfig = hazelcast.getConfig().getMapConfig(TrainMapConstants.ROUTE_PRICES);
        final RouteLoaderPrices routeStore = new RouteLoaderPrices(routeCDAO);
        final MapStoreConfig mapStoreConfig = mapConfig.getMapStoreConfig();
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapStoreConfig.setImplementation(routeStore);
        mapStoreConfig.setEnabled(true);
        mapConfig.setBackupCount(BACKUP_COUNT);
        mapConfig.setMaxIdleSeconds(MAX_IDLE);
    }

    private void configureRouteStations() {
        MapConfig mapConfig = hazelcast.getConfig().getMapConfig(TrainMapConstants.ROUTE_STATIONS);
        final RouteLoaderStations routeStore = new RouteLoaderStations(routeCDAO);
        final MapStoreConfig mapStoreConfig = mapConfig.getMapStoreConfig();
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapStoreConfig.setImplementation(routeStore);
        mapStoreConfig.setEnabled(true);
        mapConfig.setBackupCount(BACKUP_COUNT);
        mapConfig.setMaxIdleSeconds(MAX_IDLE);
        idGenRoute = hazelcast.getFlakeIdGenerator(TrainMapConstants.ROUTE_STATIONS);
    }

    private void configureTicket() {
        MapConfig mapConfig = hazelcast.getConfig().getMapConfig(TrainMapConstants.TICKET);
        final TicketStore routeStore = new TicketStore(ticketDAO);
        final MapStoreConfig mapStoreConfig = mapConfig.getMapStoreConfig();
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.LAZY);
        mapStoreConfig.setImplementation(routeStore);
        mapStoreConfig.setEnabled(true);
        mapStoreConfig.setWriteDelaySeconds(10);
        mapConfig.setBackupCount(BACKUP_COUNT);
        mapConfig.setMaxIdleSeconds(10);
    }

    public RouteCDAO getRouteCDAO() {
        return routeCDAO;
    }

    public RailroadCarCDAO getRailroadCarCDAO() {
        return railroadCarCDAO;
    }

    public UserCDAO getUserCDAO() {
        return userCDAO;
    }

    public RailroadCarTravelCDAO getRailroadCarTravelCDAO() {
        return railroadCarTravelCDAO;
    }

    @Override
    public HazelcastInstance getHazelcast() {
        return hazelcast;
    }

    public TravelCDAO getTravelCDAO() {
        return travelCDAO;
    }

    public StationsCDAO getStationsCDAO() {
        return stationsCDAO;
    }

    public FlakeIdGenerator getIdGenRailroadCar() {
        return idGenRailroadCar;
    }

    public FlakeIdGenerator getIdGenRoute() {
        return idGenRoute;
    }

    public SeatStateCDAO getSeatStateCDAO() {
        return seatStateCDAO;
    }

    public SeatMultiStore getSeatMultiStore() {
        return seatMultiStore;
    }
}
