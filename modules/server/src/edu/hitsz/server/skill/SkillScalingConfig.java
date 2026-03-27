package edu.hitsz.server.skill;

import edu.hitsz.server.GameplayBalance;

public class SkillScalingConfig {

    public static SkillScalingConfig defaultConfig() {
        return new SkillScalingConfig();
    }

    public long getFreezeDurationMillis(int level) {
        return GameplayBalance.FREEZE_BASE_DURATION_MILLIS
                + Math.max(0, level - 1) * GameplayBalance.FREEZE_LEVEL_BONUS_DURATION_MILLIS;
    }

    public int getBombDamage(int level) {
        return GameplayBalance.BOMB_BASE_DAMAGE
                + Math.max(0, level - 1) * GameplayBalance.BOMB_LEVEL_BONUS_DAMAGE;
    }

    public long getSkillCooldownMillis(SkillType skillType, int level) {
        switch (skillType) {
            case FREEZE:
                return GameplayBalance.FREEZE_SKILL_COOLDOWN_MILLIS;
            case BOMB:
                return GameplayBalance.BOMB_SKILL_COOLDOWN_MILLIS;
            case SHIELD:
                return GameplayBalance.SHIELD_SKILL_COOLDOWN_MILLIS;
            default:
                throw new IllegalArgumentException("Unsupported skill type: " + skillType);
        }
    }

    public long getShieldDurationMillis(int level) {
        return GameplayBalance.SHIELD_BASE_DURATION_MILLIS
                + Math.max(0, level - 1) * GameplayBalance.SHIELD_LEVEL_BONUS_DURATION_MILLIS;
    }
}
