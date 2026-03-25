package edu.hitsz.application.protocol.dto;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class WorldSnapshot {

    private final long tick;
    private final List<PlayerSnapshot> playerSnapshots;

    public WorldSnapshot(long tick) {
        this.tick = tick;
        this.playerSnapshots = new LinkedList<>();
    }

    public long getTick() {
        return tick;
    }

    public void addPlayerSnapshot(PlayerSnapshot playerSnapshot) {
        playerSnapshots.add(playerSnapshot);
    }

    public List<PlayerSnapshot> getPlayerSnapshots() {
        return Collections.unmodifiableList(playerSnapshots);
    }
}
