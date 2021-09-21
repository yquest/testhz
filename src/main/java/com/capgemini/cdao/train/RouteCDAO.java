package com.capgemini.cdao.train;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;

import java.util.List;
import java.util.Optional;


public class RouteCDAO {
    public static final String STATIONS = "stations";
    public static final String ID = "id";
    private static final String PRICES = "prices";
    private final CqlSession session;
    private final PreparedStatement insert;
    private final PreparedStatement delete;
    private final PreparedStatement selectStations;
    private final PreparedStatement selectPrices;
    private final PreparedStatement selectDelays;

    public RouteCDAO(CqlSession session) {
        this.session = session;

        insert = session.prepare("insert into route (id, stations, delays, prices) values (?,?,?,?)");
        selectStations = session.prepare("select stations from route where id = ?");
        selectPrices = session.prepare("select prices from route where id = ?");
        selectDelays = session.prepare("select delays from route where id = ?");
        delete = session.prepare("delete from route where id = ?");
    }

    public Optional<List<String>> getStations(long id) {
        final ResultSet rs = session.execute(selectStations.bind(id));

        return Optional.ofNullable(rs.one())
                .map(row -> row.getList(STATIONS, String.class));
    }

    public Optional<List<Integer>> getPrices(long id) {
        final ResultSet rs = session.execute(selectPrices.bind(id));

        return Optional.ofNullable(rs.one())
                .map(row -> row.getList(PRICES, Integer.class));
    }

    public Optional<List<Long>> getDelays(long id) {
        final ResultSet rs = session.execute(selectDelays.bind(id));

        return Optional.ofNullable(rs.one())
                .map(row -> row.getList("delays", Long.class));
    }

    public void insert(long id, List<String> stations, List<Integer> delays, List<Integer> prices) {
        session.execute(insert.bind(id, stations, delays, prices));
    }

    public void delete(long id) {
        session.execute(delete.bind(id));
    }


}
