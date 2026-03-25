package edu.hitsz.application.server.skill;

public class PlayerSkillState {

    private long shieldUntilMillis;

    public void activateShield(long untilMillis) {
        shieldUntilMillis = Math.max(shieldUntilMillis, untilMillis);
    }

    public boolean isShieldActive(long nowMillis) {
        return nowMillis < shieldUntilMillis;
    }

    public long getShieldUntilMillis() {
        return shieldUntilMillis;
    }
}
