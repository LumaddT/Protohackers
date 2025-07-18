package com.lumadd.protohackers.problem06.devices;

import lombok.Getter;
import com.lumadd.protohackers.problem06.IslandManager;
import com.lumadd.protohackers.problem06.MessageTypes;
import com.lumadd.protohackers.problem06.SocketHolder;
import com.lumadd.protohackers.problem06.clientMessages.IAmDispatcher;
import com.lumadd.protohackers.problem06.exceptions.SocketIsDeadException;
import com.lumadd.protohackers.problem06.serverMessages.Ticket;

import java.util.Collections;
import java.util.Set;

@Getter
public class Dispatcher {
    private final SocketHolder SocketHolder;
    private final Set<Integer> Roads;

    public Dispatcher(SocketHolder socketHolder) {
        if (socketHolder.getInitialMessage().getMessageType() != MessageTypes.I_AM_DISPATCHER) {
            throw new RuntimeException("SocketHolder with initial message type %s sent to Dispatcher constructor. This should never happen."
                    .formatted(socketHolder.getInitialMessage().getMessageType().name()));
        }

        IAmDispatcher initialMessage = (IAmDispatcher) socketHolder.getInitialMessage();

        SocketHolder = socketHolder;
        Roads = Collections.unmodifiableSet(initialMessage.getRoads());
    }

    public void sendTicket(Ticket ticket) {
        try {
            SocketHolder.sendMessage(ticket);
        } catch (SocketIsDeadException e) {
            this.disconnect();
        }
    }

    public void disconnect() {
        SocketHolder.close();
        IslandManager.remove(this);
    }
}
