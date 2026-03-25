package edu.hitsz.application.server.skill;

public class SkillScalingConfig {

    private static final long FREEZE_BASE_MILLIS = 1000L;
    private static final long FREEZE_LEVEL_BONUS_MILLIS = 500L;
    private static final int BOMB_BASE_DAMAGE = 40;
    private static final int BOMB_LEVEL_BONUS_DAMAGE = 20;
    private static final long SHIELD_BASE_MILLIS = 800L;
    private static final long SHIELD_LEVEL_BONUS_MILLIS = 400L;

    public static SkillScalingConfig defaultConfig() {
        return new SkillScalingConfig();
    }

    public long getFreezeDurationMillis(int level) {
        return FREEZE_BASE_MILLIS + Math.max(0, level - 1) * FREEZE_LEVEL_BONUS_MILLIS;
    }

    public int getBombDamage(int level) {
        return BOMB_BASE_DAMAGE + Math.max(0, level - 1) * BOMB_LEVEL_BONUS_DAMAGE;
    }

    public long getShieldDurationMillis(int level) {
        return SHIELD_BASE_MILLIS + Math.max(0, level - 1) * SHIELD_LEVEL_BONUS_MILLIS;
    }
}
