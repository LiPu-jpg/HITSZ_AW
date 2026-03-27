package edu.hitsz.server;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.server.aircraft.AbstractAircraft;
import edu.hitsz.server.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

public class ServerPlayerAircraft extends AbstractAircraft {

    private static final int DIRECTION = -1;

    private int shootNum = GameplayBalance.PLAYER_BASE_SHOOT_NUM;
    private int bulletPower = GameplayBalance.PLAYER_BASE_BULLET_POWER;

    public ServerPlayerAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.width = EntitySizing.HERO_WIDTH;
        this.height = EntitySizing.HERO_HEIGHT;
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
        return shoot(ownerSessionId, 0);
    }

    public List<BaseBullet> shoot(String ownerSessionId, int trackingSpeedX) {
        List<BaseBullet> bullets = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + DIRECTION * 2;
        int speedY = this.getSpeedY() + DIRECTION * GameplayBalance.PLAYER_BULLET_SPEED;

        for (int i = 0; i < shootNum; i++) {
            bullets.add(new ServerHeroBullet(
                    ownerSessionId,
                    x + (i * 2 - shootNum + 1) * 10,
                    y,
                    trackingSpeedX,
                    speedY,
                    bulletPower
            ));
        }
        return bullets;
    }

    public List<BaseBullet> shootSpread(String ownerSessionId, int trackingSpeedX) {
        List<BaseBullet> bullets = new LinkedList<>();
        int spreadNum = Math.max(shootNum, GameplayBalance.GREEN_DEFENSE_SPREAD_BULLET_COUNT);
        int x = this.getLocationX();
        int y = this.getLocationY() + DIRECTION * 2;
        int speedY = this.getSpeedY() + DIRECTION * GameplayBalance.PLAYER_BULLET_SPEED;
        int centerIndex = spreadNum / 2;

        for (int i = 0; i < spreadNum; i++) {
            int speedX = trackingSpeedX + (i - centerIndex) * GameplayBalance.GREEN_DEFENSE_SPREAD_X_SPEED_STEP;
            bullets.add(new ServerHeroBullet(
                    ownerSessionId,
                    x + (i * 2 - spreadNum + 1) * 10,
                    y,
                    speedX,
                    speedY,
                    bulletPower
            ));
        }
        return bullets;
    }

    public void increaseFirepower(int amount) {
        if (amount <= 0) {
            return;
        }
        increaseShootNum(amount);
        increaseBulletPower(amount * GameplayBalance.PLAYER_BULLET_POWER_STEP);
    }

    public void increaseShootNum(int amount) {
        if (amount <= 0) {
            return;
        }
        shootNum = Math.min(GameplayBalance.PLAYER_MAX_SHOOT_NUM, shootNum + amount);
    }

    public void increaseBulletPower(int amount) {
        if (amount <= 0) {
            return;
        }
        bulletPower = Math.min(GameplayBalance.PLAYER_MAX_BULLET_POWER, bulletPower + amount);
    }

    public int getShootNum() {
        return shootNum;
    }

    public int getBulletPower() {
        return bulletPower;
    }

    public void resetForRound(int x, int y) {
        revive();
        setLocation(x, y);
        hp = maxHp;
        shootNum = GameplayBalance.PLAYER_BASE_SHOOT_NUM;
        bulletPower = GameplayBalance.PLAYER_BASE_BULLET_POWER;
    }
}
