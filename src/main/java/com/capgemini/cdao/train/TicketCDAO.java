package com.capgemini.cdao.train;

import com.capgemini.entity.train.Ticket;
import com.capgemini.entity.train.TicketKey;
import com.capgemini.entity.train.TicketState;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class TicketCDAO {
    private final CqlSession session;
    private final PreparedStatement select;
    private final PreparedStatement insert;
    private final PreparedStatement delete;

    public TicketCDAO(CqlSession session) {
        this.session = session;
        insert = session.prepare("insert into ticket (route, start, railroad_car, seat, start_station, end_station, paid, price, user) values (?,?,?,?,?,?,?,?,?)");
        select = session.prepare("select route, start, user, start_station, end_station, paid, price from ticket where route = ? and start = ? and railroad_car = ? and start_station = ? and seat = ?");
        delete = session.prepare("delete from ticket where route = ? and start = ? and railroad_car = ? and start_station = ? and seat = ?");
    }

    private static Map.Entry<TicketKey, Ticket> toEntry(Row row) {
        TicketKey key = new TicketKey(row.getLong("route"), row.getInstant("start"), row.getLong("railroad_car"), row.getString("station"), row.getString("seat"));
        Ticket ticket = new Ticket(row.getLong("user"), row.getBoolean("paid") ? TicketState.PAYED : TicketState.EXPIRED, row.getString("end_station"), row.getInt("price"));
        return Pair.of(key, ticket);
    }

    private static Ticket toTicket(Row row) {
        return new Ticket(row.getLong("user"), row.getBoolean("paid") ? TicketState.PAYED : TicketState.EXPIRED, row.getString("end_station"), row.getInt("price"));
    }

    public Optional<Ticket> find(TicketKey ticketKey) {
        return Optional.ofNullable(session.execute(select.bind(ticketKey.getRoute(), ticketKey.getStart(), ticketKey.getRailroadCar(), ticketKey.getStartStation(), ticketKey.getSeat())).one())
                .map(TicketCDAO::toTicket);
    }

    public void insert(TicketKey ticketKey, Ticket ticket) {
        //insert into ticket (route, start,railroad_car, seat, start_station, end_station, paid, price, user) values (?,?,?,?,?,?,?,?,?)
        session.execute(insert.bind(
                ticketKey.getRoute(), //route
                ticketKey.getStart(), //start
                ticketKey.getRailroadCar(), //railroad_car
                ticketKey.getSeat(), //seat
                ticketKey.getStartStation(), //start_station
                ticket.getEndStation(), //end_station
                ticket.getState() == TicketState.PAYED, //paid
                ticket.getPrice(), //price
                ticket.getUserId() //user
        ));
    }

    public void insert(Map<TicketKey, Ticket> map) {
        //insert into ticket (route, start, railroad_car, seat, start_station, end_station, paid, price, user) values (?,?,?,?,?,?,?,?,?)
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        for (Map.Entry<TicketKey, Ticket> entry : map.entrySet()) {
            TicketKey ticketKey = entry.getKey();
            Ticket ticket = entry.getValue();
            builder.addStatement(insert.bind(
                    ticketKey.getRoute(), //route
                    ticketKey.getStart(), //start
                    ticketKey.getRailroadCar(), //railroad_car
                    ticketKey.getSeat(), //seat
                    ticketKey.getStartStation(), //start_station
                    ticket.getEndStation(), //end_station
                    ticket.getState() == TicketState.PAYED, //paid
                    ticket.getPrice(), //price
                    ticket.getUserId() //user
            ));
        }

        session.execute(builder.build());
    }

    public void delete(TicketKey ticketKey) {
        //delete from user where select route, start, user, start_station, end_station, paid, price from ticket where
        session.execute(delete.bind(
                ticketKey.getRoute(),// route = ?
                ticketKey.getStart(),// and start = ?
                ticketKey.getRailroadCar(),// and railroad_car = ?
                ticketKey.getStartStation(),// and start_station = ?
                ticketKey.getSeat()// and seat = ?
        ));
    }

    public void delete(Collection<TicketKey> ticketKeys) {
        //delete from user where select route, start, user, start_station, end_station, paid, price from ticket where
        BatchStatementBuilder builder = new BatchStatementBuilder(BatchType.UNLOGGED);
        for (TicketKey ticketKey : ticketKeys) {
            builder.addStatement(delete.bind(
                    ticketKey.getRoute(),// route = ?
                    ticketKey.getStart(),// and start = ?
                    ticketKey.getRailroadCar(),// and railroad_car = ?
                    ticketKey.getStartStation(),// and start_station = ?
                    ticketKey.getSeat()// and seat = ?
            ));
        }
    }


}
