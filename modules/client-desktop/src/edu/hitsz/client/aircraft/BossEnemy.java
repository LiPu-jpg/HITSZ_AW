package edu.hitsz.client.aircraft;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.EntityRenderSizing;

/**
 * Boss 敌机
 */
public class BossEnemy extends EliteEnemy {

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootNum = 3;
        this.power = 50;
        this.width = EntitySizing.BOSS_ENEMY_WIDTH;
        this.height = EntitySizing.BOSS_ENEMY_HEIGHT;
        setRenderSize(EntityRenderSizing.BOSS_ENEMY_WIDTH, EntityRenderSizing.BOSS_ENEMY_HEIGHT);
    }

    @Override
    public void forward() {
        super.forward();
    }
}
