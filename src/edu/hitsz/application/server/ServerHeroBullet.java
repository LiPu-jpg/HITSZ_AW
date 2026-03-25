package edu.hitsz.application.server;

import edu.hitsz.application.ImageManager;
import edu.hitsz.bullet.HeroBullet;

public class ServerHeroBullet extends HeroBullet {

    private final String ownerSessionId;

    public ServerHeroBullet(String ownerSessionId, int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
        this.ownerSessionId = ownerSessionId;
        this.width = ImageManager.HERO_BULLET_IMAGE.getWidth();
        this.height = ImageManager.HERO_BULLET_IMAGE.getHeight();
    }

    public String getOwnerSessionId() {
        return ownerSessionId;
    }
}
