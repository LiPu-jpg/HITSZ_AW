package edu.hitsz.server;

import edu.hitsz.server.skill.SkillScalingConfig;

public class SkillScalingTest {

    public static void main(String[] args) {
        SkillScalingConfig config = SkillScalingConfig.defaultConfig();
        assert config.getFreezeDurationMillis(3) > config.getFreezeDurationMillis(1);
        assert config.getBombDamage(3) > config.getBombDamage(1);
        assert config.getShieldDurationMillis(3) > config.getShieldDurationMillis(1);
    }
}
