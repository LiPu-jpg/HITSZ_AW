package edu.hitsz.client;

import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.client.aircraft.MobEnemy;
import edu.hitsz.client.aircraft.OtherPlayer;
import edu.hitsz.client.ClientWorldState;
import edu.hitsz.client.DefaultSnapshotApplier;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.protocol.SnapshotTypes;
import edu.hitsz.common.protocol.dto.BulletSnapshot;
import edu.hitsz.common.protocol.dto.EnemySnapshot;
import edu.hitsz.common.protocol.dto.ItemSnapshot;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;

import java.lang.reflect.Method;

public class SnapshotApplyTest {

    public static void main(String[] args) {
        ClientWorldState state = new ClientWorldState();
        assert state.getPlayerAircrafts().isEmpty();

        WorldSnapshot snapshot = new WorldSnapshot(5L);
        snapshot.setDifficulty(Difficulty.HARD.name());
        snapshot.setTotalScore(180);
        snapshot.setBossActive(true);
        snapshot.setNextBossScoreThreshold(320);
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-2", "player-2", 260, 340, 800, 10, false, 2, "BOMB"));
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-local", "player-local", 200, 300, 900, 66, false, 3, "FREEZE", 2500L));
        snapshot.addEnemySnapshot(new EnemySnapshot(SnapshotTypes.Enemy.MOB, 100, 120, 30));
        snapshot.addHeroBulletSnapshot(new BulletSnapshot(SnapshotTypes.Bullet.HERO, 120, 220));
        snapshot.addEnemyBulletSnapshot(new BulletSnapshot(SnapshotTypes.Bullet.ENEMY, 140, 240));
        snapshot.addItemSnapshot(new ItemSnapshot(SnapshotTypes.Item.BLOOD, 160, 260));

        WorldSnapshotJsonMapper mapper = new WorldSnapshotJsonMapper();
        new DefaultSnapshotApplier().apply(mapper.fromJson(mapper.toJson(snapshot)), state, "session-local");

        assert state.getPlayerAircrafts().size() == 2 : "Snapshot should create local and remote players";
        assert state.getPlayerAircrafts().get(0) instanceof OtherPlayer
                : "Remote player should become OtherPlayer";
        assert state.getPlayerAircrafts().get(1) == HeroAircraft.getSingleton()
                : "Client should identify local player using its sessionId";
        assert state.getEnemyAircrafts().size() == 1 : "Enemy snapshot should create enemy render object";
        assert state.getHeroBullets().size() == 1 : "Hero bullet snapshot should create render object";
        assert state.getEnemyBullets().size() == 1 : "Enemy bullet snapshot should create render object";
        assert state.getItems().size() == 1 : "Item snapshot should create render object";
        assert state.getLocalScore() == 66 : "Local score should come from local player snapshot";
        assert state.getLocalHp() == 900 : "Local HP should come from local player snapshot";
        assert state.getLocalLevel() == 3 : "Local level should come from local player snapshot";
        assert "FREEZE".equals(state.getLocalSelectedSkill()) : "Local selected skill should come from local player snapshot";
        assert localCooldown(state) == 2500L : "Local skill cooldown should come from local player snapshot";
        assert Difficulty.HARD.name().equals(state.getDifficulty()) : "Room difficulty should come from world snapshot";
        assert state.getTotalScore() == 180 : "Total room score should come from world snapshot";
        assert state.isBossActive() : "Boss state should come from world snapshot";
        assert state.getNextBossScoreThreshold() == 320 : "Boss threshold should come from world snapshot";
    }

    private static long localCooldown(ClientWorldState state) {
        try {
            Method getter = ClientWorldState.class.getMethod("getLocalSkillCooldownRemainingMillis");
            return (Long) getter.invoke(state);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("ClientWorldState should expose local skill cooldown", e);
        }
    }
}
