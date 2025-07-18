package com.lumadd.protohackers.problem07.transportLayer.messages;

public interface Message {
    int getSessionId();

    MessageTypes getMessageType();
}
