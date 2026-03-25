package edu.hitsz.application.protocol.dto;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class WorldSnapshot {

    private final long tick;
    private final List<PlayerSnapshot> playerSnapshots;
    private final List<EnemySnapshot> enemySnapshots;
    private final List<BulletSnapshot> heroBulletSnapshots;
    private final List<BulletSnapshot> enemyBulletSnapshots;
    private final List<ItemSnapshot> itemSnapshots;

    public WorldSnapshot(long tick) {
        this.tick = tick;
        this.playerSnapshots = new LinkedList<>();
        this.enemySnapshots = new LinkedList<>();
        this.heroBulletSnapshots = new LinkedList<>();
        this.enemyBulletSnapshots = new LinkedList<>();
        this.itemSnapshots = new LinkedList<>();
    }

    public long getTick() {
        return tick;
    }

    public void addPlayerSnapshot(PlayerSnapshot playerSnapshot) {
        playerSnapshots.add(playerSnapshot);
    }

    public List<PlayerSnapshot> getPlayerSnapshots() {
        return Collections.unmodifiableList(playerSnapshots);
    }

    public void addEnemySnapshot(EnemySnapshot enemySnapshot) {
        enemySnapshots.add(enemySnapshot);
    }

    public List<EnemySnapshot> getEnemySnapshots() {
        return Collections.unmodifiableList(enemySnapshots);
    }

    public void addHeroBulletSnapshot(BulletSnapshot bulletSnapshot) {
        heroBulletSnapshots.add(bulletSnapshot);
    }

    public List<BulletSnapshot> getHeroBulletSnapshots() {
        return Collections.unmodifiableList(heroBulletSnapshots);
    }

    public void addEnemyBulletSnapshot(BulletSnapshot bulletSnapshot) {
        enemyBulletSnapshots.add(bulletSnapshot);
    }

    public List<BulletSnapshot> getEnemyBulletSnapshots() {
        return Collections.unmodifiableList(enemyBulletSnapshots);
    }

    public void addItemSnapshot(ItemSnapshot itemSnapshot) {
        itemSnapshots.add(itemSnapshot);
    }

    public List<ItemSnapshot> getItemSnapshots() {
        return Collections.unmodifiableList(itemSnapshots);
    }
}
