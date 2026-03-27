package edu.hitsz.server.aircraft;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.server.GameplayBalance;
import edu.hitsz.server.bullet.BaseBullet;
import edu.hitsz.server.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

public class ElitePlusEnemy extends EliteEnemy {

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = GameplayBalance.ELITE_PLUS_ENEMY_BULLET_POWER;
        this.width = EntitySizing.ELITE_PLUS_ENEMY_WIDTH;
        this.height = EntitySizing.ELITE_PLUS_ENEMY_HEIGHT;
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> bullets = new LinkedList<>();
        int[] spread = {-2, 0, 2};
        int x = this.getLocationX();
        int y = this.getLocationY() + direction * 2;
        int speedY = this.getSpeedY() + direction * 5;
        for (int lateralSpeed : spread) {
            bullets.add(new EnemyBullet(x + lateralSpeed * 8, y, lateralSpeed, speedY, power));
        }
        return bullets;
    }
}
