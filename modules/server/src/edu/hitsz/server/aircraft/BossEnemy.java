package edu.hitsz.server.aircraft;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.server.GameplayBalance;
import edu.hitsz.server.bullet.BaseBullet;
import edu.hitsz.server.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

public class BossEnemy extends EliteEnemy {

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = GameplayBalance.BOSS_ENEMY_BULLET_POWER;
        this.width = EntitySizing.BOSS_ENEMY_WIDTH;
        this.height = EntitySizing.BOSS_ENEMY_HEIGHT;
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> bullets = new LinkedList<>();
        int[] spread = {-6, -4, -2, 0, 2, 4, 6};
        int x = this.getLocationX();
        int y = this.getLocationY() + direction * 2;
        int speedY = this.getSpeedY() + direction * 5;
        for (int lateralSpeed : spread) {
            bullets.add(new EnemyBullet(x + lateralSpeed * 8, y, lateralSpeed, speedY, power));
        }
        return bullets;
    }
}
