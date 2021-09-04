package com.capgemini.cdao.bank;

import com.capgemini.dto.bank.client.Address;
import com.capgemini.dto.bank.client.Client;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;

import java.time.LocalDate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class ClientCDAO {
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String ADDRESS = "address";
    public static final String STREET = "street";
    public static final String POSTAL_CODE = "postal_code";
    public static final String DOOR = "door";
    public static final String BIRTH_DATE = "birth_date";
    public static final String CLIENT_ID = "client_id";
    private final CqlSession session;
    private final PreparedStatement selectFromName;
    private final PreparedStatement insert;
    private final PreparedStatement selectAll;
    private final PreparedStatement addAccountPermission;
    private final PreparedStatement deleteAccountPermission;
    private final PreparedStatement selectAccountIdsByClientId;

    public ClientCDAO(CqlSession session) {
        this.session = session;
        String columns = "id, name, birth_date, address";
        insert = session.prepare(String.format("insert into client (%s) values (?,?,?,?)", columns));
        selectFromName = session.prepare(String.format("select %s from client", columns));
        selectAll = session.prepare(String.format("select %s from client", columns));
        addAccountPermission = session.prepare("insert into idx_client_account (client_id , account) VALUES (?,?)");
        deleteAccountPermission = session.prepare("delete from idx_client_account where client_id = ? and account = ?");
        selectAccountIdsByClientId = session.prepare("select account from idx_client_account where client_id = ?");
    }

    private static Client toClient(Row row) {
        String name = row.getString(NAME);
        int id = row.getInt(ID);
        LocalDate birthDate = row.getLocalDate(BIRTH_DATE);
        UdtValue udt = row.getUdtValue(ADDRESS);
        Address address;
        if (udt != null) {
            String street = udt.getString(STREET);
            String door = udt.getString(DOOR);
            int postalCode = udt.getInt(POSTAL_CODE);
            address = new Address(postalCode, street, door);
        } else {
            address = null;
        }

        return new Client(id, name, birthDate, address);
    }

    public Stream<Client> getClientByName(String name) {
        final ResultSet rs = session.execute(selectFromName.bind(name));
        return StreamSupport.stream(rs.spliterator(), false).map(ClientCDAO::toClient);
    }

    public void create(Client client) {
        final Address address = client.getAddress();
        final UdtValue udtAddress;
        if (address != null) {
            final CqlIdentifier cqlIdentifier = session.getKeyspace().orElse(null);
            assert cqlIdentifier != null;
            UserDefinedType type = (UserDefinedType) insert.getVariableDefinitions().get("address").getType();
            udtAddress = type.newValue()
                    .setString(STREET, address.getStreet())
                    .setInt(POSTAL_CODE, address.getPostalCode())
                    .setString(DOOR, address.getDoor());
        } else {
            udtAddress = null;
        }
        session.execute(
                insert.bind(client.getId(), client.getName(), client.getBirthDate(), udtAddress)
        );
    }

    public void addAccountPermission(int clientId, int accountId) {
        session.execute(addAccountPermission.bind(clientId, accountId));
    }

    public void deleteAccountPermission(int clientId, int accountId) {
        session.execute(deleteAccountPermission.bind(clientId, accountId));
    }

    public Stream<Client> getAll() {
        ResultSet rs = session.execute(selectAll.bind());
        return StreamSupport.stream(rs.spliterator(), false).map(ClientCDAO::toClient);
    }

    public Stream<Integer> getClientAccountsById(int clientId) {
        ResultSet rs = session.execute(selectAccountIdsByClientId.bind(clientId));
        return StreamSupport.stream(rs.spliterator(), false)
                .map(row -> row.getInt("account"));
    }
}
