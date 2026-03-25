package edu.hitsz.application.server;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;
import edu.hitsz.application.server.skill.WorldEffectState;
import edu.hitsz.basic.AbstractItem;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.basic.BloodSupply;
import edu.hitsz.basic.FirePlusSupply;
import edu.hitsz.basic.FireSupply;
import edu.hitsz.bullet.BaseBullet;

import edu.hitsz.aircraft.AbstractAircraft;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ServerWorldState {

    private static final double ELITE_ENEMY_PROBABILITY = 0.3;
    private static final double ITEM_DROP_PROBABILITY = 0.6;

    private long tick;
    private final SessionRegistry sessionRegistry;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<AbstractItem> items;
    private final WorldEffectState worldEffectState;
    private final Random random;
    private final int enemyMaxNumber;
    private final double enemySpawnCycle;
    private final double shootCycle;
    private int enemySpawnCounter;
    private int shootCounter;

    public ServerWorldState() {
        this.sessionRegistry = new SessionRegistry();
        this.enemyAircrafts = new LinkedList<>();
        this.heroBullets = new LinkedList<>();
        this.enemyBullets = new LinkedList<>();
        this.items = new LinkedList<>();
        this.worldEffectState = new WorldEffectState();
        this.random = new Random();
        this.enemyMaxNumber = 5;
        this.enemySpawnCycle = 20;
        this.shootCycle = 20;
    }

    public long getTick() {
        return tick;
    }

    public void advanceTick() {
        tick++;
    }

    public SessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }

    public Collection<PlayerSession> getPlayerSessions() {
        return Collections.unmodifiableCollection(sessionRegistry.allSessions());
    }

    public List<AbstractAircraft> getEnemyAircrafts() {
        return enemyAircrafts;
    }

    public List<BaseBullet> getHeroBullets() {
        return heroBullets;
    }

    public List<BaseBullet> getEnemyBullets() {
        return enemyBullets;
    }

    public List<AbstractItem> getItems() {
        return items;
    }

    public WorldEffectState getWorldEffectState() {
        return worldEffectState;
    }

    public void stepWorld(long nowMillis) {
        enemySpawnCounter++;
        createEnemyAction();
        shootAction(nowMillis);
        bulletsMoveAction();
        stepEnemies(nowMillis);
        itemsMoveAction();
        crashCheckAction(nowMillis);
        postProcessAction();
    }

    public void stepEnemies(long nowMillis) {
        if (worldEffectState.isFrozen(nowMillis)) {
            return;
        }
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (!enemyAircraft.notValid()) {
                enemyAircraft.forward();
            }
        }
    }

    public void applyDamageToPlayer(PlayerSession session, int damage, long nowMillis) {
        if (session.getPlayerState().getSkillState().isShieldActive(nowMillis)) {
            return;
        }
        session.getPlayerState().decreaseHp(damage);
    }

    private void createEnemyAction() {
        if (enemySpawnCounter < enemySpawnCycle) {
            return;
        }
        enemySpawnCounter = 0;
        if (enemyAircrafts.size() >= enemyMaxNumber) {
            return;
        }
        enemyAircrafts.add(createEnemy());
    }

    private AbstractAircraft createEnemy() {
        int locationX = random.nextInt(Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth())
                + ImageManager.MOB_ENEMY_IMAGE.getWidth() / 2;
        int locationY = random.nextInt(Math.max(1, (int) (Main.WINDOW_HEIGHT * 0.05)));
        if (random.nextDouble() < ELITE_ENEMY_PROBABILITY) {
            return new EliteEnemy(locationX, locationY, 0, 8, 60);
        }
        return new MobEnemy(locationX, locationY, 0, 10, 30);
    }

    private void shootAction(long nowMillis) {
        shootCounter++;
        if (shootCounter < shootCycle) {
            return;
        }

        shootCounter = 0;
        for (PlayerSession session : getPlayerSessions()) {
            if (!session.getPlayerState().getAircraft().notValid()) {
                heroBullets.addAll(session.getPlayerState().getAircraft().shoot(session.getSessionId()));
            }
        }

        if (worldEffectState.isFrozen(nowMillis)) {
            return;
        }

        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (!enemyAircraft.notValid()) {
                enemyBullets.addAll(enemyAircraft.shoot());
            }
        }
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void itemsMoveAction() {
        for (AbstractItem item : items) {
            item.forward();
        }
    }

    private void crashCheckAction(long nowMillis) {
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (PlayerSession session : getPlayerSessions()) {
                ServerPlayerAircraft playerAircraft = session.getPlayerState().getAircraft();
                if (playerAircraft.notValid()) {
                    continue;
                }
                if (playerAircraft.crash(bullet) || bullet.crash(playerAircraft)) {
                    applyDamageToPlayer(session, bullet.getPower(), nowMillis);
                    bullet.vanish();
                }
            }
        }

        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        awardScore(bullet, enemyAircraft);
                        generateSupply(enemyAircraft);
                    }
                }
            }
        }

        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft.notValid()) {
                continue;
            }
            for (PlayerSession session : getPlayerSessions()) {
                ServerPlayerAircraft playerAircraft = session.getPlayerState().getAircraft();
                if (playerAircraft.notValid()) {
                    continue;
                }
                if (enemyAircraft.crash(playerAircraft) || playerAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    applyDamageToPlayer(session, Integer.MAX_VALUE, nowMillis);
                }
            }
        }

        for (AbstractItem item : items) {
            if (item.notValid()) {
                continue;
            }
            for (PlayerSession session : getPlayerSessions()) {
                ServerPlayerAircraft playerAircraft = session.getPlayerState().getAircraft();
                if (playerAircraft.notValid()) {
                    continue;
                }
                if (item.crash(playerAircraft) || playerAircraft.crash(item)) {
                    applyItemEffect(item, session);
                    item.vanish();
                }
            }
        }
    }

    private void awardScore(BaseBullet bullet, AbstractAircraft enemyAircraft) {
        if (!(bullet instanceof ServerHeroBullet)) {
            return;
        }
        PlayerSession session = sessionRegistry.find(((ServerHeroBullet) bullet).getOwnerSessionId());
        if (session == null) {
            return;
        }
        session.getPlayerState().addScore(enemyAircraft instanceof EliteEnemy ? 20 : 10);
    }

    private void generateSupply(AbstractAircraft enemyAircraft) {
        if (!(enemyAircraft instanceof EliteEnemy)) {
            return;
        }
        if (random.nextDouble() >= ITEM_DROP_PROBABILITY) {
            return;
        }

        int locationX = enemyAircraft.getLocationX();
        int locationY = enemyAircraft.getLocationY();
        double itemType = random.nextDouble();
        if (itemType < 0.34) {
            items.add(new BloodSupply(locationX, locationY, 0, 5));
            return;
        }
        if (itemType < 0.67) {
            items.add(new FireSupply(locationX, locationY, 0, 5));
            return;
        }
        items.add(new FirePlusSupply(locationX, locationY, 0, 5));
    }

    private void applyItemEffect(AbstractItem item, PlayerSession session) {
        if (item instanceof BloodSupply) {
            session.getPlayerState().increaseHp(100);
            return;
        }
        if (item instanceof FireSupply) {
            System.out.println("FireSupply active!");
            return;
        }
        if (item instanceof FirePlusSupply) {
            System.out.println("FirePlusSupply active!");
        }
    }

    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        items.removeIf(AbstractFlyingObject::notValid);
    }
}
