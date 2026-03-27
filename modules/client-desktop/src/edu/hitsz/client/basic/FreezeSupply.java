package edu.hitsz.client.basic;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.EntityRenderSizing;

/**
 * 冰冻道具
 */
public class FreezeSupply extends AbstractItem {

    public FreezeSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
        this.width = EntitySizing.FREEZE_SUPPLY_WIDTH;
        this.height = EntitySizing.FREEZE_SUPPLY_HEIGHT;
        setRenderSize(EntityRenderSizing.FREEZE_SUPPLY_WIDTH, EntityRenderSizing.FREEZE_SUPPLY_HEIGHT);
    }

    public String getSkillCommandType() {
        return "FREEZE";
    }
}
