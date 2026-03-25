package edu.hitsz.basic;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 火力道具
 */
public class FireSupply extends AbstractItem {

    public FireSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate(HeroAircraft heroAircraft) {
        System.out.println("FireSupply active!");
    }
}
