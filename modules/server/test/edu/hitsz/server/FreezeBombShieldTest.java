package edu.hitsz.server;

import edu.hitsz.server.aircraft.MobEnemy;
import edu.hitsz.server.PlayerSession;
import edu.hitsz.server.ServerWorldState;
import edu.hitsz.server.skill.DefaultServerSkillResolver;
import edu.hitsz.server.skill.SkillScalingConfig;
import edu.hitsz.server.skill.SkillType;

public class FreezeBombShieldTest {

    public static void main(String[] args) {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-A", "player-A");
        session.getPlayerState().setLevel(2);
        session.getPlayerState().setHp(1000);

        MobEnemy enemyA = new MobEnemy(100, 100, 0, 10, 120);
        MobEnemy enemyB = new MobEnemy(200, 100, 0, 10, 120);
        worldState.getEnemyAircrafts().add(enemyA);
        worldState.getEnemyAircrafts().add(enemyB);

        SkillScalingConfig config = SkillScalingConfig.defaultConfig();
        DefaultServerSkillResolver resolver = new DefaultServerSkillResolver(config);

        int frozenY = enemyA.getLocationY();
        resolver.applySkill(SkillType.FREEZE, session, worldState, 1000L);
        worldState.stepEnemies(1500L);
        assert enemyA.getLocationY() == frozenY : "Freeze should stop enemy movement";

        resolver.applySkill(SkillType.BOMB, session, worldState, 1600L);
        assert enemyA.getHp() < 120 : "Bomb should damage all enemies";
        assert enemyB.getHp() < 120 : "Bomb should damage all enemies";

        int hpBefore = session.getPlayerState().getHp();
        resolver.applySkill(SkillType.SHIELD, session, worldState, 1700L);
        worldState.applyDamageToPlayer(session, 200, 1800L);
        assert session.getPlayerState().getHp() == hpBefore : "Shield should prevent damage";
    }
}
