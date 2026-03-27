package edu.hitsz.server.skill;

import java.util.EnumMap;
import java.util.Map;

public class PlayerSkillState {

    private final Map<SkillType, Long> skillCooldownUntilMillis;
    private long shieldUntilMillis;

    public PlayerSkillState() {
        this.skillCooldownUntilMillis = new EnumMap<>(SkillType.class);
    }

    public void activateShield(long untilMillis) {
        shieldUntilMillis = Math.max(shieldUntilMillis, untilMillis);
    }

    public void activateSkillCooldown(SkillType skillType, long untilMillis) {
        long currentUntilMillis = skillCooldownUntilMillis.containsKey(skillType)
                ? skillCooldownUntilMillis.get(skillType)
                : 0L;
        skillCooldownUntilMillis.put(skillType, Math.max(currentUntilMillis, untilMillis));
    }

    public boolean isShieldActive(long nowMillis) {
        return nowMillis < shieldUntilMillis;
    }

    public boolean isSkillReady(SkillType skillType, long nowMillis) {
        return nowMillis >= skillCooldownUntilMillis.getOrDefault(skillType, 0L);
    }

    public long getSkillCooldownRemainingMillis(SkillType skillType, long nowMillis) {
        return Math.max(0L, skillCooldownUntilMillis.getOrDefault(skillType, 0L) - nowMillis);
    }

    public long getShieldUntilMillis() {
        return shieldUntilMillis;
    }

    public void reset() {
        shieldUntilMillis = 0L;
        skillCooldownUntilMillis.clear();
    }
}
