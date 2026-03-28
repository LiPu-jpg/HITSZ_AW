package edu.hitsz.protocol;

import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;

public class AudioCueProtocolTest {

    public static void main(String[] args) {
        audioCueCountersRoundTrip();
    }

    private static void audioCueCountersRoundTrip() {
        WorldSnapshot snapshot = new WorldSnapshot(33L);
        snapshot.setBulletHitAudioCount(7);
        snapshot.setSupplyPickupAudioCount(4);

        WorldSnapshot decoded = new WorldSnapshotJsonMapper().fromJson(
                new WorldSnapshotJsonMapper().toJson(snapshot)
        );

        assert decoded.getBulletHitAudioCount() == 7
                : "WorldSnapshot should serialize bullet-hit audio counters";
        assert decoded.getSupplyPickupAudioCount() == 4
                : "WorldSnapshot should serialize supply-pickup audio counters";
    }
}
