package com.sprunck.druid.riemann;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Configuration for the riemann emitter.
 */
public class RiemannEmitterConfig {
    /**
     * Hostname or IP address of the riemann server.
     */
    @NotNull
    @JsonProperty
    private String host = null;

    /**
     * Connection port of the riemann server. (ex: 5555)
     */
    @JsonProperty
    private int port;

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }
}
