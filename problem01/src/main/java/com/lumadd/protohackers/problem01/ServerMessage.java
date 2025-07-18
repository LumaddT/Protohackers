package com.lumadd.protohackers.problem01;

import com.fasterxml.jackson.annotation.JsonProperty;

class ServerMessage {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @JsonProperty("method")
    private final String Method;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @JsonProperty("prime")
    private final boolean Prime;

    public ServerMessage(String method, boolean prime) {
        Method = method;
        Prime = prime;
    }
}
