package edu.hitsz.application.server;

import edu.hitsz.application.server.skill.PlayerSkillState;

public class PlayerRuntimeState {

    private final String playerId;
    private final PlayerSkillState skillState;
    private final ServerPlayerAircraft aircraft;
    private int level;
    private int score;

    public PlayerRuntimeState(String playerId) {
        this.playerId = playerId;
        this.skillState = new PlayerSkillState();
        this.aircraft = new ServerPlayerAircraft(0, 0, 0, 0, 1000);
        this.level = 1;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getX() {
        return aircraft.getLocationX();
    }

    public int getY() {
        return aircraft.getLocationY();
    }

    public void setPosition(int x, int y) {
        aircraft.setLocation(x, y);
    }

    public int getHp() {
        return aircraft.getHp();
    }

    public void setHp(int hp) {
        aircraft.setHp(hp);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void decreaseHp(int damage) {
        aircraft.decreaseHp(damage);
    }

    public PlayerSkillState getSkillState() {
        return skillState;
    }

    public void increaseHp(int amount) {
        aircraft.increaseHp(amount);
    }

    public ServerPlayerAircraft getAircraft() {
        return aircraft;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int delta) {
        this.score += delta;
    }
}
