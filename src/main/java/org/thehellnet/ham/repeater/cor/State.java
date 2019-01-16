package org.thehellnet.ham.repeater.cor;

public enum State {
    OFF("Off"),
    INIT("Initialization"),
    WAITING("Waiting"),
    RELAY_LOCAL("Relaying local signal"),
    RELAY_REMOTE("Relaying remote signal"),
    BEACON("Sendind Beacon"),
    DEINIT("Deinitialization");

    private final String description;

    State(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
