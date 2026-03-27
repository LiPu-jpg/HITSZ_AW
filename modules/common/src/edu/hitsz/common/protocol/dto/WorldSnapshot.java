package edu.hitsz.common.protocol.dto;

import edu.hitsz.common.ChapterId;
import edu.hitsz.common.GamePhase;
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
    private boolean gameStarted;
    private int readyPlayerCount;
    private int connectedPlayerCount;
    private String difficulty = "NORMAL";
    private String roomCode;
    private String hostSessionId;
    private int totalScore;
    private boolean bossActive;
    private int nextBossScoreThreshold;
    private GamePhase gamePhase = GamePhase.LOBBY;
    private ChapterId chapterId = ChapterId.CH1;
    private boolean chapterTransitionFlash;
    private boolean firstBossBranchSelection;

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

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public int getReadyPlayerCount() {
        return readyPlayerCount;
    }

    public void setReadyPlayerCount(int readyPlayerCount) {
        this.readyPlayerCount = readyPlayerCount;
    }

    public int getConnectedPlayerCount() {
        return connectedPlayerCount;
    }

    public void setConnectedPlayerCount(int connectedPlayerCount) {
        this.connectedPlayerCount = connectedPlayerCount;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getHostSessionId() {
        return hostSessionId;
    }

    public void setHostSessionId(String hostSessionId) {
        this.hostSessionId = hostSessionId;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public boolean isBossActive() {
        return bossActive;
    }

    public void setBossActive(boolean bossActive) {
        this.bossActive = bossActive;
    }

    public int getNextBossScoreThreshold() {
        return nextBossScoreThreshold;
    }

    public void setNextBossScoreThreshold(int nextBossScoreThreshold) {
        this.nextBossScoreThreshold = nextBossScoreThreshold;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public void setGamePhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase == null ? GamePhase.LOBBY : gamePhase;
    }

    public ChapterId getChapterId() {
        return chapterId;
    }

    public void setChapterId(ChapterId chapterId) {
        this.chapterId = chapterId == null ? ChapterId.CH1 : chapterId;
    }

    public boolean isChapterTransitionFlash() {
        return chapterTransitionFlash;
    }

    public void setChapterTransitionFlash(boolean chapterTransitionFlash) {
        this.chapterTransitionFlash = chapterTransitionFlash;
    }

    public boolean isFirstBossBranchSelection() {
        return firstBossBranchSelection;
    }

    public void setFirstBossBranchSelection(boolean firstBossBranchSelection) {
        this.firstBossBranchSelection = firstBossBranchSelection;
    }
}
