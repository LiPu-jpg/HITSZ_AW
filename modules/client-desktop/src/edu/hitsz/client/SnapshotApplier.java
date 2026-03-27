package edu.hitsz.client;

import edu.hitsz.common.protocol.dto.WorldSnapshot;

public interface SnapshotApplier {

    void apply(WorldSnapshot snapshot, ClientWorldState state, String localSessionId);
}
