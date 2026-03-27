package edu.hitsz.server.basic;

import edu.hitsz.common.EntitySizing;

/**
 * 加血道具
 */
public class BloodSupply extends AbstractItem {

    public BloodSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
        this.width = EntitySizing.BLOOD_SUPPLY_WIDTH;
        this.height = EntitySizing.BLOOD_SUPPLY_HEIGHT;
    }
}
