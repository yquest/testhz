package com.capgemini.cdao.train;

import com.capgemini.store.train.RailroadCarTravelKey;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class RailroadCarTravelCDAO {
    public static final String RAIL_ROAD_CAR = "railroad_car";
    private final CqlSession session;
    private final PreparedStatement select;
    private final PreparedStatement insert;
    private final PreparedStatement delete;

    public RailroadCarTravelCDAO(CqlSession session) {
        this.session = session;

        insert = session.prepare("insert into railroad_car_travel (route, start, railroad_car) values (?,?,?)");
        select = session.prepare("select railroad_car from railroad_car_travel where route = ? and start = ?");
        delete = session.prepare("delete from railroad_car_travel where route = ? and start = ? and railroad_car = ?");
    }

    public Stream<Long> getRailroadCarIds(long route, Instant start) {
        final ResultSet rs = session.execute(select.bind(route, start));

        return StreamSupport.stream(rs.spliterator(), false)
                .map(row -> row.getLong(RAIL_ROAD_CAR));
    }

    public void insert(long route, Instant start, List<Long> railroadCars) {
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        for (Long railroadCar : railroadCars) {
            builder.addStatement(insert.bind(route, start, railroadCar));
        }
        session.execute(builder.build());
    }

    public void delete(RailroadCarTravelKey key) {
        //delete from railroad_car_travel where route = ? and start = ? and railroad_car = ?
        session.execute(delete.bind(key.getRoute(), key.getStart(), key.getRailroadCar()));
    }


}
