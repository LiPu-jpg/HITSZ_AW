package edu.hitsz.client.bullet;

import edu.hitsz.common.EntityRenderSizing;
import edu.hitsz.common.EntitySizing;

public class ExplosiveEnemyBullet extends EnemyBullet {

    public ExplosiveEnemyBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
        this.width = EntitySizing.ENEMY_BULLET_WIDTH;
        this.height = EntitySizing.ENEMY_BULLET_HEIGHT;
        setRenderSize(
                EntityRenderSizing.ENEMY_BULLET_WIDTH + 8,
                EntityRenderSizing.ENEMY_BULLET_HEIGHT + 8
        );
    }
}
