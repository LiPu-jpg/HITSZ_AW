package edu.hitsz.application.server;

import edu.hitsz.application.server.skill.PlayerSkillState;

public class PlayerRuntimeState {

    private final String playerId;
    private final PlayerSkillState skillState;
    private int x;
    private int y;
    private int hp;
    private int level;

    public PlayerRuntimeState(String playerId) {
        this.playerId = playerId;
        this.skillState = new PlayerSkillState();
        this.hp = 1000;
        this.level = 1;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, hp);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void decreaseHp(int damage) {
        setHp(hp - damage);
    }

    public PlayerSkillState getSkillState() {
        return skillState;
    }
}
