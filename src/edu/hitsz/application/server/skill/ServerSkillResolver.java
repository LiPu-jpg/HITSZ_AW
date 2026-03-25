package edu.hitsz.application.server.skill;

import edu.hitsz.application.server.PlayerSession;
import edu.hitsz.application.server.ServerWorldState;

public interface ServerSkillResolver {

    void applySkill(
            SkillType skillType,
            PlayerSession session,
            ServerWorldState worldState,
            long nowMillis
    );
}
