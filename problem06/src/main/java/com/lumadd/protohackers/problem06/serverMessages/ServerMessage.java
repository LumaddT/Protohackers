package com.lumadd.protohackers.problem06.serverMessages;

import com.lumadd.protohackers.problem06.MessageTypes;
import com.lumadd.protohackers.problem06.exceptions.ImpossibleEncodingException;

public interface ServerMessage {
    MessageTypes getMessageType();

    byte[] encode() throws ImpossibleEncodingException;
}
