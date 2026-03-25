package edu.hitsz.basic;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.application.Main;

/**
 * 道具抽象父类
 */
public abstract class AbstractItem extends AbstractFlyingObject {

    public AbstractItem(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }

    public abstract void activate(HeroAircraft heroAircraft);
}
