package edu.hitsz.server.bullet;

import edu.hitsz.common.EntitySizing;

/**
 * 英雄机子弹
 * @Author hitsz
 */
public class HeroBullet extends BaseBullet {

    public HeroBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
        this.width = EntitySizing.HERO_BULLET_WIDTH;
        this.height = EntitySizing.HERO_BULLET_HEIGHT;
    }

}
