package edu.hitsz.client;

import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;

public class StatusBarSnapshotStateTest {

    public static void main(String[] args) throws Exception {
        ClientWorldState state = new ClientWorldState();
        WorldSnapshot snapshot = new WorldSnapshot(7L);
        snapshot.addPlayerSnapshot(playerSnapshotWithHudState(
                "session-local",
                "player-local",
                200,
                300,
                850,
                40,
                false,
                3,
                "FREEZE",
                2500L,
                6000L,
                1200
        ));

        WorldSnapshotJsonMapper mapper = new WorldSnapshotJsonMapper();
        new DefaultSnapshotApplier().apply(mapper.fromJson(mapper.toJson(snapshot)), state, "session-local");

        assert localMaxHp(state) == 1200
                : "Client world state should keep local max hp for the HP status bar";
        assert localCooldownTotal(state) == 6000L
                : "Client world state should keep total skill cooldown for the cooldown bar";
    }

    private static PlayerSnapshot playerSnapshotWithHudState(
            String sessionId,
            String playerId,
            int x,
            int y,
            int hp,
            int score,
            boolean ready,
            int level,
            String selectedSkill,
            long cooldownRemainingMillis,
            long cooldownTotalMillis,
            int maxHp
    ) throws Exception {
        Constructor<PlayerSnapshot> constructor = PlayerSnapshot.class.getConstructor(
                String.class,
                String.class,
                int.class,
                int.class,
                int.class,
                int.class,
                boolean.class,
                int.class,
                String.class,
                long.class,
                long.class,
                int.class,
                java.util.List.class,
                edu.hitsz.common.BranchUpgradeChoice.class,
                edu.hitsz.common.AircraftBranch.class,
                java.util.List.class,
                boolean.class
        );
        return constructor.newInstance(
                sessionId,
                playerId,
                x,
                y,
                hp,
                score,
                ready,
                level,
                selectedSkill,
                cooldownRemainingMillis,
                cooldownTotalMillis,
                maxHp,
                Collections.emptyList(),
                null,
                null,
                Collections.emptyList(),
                false
        );
    }

    private static int localMaxHp(ClientWorldState state) throws Exception {
        Method getter = ClientWorldState.class.getMethod("getLocalMaxHp");
        return (Integer) getter.invoke(state);
    }

    private static long localCooldownTotal(ClientWorldState state) throws Exception {
        Method getter = ClientWorldState.class.getMethod("getLocalSkillCooldownTotalMillis");
        return (Long) getter.invoke(state);
    }
}
