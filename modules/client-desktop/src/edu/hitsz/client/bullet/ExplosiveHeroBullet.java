package edu.hitsz.client.bullet;

import edu.hitsz.common.EntityRenderSizing;
import edu.hitsz.common.EntitySizing;

public class ExplosiveHeroBullet extends HeroBullet {

    public ExplosiveHeroBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
        this.width = EntitySizing.HERO_BULLET_WIDTH;
        this.height = EntitySizing.HERO_BULLET_HEIGHT;
        setRenderSize(
                EntityRenderSizing.HERO_BULLET_WIDTH + 10,
                EntityRenderSizing.HERO_BULLET_HEIGHT + 10
        );
    }
}
