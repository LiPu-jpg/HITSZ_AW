package edu.hitsz.application.server.skill;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.server.PlayerSession;
import edu.hitsz.application.server.ServerWorldState;

public class DefaultServerSkillResolver implements ServerSkillResolver {

    private final SkillScalingConfig scalingConfig;

    public DefaultServerSkillResolver(SkillScalingConfig scalingConfig) {
        this.scalingConfig = scalingConfig;
    }

    @Override
    public void applySkill(SkillType skillType, PlayerSession session, ServerWorldState worldState, long nowMillis) {
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
    }
}
