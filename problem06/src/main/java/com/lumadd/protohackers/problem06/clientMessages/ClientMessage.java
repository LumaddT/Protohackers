package com.lumadd.protohackers.problem06.clientMessages;

import com.lumadd.protohackers.problem06.MessageTypes;

public interface ClientMessage {
    MessageTypes getMessageType();
}
