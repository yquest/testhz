package com.capgemini.cdao.train;

import com.capgemini.entity.train.TravelState;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.StreamSupport;


public class TravelCDAO {
    public static final String TYPE_OF_TRAIN = "type_of_train";
    public static final String STATE = "state";
    private final CqlSession session;
    private final PreparedStatement select;
    private final PreparedStatement insert;
    private final PreparedStatement delete;
    private final PreparedStatement updateState;

    public TravelCDAO(CqlSession session) {
        this.session = session;

        insert = session.prepare("insert into travel (route, start, type_of_train, state) values (?,?,?,?)");
        updateState = session.prepare("update travel set state = ? where route = ? and start = ?");
        select = session.prepare("select route, start, type_of_train, state from travel where route = ? and start = ?");
        delete = session.prepare("delete from travel where route = ? and start = ?");
    }

    public Optional<String> getType(long route, Instant start) {
        final ResultSet rs = session.execute(select.bind(route, start));

        return StreamSupport.stream(rs.spliterator(), false)
                .map(row -> row.getString(TYPE_OF_TRAIN))
                .findAny();
    }

    public Optional<TravelState> getState(long route, Instant start) {
        final ResultSet rs = session.execute(select.bind(route, start));
        return Optional.ofNullable(rs.one())
                .map(row -> row.getString(STATE))
                .flatMap(s ->
                        Arrays.stream(TravelState.values())
                                .filter(r -> r.name().equals(s))
                                .findAny()
                );
    }

    public void insert(long route, Instant start, String trainType) {
        session.execute(insert.bind(route, start, trainType, TravelState.PREPARING.name()));
    }

    public void delete(long route, Instant start) {
        session.execute(delete.bind(route, start));
    }


    public void updateState(Instant start, long route, TravelState state) {
        session.execute(updateState.bind(state.name(), route, start));
    }
}
