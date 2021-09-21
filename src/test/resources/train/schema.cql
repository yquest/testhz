--change travel, seat_state and ticket
create keyspace train  with replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

use train;

CREATE TYPE address (
    street text,
    door text,
    postal_code int
);

create table user (
   id bigint primary key,
   name text,
   birth_date date,
   address frozen<address>
);

create table route(
    id bigint primary key,
    stations list<text>,
    delays list<int>,
    prices list<int>
);

create table station(
    name text primary key,
    label text
);

create table travel(
    route bigint,
    start timestamp,
    type_of_train text, -- regional, intercity, alpha
    state text, -- selling, preparing, maintenance
    primary key ((route), start)
);

create table railroad_car_travel(
    route bigint,
    start timestamp,
    railroad_car bigint,
    primary key((route, start),railroad_car)
);

create table railroad_car(
    id bigint primary key,
    travel_type text,
    travel tuple<bigint, timestamp>,
    seat_places set<text>
);

create table seat_state(
    route bigint,
    start timestamp,
    railroad_car bigint,
    seat_place text,
    station text,
    state text, -- occupied, free, reserved
    primary key((start, route, railroad_car), seat_place, station)
);

create table ticket(
    route bigint,
    start timestamp,
    railroad_car bigint,
    start_station text,
    seat text,
    end_station text,
    user bigint,
    price int,
    paid boolean,
    primary key((route, start), railroad_car, seat, start_station)
);