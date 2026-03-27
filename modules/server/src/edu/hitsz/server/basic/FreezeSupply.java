package edu.hitsz.server.basic;

import edu.hitsz.common.EntitySizing;

/**
 * 冰冻道具
 */
public class FreezeSupply extends AbstractItem {

    public FreezeSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
        this.width = EntitySizing.FREEZE_SUPPLY_WIDTH;
        this.height = EntitySizing.FREEZE_SUPPLY_HEIGHT;
    }

    public String getSkillCommandType() {
        return "FREEZE";
    }
}
