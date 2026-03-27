package edu.hitsz.client.aircraft;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.EntityRenderSizing;

/**
 * 强化精英敌机
 */
public class ElitePlusEnemy extends EliteEnemy {

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = 35;
        this.width = EntitySizing.ELITE_PLUS_ENEMY_WIDTH;
        this.height = EntitySizing.ELITE_PLUS_ENEMY_HEIGHT;
        setRenderSize(EntityRenderSizing.ELITE_PLUS_ENEMY_WIDTH, EntityRenderSizing.ELITE_PLUS_ENEMY_HEIGHT);
    }

    @Override
    public void forward() {
        super.forward();
    }
}
