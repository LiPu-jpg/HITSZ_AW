package edu.hitsz.common.protocol.dto;

public class ReadyPayload {

    private final boolean ready;

    public ReadyPayload(boolean ready) {
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }
}
