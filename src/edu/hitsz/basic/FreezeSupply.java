package edu.hitsz.basic;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 冰冻道具
 */
public class FreezeSupply extends AbstractItem {

    public FreezeSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    public String getSkillCommandType() {
        return "FREEZE";
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("FreezeSupply active! Request skill: " + getSkillCommandType());
    }
}
