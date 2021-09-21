CREATE KEYSPACE bank  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

use bank;

CREATE TYPE address (
    street text,
    door text,
    postal_code int
);

CREATE TABLE client (
   id int,
   name text,
   birth_date date,
   address FROZEN<address>,
   PRIMARY KEY ((name),id)
);

CREATE TABLE account (
   id int,
   balance bigint,
   clients set<int>,
   PRIMARY KEY (id)
);

CREATE TABLE request (
   id int,
   account_number text,
   done boolean,
   amount bigint,
   creation_date timestamp,
   PRIMARY KEY ((account_number), id, done)
);

CREATE TABLE idx_client_account (
   client_id int,
   account int,
   PRIMARY KEY ((client_id),account)
);
