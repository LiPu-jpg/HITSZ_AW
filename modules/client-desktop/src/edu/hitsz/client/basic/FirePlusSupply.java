package edu.hitsz.client.basic;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.EntityRenderSizing;

/**
 * 超级火力道具
 */
public class FirePlusSupply extends AbstractItem {

    public FirePlusSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
        this.width = EntitySizing.FIRE_PLUS_SUPPLY_WIDTH;
        this.height = EntitySizing.FIRE_PLUS_SUPPLY_HEIGHT;
        setRenderSize(EntityRenderSizing.FIRE_PLUS_SUPPLY_WIDTH, EntityRenderSizing.FIRE_PLUS_SUPPLY_HEIGHT);
    }
}
