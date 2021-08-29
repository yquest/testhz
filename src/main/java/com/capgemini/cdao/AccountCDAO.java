package com.capgemini.cdao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;


public class AccountCDAO {
    public static final String ID = "id";
    public static final String BALANCE = "balance";
    public static final String CLIENTS = "clients";
    private final CqlSession session;
    private final PreparedStatement selectBalanceId;
    private final PreparedStatement insert;
    private final PreparedStatement updateBalance;
    private final PreparedStatement addClients;
    private final PreparedStatement removeClients;
    private final PreparedStatement getClients;

    public AccountCDAO(CqlSession session) {
        this.session = session;

        List<String> columns = Arrays.asList(ID, BALANCE, CLIENTS);
        String columnsStr = String.join(", ", columns);
        String qm = IntStream.range(0, columns.size()).mapToObj(i -> "?")
                .collect(Collectors.joining(","));
        insert = session.prepare(String.format("insert into account (%s) values (%s)", columnsStr, qm));
        selectBalanceId = session.prepare("select balance from account where id = ?");
        updateBalance = session.prepare("update account set balance = ? where id = ?");
        addClients = session.prepare("update account set clients = clients + ? where id = ?");
        getClients = session.prepare("select clients from account where id = ?");
        removeClients = session.prepare("update account set clients = clients - ? where id = ?");
    }


    public Optional<Long> getBalance(Integer id) {
        final ResultSet rs = session.execute(selectBalanceId.bind(id));

        return StreamSupport.stream(rs.spliterator(), false)
                .map(row -> row.getLong(BALANCE))
                .findAny();
    }

    public void updateBalanceAccount(Integer id, long balance) {
        session.execute(updateBalance.bind(balance, id));
    }

    public void addClients(Integer accountId, Set<Integer> clientIds) {
        session.execute(addClients.bind(clientIds, accountId));
    }

    public void addClient(Integer accountId, Integer clientId) {
        session.execute(addClients.bind(Collections.singleton(clientId), accountId));
    }

    public void removeClients(Integer accountId, Set<Integer> clientIds) {
        session.execute(removeClients.bind(clientIds, accountId));
    }

    public void removeClient(Integer accountId, Integer clientId) {
        session.execute(removeClients.bind(Collections.singleton(clientId), accountId));
    }

    public Integer create(Integer id, Set<Integer> clientIds, long amount) {
        session.execute(
                insert.bind(id, amount, clientIds)
        );
        return id;
    }

    public Set<Integer> getClientIds(int id) {
        Row row = session.execute(getClients.bind(id)).one();
        return Optional.ofNullable(row).map(e -> e.getSet("clients", Integer.class))
                .orElse(Collections.emptySet());
    }

}
