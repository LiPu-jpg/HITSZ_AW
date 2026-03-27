package edu.hitsz.server;

import edu.hitsz.server.bullet.HeroBullet;

public class ServerHeroBullet extends HeroBullet {

    private final String ownerSessionId;

    public ServerHeroBullet(String ownerSessionId, int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY, power);
        this.ownerSessionId = ownerSessionId;
    }

    public String getOwnerSessionId() {
        return ownerSessionId;
    }
}
