package edu.hitsz.client.basic;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.EntityRenderSizing;

/**
 * 火力道具
 */
public class FireSupply extends AbstractItem {

    public FireSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
        this.width = EntitySizing.FIRE_SUPPLY_WIDTH;
        this.height = EntitySizing.FIRE_SUPPLY_HEIGHT;
        setRenderSize(EntityRenderSizing.FIRE_SUPPLY_WIDTH, EntityRenderSizing.FIRE_SUPPLY_HEIGHT);
    }
}
