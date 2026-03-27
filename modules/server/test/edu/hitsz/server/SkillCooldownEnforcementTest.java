package edu.hitsz.server;

import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.server.aircraft.MobEnemy;
import edu.hitsz.server.skill.DefaultServerSkillResolver;
import edu.hitsz.server.skill.SkillScalingConfig;
import edu.hitsz.server.skill.SkillType;

import java.lang.reflect.Method;

public class SkillCooldownEnforcementTest {

    public static void main(String[] args) {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-A", "player-A");
        session.getPlayerState().setLevel(1);
        session.getPlayerState().setSelectedSkill(SkillType.BOMB.name());

        MobEnemy enemy = new MobEnemy(100, 100, 0, 10, 120);
        worldState.getEnemyAircrafts().add(enemy);

        DefaultServerSkillResolver resolver = new DefaultServerSkillResolver(SkillScalingConfig.defaultConfig());
        resolver.applySkill(SkillType.BOMB, session, worldState, 1000L);
        int hpAfterFirstCast = enemy.getHp();

        resolver.applySkill(SkillType.BOMB, session, worldState, 1100L);

        assert enemy.getHp() == hpAfterFirstCast : "Skill should not apply again while cooling down";
        assert skillCooldownRemaining(session, 1100L) > 0L : "Player skill state should track remaining cooldown";

        WorldSnapshot snapshot = snapshotWithCooldown(worldState, 1100L);
        assert playerCooldown(snapshot.getPlayerSnapshots().get(0)) > 0L
                : "World snapshot should expose remaining skill cooldown";
    }

    private static long skillCooldownRemaining(PlayerSession session, long nowMillis) {
        try {
            Method getter = session.getPlayerState().getSkillState().getClass()
                    .getMethod("getSkillCooldownRemainingMillis", SkillType.class, long.class);
            return (Long) getter.invoke(session.getPlayerState().getSkillState(), SkillType.BOMB, nowMillis);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("PlayerSkillState should expose remaining cooldown", e);
        }
    }

    private static WorldSnapshot snapshotWithCooldown(ServerWorldState worldState, long nowMillis) {
        try {
            Method create = WorldSnapshotFactory.class.getMethod("create", ServerWorldState.class, long.class);
            return (WorldSnapshot) create.invoke(new WorldSnapshotFactory(), worldState, nowMillis);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("WorldSnapshotFactory should include skill cooldown data", e);
        }
    }

    private static long playerCooldown(PlayerSnapshot snapshot) {
        try {
            Method getter = PlayerSnapshot.class.getMethod("getSkillCooldownRemainingMillis");
            return (Long) getter.invoke(snapshot);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("PlayerSnapshot should expose skill cooldown", e);
        }
    }
}
