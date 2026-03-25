package edu.hitsz.application.client;

import edu.hitsz.aircraft.AbstractAircraft;

import java.util.LinkedList;
import java.util.List;

public class ClientWorldState {

    private final List<AbstractAircraft> playerAircrafts;

    public ClientWorldState() {
        this.playerAircrafts = new LinkedList<>();
    }

    public List<AbstractAircraft> getPlayerAircrafts() {
        return playerAircrafts;
    }
}
