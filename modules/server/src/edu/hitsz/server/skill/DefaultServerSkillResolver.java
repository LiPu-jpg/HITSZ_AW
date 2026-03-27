package edu.hitsz.server.skill;

import edu.hitsz.server.aircraft.AbstractAircraft;
import edu.hitsz.server.PlayerSession;
import edu.hitsz.server.ServerWorldState;

public class DefaultServerSkillResolver implements ServerSkillResolver {

    private final SkillScalingConfig scalingConfig;

    public DefaultServerSkillResolver(SkillScalingConfig scalingConfig) {
        this.scalingConfig = scalingConfig;
    }

    @Override
    public void applySkill(SkillType skillType, PlayerSession session, ServerWorldState worldState, long nowMillis) {
        PlayerSkillState skillState = session.getPlayerState().getSkillState();
        if (!skillState.isSkillReady(skillType, nowMillis)) {
            return;
        }
        int level = session.getPlayerState().getLevel();
        switch (skillType) {
            case FREEZE:
                worldState.getWorldEffectState().activateFreeze(
                        nowMillis + scalingConfig.getFreezeDurationMillis(level)
                );
                break;
            case BOMB:
                int damage = scalingConfig.getBombDamage(level);
                for (AbstractAircraft enemyAircraft : worldState.getEnemyAircrafts()) {
                    if (!enemyAircraft.notValid()) {
                        enemyAircraft.decreaseHp(damage);
                        if (enemyAircraft.notValid()) {
                            worldState.registerEnemyDestroyed(session, enemyAircraft);
                        }
                    }
                }
                break;
            case SHIELD:
                session.getPlayerState().getSkillState().activateShield(
                        nowMillis + scalingConfig.getShieldDurationMillis(level)
                );
                break;
            default:
                throw new IllegalArgumentException("Unsupported skill type: " + skillType);
        }
        skillState.activateSkillCooldown(skillType, nowMillis + scalingConfig.getSkillCooldownMillis(skillType, level));
    }
}
