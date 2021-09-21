package com.capgemini.cdao.train;

import com.capgemini.entity.train.RailroadCar;
import com.capgemini.entity.train.TravelKey;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;

import java.util.*;
import java.util.stream.StreamSupport;


public class RailroadCarCDAO {
    public static final String SEAT_PLACES = "seat_places";
    public static final String TRAVEL_TYPE = "travel_type";
    public static final String TRAVEL = "travel";
    private final CqlSession session;
    private final PreparedStatement select;
    private final PreparedStatement insert;
    private final PreparedStatement delete;
    private final PreparedStatement updateTravel;

    public RailroadCarCDAO(CqlSession session) {
        this.session = session;
        insert = session.prepare("insert into railroad_car (id, travel_type, seat_places) values (?,?,?)");
        updateTravel = session.prepare("UPDATE railroad_car SET travel=(?, ?) WHERE id = ?;");
        select = session.prepare("select travel_type, seat_places, travel from railroad_car where id = ?");
        delete = session.prepare("delete from railroad_car where id = ?");
    }

    private static RailroadCar toRailroadCar(Row row) {
        return new RailroadCar(
                Optional.ofNullable(row.getTupleValue(TRAVEL))
                        .map(tupple -> new TravelKey(tupple.getLong(0), tupple.getInstant(1)))
                        .orElse(null),
                row.getString(TRAVEL_TYPE),
                row.getSet(SEAT_PLACES, String.class)
        );
    }

    public Optional<RailroadCar> getRailroadCar(long id) {
        final ResultSet rs = session.execute(select.bind(id));
        return StreamSupport.stream(rs.spliterator(), false)
                .map(RailroadCarCDAO::toRailroadCar)
                .findAny();
    }

    public void insert(long id, String travelType, Set<String> seats) {
        session.execute(insert.bind(id, travelType, seats));
    }

    public void updateTravels(Iterable<Long> railroadCars, TravelKey travelKey) {
        BatchStatementBuilder batchBuilder = new BatchStatementBuilder(BatchType.UNLOGGED);
        for (Long id : railroadCars) {
            batchBuilder.addStatement(updateTravel.bind(
                            travelKey.getRoute(),
                            travelKey.getStart(),
                            id
                    )
            );
        }
        session.execute(batchBuilder.build());
    }

    public void delete(long id) {
        session.execute(delete.bind(id));
    }


}
