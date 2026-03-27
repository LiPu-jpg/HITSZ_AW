package edu.hitsz.server.skill;

import edu.hitsz.server.PlayerSession;
import edu.hitsz.server.ServerWorldState;

public interface ServerSkillResolver {

    void applySkill(
            SkillType skillType,
            PlayerSession session,
            ServerWorldState worldState,
            long nowMillis
    );
}
