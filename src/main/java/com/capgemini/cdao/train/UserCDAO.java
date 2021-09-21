package com.capgemini.cdao.train;

import com.capgemini.dto.Address;
import com.capgemini.entity.train.User;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Optional;

public class UserCDAO {
    public static final String TYPE_OF_TRAIN = "type_of_train";
    public static final String STATE = "state";
    public static final String BIRTH_DATE = "birth_date";
    public static final String ID = "id";
    public static final String NAME = "name";
    private static final String POSTAL_CODE = "postal_code";
    private static final String DOOR = "door";
    private static final String STREET = "street";
    private final CqlSession session;
    private final PreparedStatement select;
    private final PreparedStatement insert;
    private final PreparedStatement delete;

    public UserCDAO(CqlSession session) {
        this.session = session;

        insert = session.prepare("insert into user (id, name, birth_date, address) values (?,?,?,?)");
        select = session.prepare("select id, name, birth_date, address from user where id = ?");
        delete = session.prepare("delete from user where id=?");
    }

    private static User toUser(Row row) {
        UdtValue udtAddress = row.getUdtValue("address");
        final Optional<UdtValue> opUdtAddress = Optional.ofNullable(udtAddress);
        Address address = new Address(
                opUdtAddress.map(e -> e.getInt(POSTAL_CODE)).orElse(-1),
                opUdtAddress.map(e -> e.getString(STREET)).orElse(null),
                opUdtAddress.map(e -> e.getString(DOOR)).orElse(null)
        );
        final LocalDate birthDate = row.getLocalDate(BIRTH_DATE);
        return new User(address, row.getLong(ID), birthDate, row.getString(NAME));
    }

    public Optional<User> find(@Nonnull Long id) {
        return Optional.ofNullable(session.execute(select.bind(id)).one())
                .map(UserCDAO::toUser);
    }

    public void insert(long id, String name, LocalDate birthDate, Address address) {
        final UdtValue udtAddress;
        UserDefinedType type = (UserDefinedType) insert.getVariableDefinitions().get("address").getType();
        udtAddress = type.newValue()
                .setString(STREET, address.getStreet())
                .setInt(POSTAL_CODE, address.getPostalCode())
                .setString(DOOR, address.getDoor());

        session.execute(insert.bind(id, name, birthDate, udtAddress));
    }

    public void delete(long id) {
        session.execute(delete.bind(id));
    }


}
