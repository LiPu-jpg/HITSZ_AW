package edu.hitsz.server.skill;

public class WorldEffectState {

    private long frozenUntilMillis;

    public void activateFreeze(long untilMillis) {
        frozenUntilMillis = Math.max(frozenUntilMillis, untilMillis);
    }

    public boolean isFrozen(long nowMillis) {
        return nowMillis < frozenUntilMillis;
    }

    public long getFrozenUntilMillis() {
        return frozenUntilMillis;
    }

    public void reset() {
        frozenUntilMillis = 0L;
    }
}
