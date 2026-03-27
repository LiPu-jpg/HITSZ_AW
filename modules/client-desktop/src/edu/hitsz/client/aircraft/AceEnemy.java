package edu.hitsz.client.aircraft;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.EntityRenderSizing;

/**
 * 王牌敌机
 */
public class AceEnemy extends EliteEnemy {

    public AceEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootNum = 2;
        this.power = 40;
        this.width = EntitySizing.ACE_ENEMY_WIDTH;
        this.height = EntitySizing.ACE_ENEMY_HEIGHT;
        setRenderSize(EntityRenderSizing.ACE_ENEMY_WIDTH, EntityRenderSizing.ACE_ENEMY_HEIGHT);
    }

    @Override
    public void forward() {
        super.forward();
    }
}
