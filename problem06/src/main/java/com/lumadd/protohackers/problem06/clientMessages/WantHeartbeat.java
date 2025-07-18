package com.lumadd.protohackers.problem06.clientMessages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import com.lumadd.protohackers.problem06.MessageTypes;

@Getter
@RequiredArgsConstructor
@ToString
public class WantHeartbeat implements ClientMessage {
    private final MessageTypes MessageType = MessageTypes.WANT_HEARTBEAT;

    private final long Interval;
}
