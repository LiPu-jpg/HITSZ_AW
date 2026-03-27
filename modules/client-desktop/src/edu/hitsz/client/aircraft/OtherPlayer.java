package edu.hitsz.client.aircraft;

import edu.hitsz.client.bullet.BaseBullet;
import edu.hitsz.common.EntityRenderSizing;
import edu.hitsz.common.EntitySizing;

import java.util.LinkedList;
import java.util.List;

/**
 * 其他玩家飞机，由服务器快照驱动
 */
public class OtherPlayer extends AbstractAircraft {

    private final String playerId;

    public OtherPlayer(String playerId, int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.playerId = playerId;
        this.width = EntitySizing.HERO_WIDTH;
        this.height = EntitySizing.HERO_HEIGHT;
        setRenderSize(EntityRenderSizing.HERO_WIDTH, EntityRenderSizing.HERO_HEIGHT);
    }

    public String getPlayerId() {
        return playerId;
    }

    @Override
    public void forward() {
        // 远端玩家位置由服务器快照决定，本地不主动推进
    }

    @Override
    public List<BaseBullet> shoot() {
        return new LinkedList<>();
    }
}
