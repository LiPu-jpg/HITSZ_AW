package edu.hitsz.client.bullet;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.EntityRenderSizing;

/**
 * 敌机子弹
 * @Author hitsz
 */
public class EnemyBullet extends BaseBullet {

    public EnemyBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
        this.width = EntitySizing.ENEMY_BULLET_WIDTH;
        this.height = EntitySizing.ENEMY_BULLET_HEIGHT;
        setRenderSize(EntityRenderSizing.ENEMY_BULLET_WIDTH, EntityRenderSizing.ENEMY_BULLET_HEIGHT);
    }

}
