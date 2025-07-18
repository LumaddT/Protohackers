package com.lumadd.protohackers.problem06.clientMessages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import com.lumadd.protohackers.problem06.MessageTypes;

import java.util.Set;

@Getter
@RequiredArgsConstructor
@ToString
public class IAmDispatcher implements ClientMessage {
    private final MessageTypes MessageType = MessageTypes.I_AM_DISPATCHER;

    private final Set<Integer> Roads;
}
