package edu.hitsz.application.server;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.server.skill.WorldEffectState;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ServerWorldState {

    private long tick;
    private final SessionRegistry sessionRegistry;
    private final List<AbstractAircraft> enemyAircrafts;
    private final WorldEffectState worldEffectState;

    public ServerWorldState() {
        this.sessionRegistry = new SessionRegistry();
        this.enemyAircrafts = new LinkedList<>();
        this.worldEffectState = new WorldEffectState();
    }

    public long getTick() {
        return tick;
    }

    public void advanceTick() {
        tick++;
    }

    public SessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }

    public Collection<PlayerSession> getPlayerSessions() {
        return Collections.unmodifiableCollection(sessionRegistry.allSessions());
    }

    public List<AbstractAircraft> getEnemyAircrafts() {
        return enemyAircrafts;
    }

    public WorldEffectState getWorldEffectState() {
        return worldEffectState;
    }

    public void stepEnemies(long nowMillis) {
        if (worldEffectState.isFrozen(nowMillis)) {
            return;
        }
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (!enemyAircraft.notValid()) {
                enemyAircraft.forward();
            }
        }
    }

    public void applyDamageToPlayer(PlayerSession session, int damage, long nowMillis) {
        if (session.getPlayerState().getSkillState().isShieldActive(nowMillis)) {
            return;
        }
        session.getPlayerState().decreaseHp(damage);
    }
}
