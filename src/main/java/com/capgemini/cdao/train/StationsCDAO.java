package com.capgemini.cdao.train;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


public class StationsCDAO {
    public static final String NAME = "name";
    private final CqlSession session;
    private final PreparedStatement select;
    private final PreparedStatement insert;
    private final PreparedStatement delete;

    public StationsCDAO(CqlSession session) {
        this.session = session;

        insert = session.prepare("insert into station(name, label) values (?,?)");
        select = session.prepare("select name, label from station where name = ?");
        delete = session.prepare("delete from station where name = ?");
    }

    public Optional<String> getLabel(String name) {
        final ResultSet rs = session.execute(select.bind(name));
        return Optional.ofNullable(rs.one()).map(r -> r.getString(NAME));
    }

    public void insert(String name, String label) {
        session.execute(insert.bind(name,label));
    }

    public void delete(String name) {
        session.execute(delete.bind(name));
    }


}
