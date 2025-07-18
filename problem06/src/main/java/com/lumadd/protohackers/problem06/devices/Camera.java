package com.lumadd.protohackers.problem06.devices;

import lombok.Getter;
import com.lumadd.protohackers.problem06.IslandManager;
import com.lumadd.protohackers.problem06.MessageTypes;
import com.lumadd.protohackers.problem06.SocketHolder;
import com.lumadd.protohackers.problem06.clientMessages.ClientMessage;
import com.lumadd.protohackers.problem06.clientMessages.IAmCamera;
import com.lumadd.protohackers.problem06.clientMessages.Plate;
import com.lumadd.protohackers.problem06.exceptions.SocketIsDeadException;
import com.lumadd.protohackers.problem06.serverMessages.Error;

@Getter
public class Camera {
    private final SocketHolder SocketHolder;
    private final int Road;
    private final int Mile;
    private final int Limit;

    public Camera(SocketHolder socketHolder) {
        if (socketHolder.getInitialMessage().getMessageType() != MessageTypes.I_AM_CAMERA) {
            throw new RuntimeException("SocketHolder with initial message type %s sent to Camera constructor. This should never happen."
                    .formatted(socketHolder.getInitialMessage().getMessageType().name()));
        }

        IAmCamera initialMessage = (IAmCamera) socketHolder.getInitialMessage();

        SocketHolder = socketHolder;
        Road = initialMessage.getRoad();
        Mile = initialMessage.getMile();
        Limit = initialMessage.getLimit();
    }

    public void run() {
        while (true) {
            ClientMessage clientMessage;
            try {
                clientMessage = SocketHolder.getNextClientMessage();
            } catch (SocketIsDeadException e) {
                this.disconnect();
                return;
            }

            Plate plate = (Plate) clientMessage;

            IslandManager.reportPlate(plate, Road, Mile);
        }
    }

    public void sendError(Error.ErrorTypes errorType) {
        SocketHolder.sendError(errorType);
    }

    public void disconnect() {
        SocketHolder.close();
        IslandManager.remove(this);
    }
}
