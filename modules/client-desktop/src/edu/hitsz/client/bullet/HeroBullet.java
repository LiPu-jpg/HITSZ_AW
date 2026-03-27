package edu.hitsz.client.bullet;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.EntityRenderSizing;

/**
 * 英雄机子弹
 * @Author hitsz
 */
public class HeroBullet extends BaseBullet {

    public HeroBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
        this.width = EntitySizing.HERO_BULLET_WIDTH;
        this.height = EntitySizing.HERO_BULLET_HEIGHT;
        setRenderSize(EntityRenderSizing.HERO_BULLET_WIDTH, EntityRenderSizing.HERO_BULLET_HEIGHT);
    }

}
