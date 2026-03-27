package edu.hitsz.server.basic;

import edu.hitsz.common.EntitySizing;

/**
 * 火力道具
 */
public class FireSupply extends AbstractItem {

    public FireSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
        this.width = EntitySizing.FIRE_SUPPLY_WIDTH;
        this.height = EntitySizing.FIRE_SUPPLY_HEIGHT;
    }
}
