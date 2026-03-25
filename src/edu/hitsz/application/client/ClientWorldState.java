package edu.hitsz.application.client;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.basic.AbstractItem;
import edu.hitsz.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

public class ClientWorldState {

    private final List<AbstractAircraft> playerAircrafts;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<AbstractItem> items;
    private int localScore;

    public ClientWorldState() {
        this.playerAircrafts = new LinkedList<>();
        this.enemyAircrafts = new LinkedList<>();
        this.heroBullets = new LinkedList<>();
        this.enemyBullets = new LinkedList<>();
        this.items = new LinkedList<>();
    }

    public List<AbstractAircraft> getPlayerAircrafts() {
        return playerAircrafts;
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

    public int getLocalScore() {
        return localScore;
    }

    public void setLocalScore(int localScore) {
        this.localScore = localScore;
    }
}
