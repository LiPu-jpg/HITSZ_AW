package edu.hitsz.basic;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 炸弹道具
 */
public class BombSupply extends AbstractItem {

    public BombSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    public String getSkillCommandType() {
        return "BOMB";
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("BombSupply active! Request skill: " + getSkillCommandType());
    }
}
