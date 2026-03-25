package edu.hitsz.application.client;

import edu.hitsz.application.protocol.dto.WorldSnapshot;

public interface SnapshotApplier {

    void apply(WorldSnapshot snapshot, ClientWorldState state);
}
