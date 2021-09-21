package com.capgemini.mdao.train;

import com.capgemini.cdao.train.SeatState;
import com.capgemini.entity.train.SeatKey;
import com.capgemini.entity.train.Ticket;
import com.capgemini.entity.train.TicketKey;
import com.capgemini.entity.train.TicketState;
import com.capgemini.testhz.train.MapResolver;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;

import java.io.Serializable;
import java.util.List;

public class TicketChecker implements Serializable, Runnable, HazelcastInstanceAware {
    private static final long serialVersionUID = 3617056301722431558L;
    private final TicketKey ticketKey;
    private final List<SeatKey> seatKeys;
    transient private MapResolver mapResolver;

    public TicketChecker(TicketKey ticketKey, List<SeatKey> seatKeys) {
        this.ticketKey = ticketKey;
        this.seatKeys = seatKeys;
    }

    @Override
    public void run() {
        System.out.println("calling the TicketChecker at:"+System.currentTimeMillis());
        final IMap<TicketKey, Ticket> ticketMap = mapResolver.getTicketMap();
        Ticket ticket = ticketMap.get(ticketKey);

        try {
            ticketMap.lock(ticketKey);
            System.out.println("triggered schedule "+ticketKey);
            if(ticket.getState() != TicketState.WAITING_PAYMENT){
                System.out.println("no waiting payment:"+ticket);
                return;
            }
            String result = mapResolver.replaceAllSeatState(seatKeys, SeatState.AVAILABLE);
            System.out.println("replace scheduled "+result);
            if (result.equals("ok")) {
                ticketMap.set(ticketKey, ticket.createClone(TicketState.EXPIRED,0));
            }
        } finally {
            ticketMap.unlock(ticketKey);
        }

    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcast) {
        mapResolver = () -> hazelcast;
    }
}
