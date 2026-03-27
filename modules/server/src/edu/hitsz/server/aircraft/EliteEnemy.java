package edu.hitsz.server.aircraft;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.GameConstants;
import edu.hitsz.server.GameplayBalance;
import edu.hitsz.server.bullet.BaseBullet;
import edu.hitsz.server.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 精英敌机
 */
public class EliteEnemy extends AbstractAircraft {

    protected int shootNum = 1;
    protected int power = GameplayBalance.ELITE_ENEMY_BULLET_POWER;
    protected int direction = 1;

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.width = EntitySizing.ELITE_ENEMY_WIDTH;
        this.height = EntitySizing.ELITE_ENEMY_HEIGHT;
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY >= GameConstants.WINDOW_HEIGHT) {
            vanish();
        }
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> bullets = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + direction * 2;
        int speedX = 0;
        int speedY = this.getSpeedY() + direction * 5;

        for (int i = 0; i < shootNum; i++) {
            bullets.add(new EnemyBullet(
                    x + (i * 2 - shootNum + 1) * 10,
                    y,
                    speedX,
                    speedY,
                    power
            ));
        }
        return bullets;
    }
}
