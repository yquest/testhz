package com.capgemini.store.train;

import com.capgemini.cdao.train.TicketCDAO;
import com.capgemini.entity.train.SeatKey;
import com.capgemini.entity.train.Ticket;
import com.capgemini.entity.train.TicketKey;
import com.hazelcast.map.MapStore;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;


public class TicketStore implements MapStore<TicketKey, Ticket> {
    private final TicketCDAO ticketCDAO;
    private final MapLoaderPrinter<TicketKey, Ticket> mapLoaderPrinter = new MapLoaderPrinter<>(this.getClass());

    public TicketStore(TicketCDAO seatStateCDAO) {
        this.ticketCDAO = seatStateCDAO;
    }

    @Override
    synchronized public Ticket load(TicketKey key) {
        mapLoaderPrinter.load(key);
        return ticketCDAO.find(key).orElse(null);
    }

    @Override
    synchronized public Map<TicketKey, Ticket> loadAll(Collection<TicketKey> collection) {
        mapLoaderPrinter.loadAll(collection);
        Map<TicketKey,Ticket> tickets = new HashMap<>();
        for(TicketKey key:collection){
            tickets.put(key,ticketCDAO.find(key).orElse(null));
        }
        return tickets;
    }

    @Override
    synchronized public Iterable<TicketKey> loadAllKeys() {
        mapLoaderPrinter.loadAllKeys();
        return Collections.emptyList();
    }

    @Override
    synchronized public void store(TicketKey key, Ticket ticket) {
        mapLoaderPrinter.store(key, ticket);
        ticketCDAO.insert(key, ticket);
    }

    @Override
    synchronized public void storeAll(Map<TicketKey, Ticket> map) {
        mapLoaderPrinter.store(map);
        ticketCDAO.insert(map);
    }

    @Override
    synchronized public void delete(TicketKey seatKey) {
        mapLoaderPrinter.deleteKey(seatKey);
        ticketCDAO.delete(seatKey);
    }

    @Override
    synchronized public void deleteAll(Collection<TicketKey> collection) {
        mapLoaderPrinter.deleteKeys(collection);
        ticketCDAO.delete(collection);
    }
}
