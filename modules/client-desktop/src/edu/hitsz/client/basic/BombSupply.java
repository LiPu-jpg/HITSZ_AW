package edu.hitsz.client.basic;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.EntityRenderSizing;

/**
 * 炸弹道具
 */
public class BombSupply extends AbstractItem {

    public BombSupply(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
        this.width = EntitySizing.BOMB_SUPPLY_WIDTH;
        this.height = EntitySizing.BOMB_SUPPLY_HEIGHT;
        setRenderSize(EntityRenderSizing.BOMB_SUPPLY_WIDTH, EntityRenderSizing.BOMB_SUPPLY_HEIGHT);
    }

    public String getSkillCommandType() {
        return "BOMB";
    }
}
