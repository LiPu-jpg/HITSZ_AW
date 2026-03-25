package edu.hitsz.application.server;

import edu.hitsz.application.ImageManager;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

public class ServerPlayerAircraft extends AbstractAircraft {

    private static final int SHOOT_NUM = 1;
    private static final int BULLET_POWER = 30;
    private static final int DIRECTION = -1;

    public ServerPlayerAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.width = ImageManager.HERO_IMAGE.getWidth();
        this.height = ImageManager.HERO_IMAGE.getHeight();
    }

    @Override
    public void forward() {
        // Player position is controlled by client commands on the server.
    }

    @Override
    public List<BaseBullet> shoot() {
        return new LinkedList<>();
    }

    public List<BaseBullet> shoot(String ownerSessionId) {
        List<BaseBullet> bullets = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + DIRECTION * 2;
        int speedX = 0;
        int speedY = this.getSpeedY() + DIRECTION * 5;

        for (int i = 0; i < SHOOT_NUM; i++) {
            bullets.add(new ServerHeroBullet(
                    ownerSessionId,
                    x + (i * 2 - SHOOT_NUM + 1) * 10,
                    y,
                    speedX,
                    speedY,
                    BULLET_POWER
            ));
        }
        return bullets;
    }
}
