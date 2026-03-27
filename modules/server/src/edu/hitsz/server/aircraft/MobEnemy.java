package edu.hitsz.server.aircraft;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.GameConstants;
import edu.hitsz.server.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 普通敌机
 * 不可射击、不掉落道具
 * @author hitsz
 */
public class MobEnemy extends AbstractAircraft {

    public MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.width = EntitySizing.MOB_ENEMY_WIDTH;
        this.height = EntitySizing.MOB_ENEMY_HEIGHT;
    }

    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= GameConstants.WINDOW_HEIGHT ) {
            vanish();
        }
    }

    @Override
    public List<BaseBullet> shoot() {
        return new LinkedList<>();
    }

}
