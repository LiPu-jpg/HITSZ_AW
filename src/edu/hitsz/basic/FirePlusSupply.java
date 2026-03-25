package edu.hitsz.basic;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 超级火力道具
 */
public class FirePlusSupply extends AbstractItem {

    public FirePlusSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("FirePlusSupply active!");
    }
}
