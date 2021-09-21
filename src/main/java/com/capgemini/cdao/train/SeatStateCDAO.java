package com.capgemini.cdao.train;

import com.capgemini.entity.train.SeatKey;
import com.capgemini.entity.train.SeatPlace;
import com.capgemini.store.train.RailroadCarTravelKey;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class SeatStateCDAO {
    private final CqlSession session;
    private final PreparedStatement select;
    private final PreparedStatement insert;
    private final PreparedStatement deleteByPartition;
    private final PreparedStatement insertOrUpdateState;
    private final PreparedStatement selectSeatState;
    private final PreparedStatement delete;

    public SeatStateCDAO(CqlSession session) {
        this.session = session;

        //when the seat state is created there is no ticket associated
        insert = session.prepare("insert into seat_state(route, start, railroad_car, seat_place, station, state) values (?,?,?,?,?,?)");
        delete = session.prepare("delete from seat_state where start = ? and route = ? and railroad_car = ? and seat_place = ? and station = ?");
        //select by pk
        selectSeatState = session.prepare("SELECT state from seat_state WHERE start = ? and route = ? and railroad_car = ? and seat_place = ? and station = ?");
        //select all seats in train
        select = session.prepare("select route, start, station, seat_place, state from seat_state where route = ? and start = ? and railroad_car = ?");
        deleteByPartition = session.prepare("delete from railroad_car_travel where route = ? and start = ? and railroad_car = ?");
        insertOrUpdateState = session.prepare("insert into seat_state (start, route, railroad_car, seat_place, station, state) values (?,?,?,?,?,?)");
    }

    public Stream<SeatPlace> getSeatPlaces(RailroadCarTravelKey seatKey) {
        //select route, start from seat_state where route = ? and start = ? and railroad_car = ?
        final ResultSet rs = session.execute(select.bind(seatKey.getRoute(), seatKey.getStart(), seatKey.getRailroadCar()));

        return StreamSupport.stream(rs.spliterator(), false).map(this::toSeatPlace);
    }

    private SeatPlace toSeatPlace(Row row) {
        return new SeatPlace(
                row.getString("station"),
                row.getString("seat_place"),
                SeatState.valueOfOrNull(row.getString("state"))
        );
    }


    public void updateState(SeatKey seatKey, SeatState seatState) {
        //insert into seat_state (start, route, railroad_car, seat_place, station, state) values (?,?,?,?,?,?)
        session.execute(insertOrUpdateState.bind(
                seatKey.getStart(),
                seatKey.getRoute(),
                seatKey.getRailroadCar(),
                seatKey.getSeatPlace(),
                seatKey.getStation(),
                seatState.name()
        ));
    }

    public void updateStates(Map<SeatKey, SeatState> map) {
        //insert into seat_state (start, route, railroad_car, seat_place, station, state) values (?,?,?,?,?,?)
        BatchStatementBuilder batchBuilder = new BatchStatementBuilder(BatchType.UNLOGGED);
        for (Map.Entry<SeatKey, SeatState> entry : map.entrySet()) {
            batchBuilder.addStatement(insertOrUpdateState.bind(
                    entry.getKey().getStart(),
                    entry.getKey().getRoute(),
                    entry.getKey().getRailroadCar(),
                    entry.getKey().getSeatPlace(),
                    entry.getKey().getStation(),
                    entry.getValue().name()
            ));
        }
        session.execute(batchBuilder.build());
        System.out.println("entry map" + map);
    }


    public Optional<SeatState> getSeatState(SeatKey seatKey) {
        ResultSet rs = session.execute(selectSeatState.bind(
                        seatKey.getStart(),
                        seatKey.getRoute(),
                        seatKey.getRailroadCar(),
                        seatKey.getSeatPlace(),
                        seatKey.getStation()
                )
        );
        return Optional.ofNullable(rs.one()).map(row -> SeatState.valueOfOrNull(row.getString("state")));
    }

    public void delete(SeatKey key) {
        session.execute(delete.bind(key.getStart(), key.getRoute(), key.getRailroadCar(), key.getSeatPlace(), key.getStation()));
    }

    public void delete(Collection<SeatKey> collection) {
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        for (SeatKey seatKey : collection) {
            builder.addStatement(delete.bind(
                    seatKey.getStart(),
                    seatKey.getRoute(),
                    seatKey.getRailroadCar(),
                    seatKey.getSeatPlace(),
                    seatKey.getStation()
            ));
        }
        session.execute(builder.build());
    }
}
