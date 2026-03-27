package edu.hitsz.client.basic;

import edu.hitsz.common.GameConstants;

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
        if (locationY >= GameConstants.WINDOW_HEIGHT) {
            vanish();
        }
    }
}
