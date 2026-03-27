package edu.hitsz.server.basic;

import edu.hitsz.common.EntitySizing;

/**
 * 超级火力道具
 */
public class FirePlusSupply extends AbstractItem {

    public FirePlusSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
        this.width = EntitySizing.FIRE_PLUS_SUPPLY_WIDTH;
        this.height = EntitySizing.FIRE_PLUS_SUPPLY_HEIGHT;
    }
}
