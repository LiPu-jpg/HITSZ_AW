package edu.hitsz.client.aircraft;

import edu.hitsz.client.ImageManager;
import edu.hitsz.client.bullet.BaseBullet;
import edu.hitsz.common.EntityRenderSizing;
import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.GameConstants;

import java.util.LinkedList;
import java.util.List;

/**
 * 英雄飞机，游戏玩家操控
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    private static volatile HeroAircraft singleton;

    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.width = EntitySizing.HERO_WIDTH;
        this.height = EntitySizing.HERO_HEIGHT;
        setRenderSize(EntityRenderSizing.HERO_WIDTH, EntityRenderSizing.HERO_HEIGHT);
    }

    public static HeroAircraft getSingleton() {
        if (singleton == null) {
            synchronized (HeroAircraft.class) {
                if (singleton == null) {
                    singleton = new HeroAircraft(
                    GameConstants.WINDOW_WIDTH / 2,
                    GameConstants.WINDOW_HEIGHT - EntityRenderSizing.HERO_HEIGHT,
                    0,
                    0,
                    1000
            );
                }
            }
        }
        return singleton;
    }

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不通过forward函数移动
    }

    @Override
    public List<BaseBullet> shoot() {
        // 客户端英雄机仅用于显示和输入映射，子弹由服务器快照驱动。
        return new LinkedList<>();
    }

}
