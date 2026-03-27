package edu.hitsz.client.basic;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.EntityRenderSizing;

/**
 * 加血道具
 */
public class BloodSupply extends AbstractItem {

    public BloodSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
        this.width = EntitySizing.BLOOD_SUPPLY_WIDTH;
        this.height = EntitySizing.BLOOD_SUPPLY_HEIGHT;
        setRenderSize(EntityRenderSizing.BLOOD_SUPPLY_WIDTH, EntityRenderSizing.BLOOD_SUPPLY_HEIGHT);
    }
}
