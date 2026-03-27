package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.server.aircraft.EliteEnemy;
import edu.hitsz.server.bullet.BaseBullet;
import edu.hitsz.server.skill.SkillScalingConfig;

public class GameplayBalanceIntegrationTest {

    public static void main(String[] args) {
        PlayerRuntimeState runtimeState = new PlayerRuntimeState("player-local");
        assert runtimeState.getHp() == GameplayBalance.PLAYER_INITIAL_HP
                : "Player initial HP should come from GameplayBalance";

        ServerPlayerAircraft aircraft = new ServerPlayerAircraft(200, 600, 0, 0, GameplayBalance.PLAYER_INITIAL_HP);
        BaseBullet heroBullet = aircraft.shoot("session-local").get(0);
        assert heroBullet.getPower() == GameplayBalance.PLAYER_BASE_BULLET_POWER
                : "Hero bullet damage should come from GameplayBalance";

        EliteEnemy eliteEnemy = new EliteEnemy(200, 120, 0, 8, GameplayBalance.ELITE_ENEMY_HP);
        BaseBullet enemyBullet = eliteEnemy.shoot().get(0);
        assert enemyBullet.getPower() == GameplayBalance.ELITE_ENEMY_BULLET_POWER
                : "Enemy bullet damage should come from GameplayBalance";

        ProgressionPolicy policy = ProgressionPolicy.defaultPolicy();
        assert policy.bossThreshold(Difficulty.NORMAL, 0) == GameplayBalance.NORMAL_INITIAL_BOSS_THRESHOLD
                : "Boss threshold should come from GameplayBalance";

        SkillScalingConfig scalingConfig = SkillScalingConfig.defaultConfig();
        assert scalingConfig.getBombDamage(3)
                == GameplayBalance.BOMB_BASE_DAMAGE + 2 * GameplayBalance.BOMB_LEVEL_BONUS_DAMAGE
                : "Skill scaling should come from GameplayBalance";
    }
}
