package com.lumadd.protohackers.problem07.transportLayer.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageTypes {
    CONNECT("connect"),
    DATA("data"),
    ACK("ack"),
    CLOSE("close");

    private final String Identifier;
}
