package edu.hitsz.client;

import edu.hitsz.client.aircraft.AbstractAircraft;
import edu.hitsz.client.basic.AbstractItem;
import edu.hitsz.client.bullet.BaseBullet;
import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.ChapterId;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.protocol.dto.ExplosionSnapshot;
import edu.hitsz.common.protocol.dto.LaserSnapshot;

import java.util.LinkedList;
import java.util.List;

public class ClientWorldState {

    private final List<AbstractAircraft> playerAircrafts;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private List<LaserSnapshot> activeLasers = java.util.Collections.emptyList();
    private List<ExplosionSnapshot> explosionSnapshots = java.util.Collections.emptyList();
    private final List<AbstractItem> items;
    private int localHp;
    private int localScore;
    private int localLevel = 1;
    private String localSelectedSkill;
    private AircraftBranch localAircraftBranch = AircraftBranch.STARTER_BLUE;
    private boolean localBranchUnlocked;
    private long localSkillCooldownRemainingMillis;
    private List<AircraftBranch> localAvailableBranchChoices = java.util.Collections.emptyList();
    private List<BranchUpgradeChoice> localAvailableUpgradeChoices = java.util.Collections.emptyList();
    private BranchUpgradeChoice localSelectedUpgradeChoice;
    private boolean localReady;
    private boolean gameStarted;
    private int readyPlayerCount;
    private int connectedPlayerCount;
    private String difficulty = "NORMAL";
    private String roomCode;
    private String hostSessionId;
    private boolean localHost;
    private int totalScore;
    private boolean bossActive;
    private int nextBossScoreThreshold;
    private ChapterId chapterId = ChapterId.CH1;
    private GamePhase gamePhase = GamePhase.LOBBY;
    private boolean chapterTransitionFlash;
    private int bulletHitAudioCount;
    private int supplyPickupAudioCount;

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

    public List<LaserSnapshot> getActiveLasers() {
        return activeLasers;
    }

    public void setActiveLasers(List<LaserSnapshot> activeLasers) {
        this.activeLasers = activeLasers == null
                ? java.util.Collections.emptyList()
                : java.util.Collections.unmodifiableList(new java.util.ArrayList<>(activeLasers));
    }

    public List<ExplosionSnapshot> getExplosionSnapshots() {
        return explosionSnapshots;
    }

    public void setExplosionSnapshots(List<ExplosionSnapshot> explosionSnapshots) {
        this.explosionSnapshots = explosionSnapshots == null
                ? java.util.Collections.emptyList()
                : java.util.Collections.unmodifiableList(new java.util.ArrayList<>(explosionSnapshots));
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

    public int getLocalHp() {
        return localHp;
    }

    public void setLocalHp(int localHp) {
        this.localHp = localHp;
    }

    public int getLocalLevel() {
        return localLevel;
    }

    public void setLocalLevel(int localLevel) {
        this.localLevel = localLevel;
    }

    public String getLocalSelectedSkill() {
        return localSelectedSkill;
    }

    public void setLocalSelectedSkill(String localSelectedSkill) {
        this.localSelectedSkill = localSelectedSkill;
    }

    public AircraftBranch getLocalAircraftBranch() {
        return localAircraftBranch;
    }

    public void setLocalAircraftBranch(AircraftBranch localAircraftBranch) {
        this.localAircraftBranch = localAircraftBranch == null ? AircraftBranch.STARTER_BLUE : localAircraftBranch;
    }

    public boolean isLocalBranchUnlocked() {
        return localBranchUnlocked;
    }

    public void setLocalBranchUnlocked(boolean localBranchUnlocked) {
        this.localBranchUnlocked = localBranchUnlocked;
    }

    public long getLocalSkillCooldownRemainingMillis() {
        return localSkillCooldownRemainingMillis;
    }

    public void setLocalSkillCooldownRemainingMillis(long localSkillCooldownRemainingMillis) {
        this.localSkillCooldownRemainingMillis = localSkillCooldownRemainingMillis;
    }

    public List<AircraftBranch> getLocalAvailableBranchChoices() {
        return localAvailableBranchChoices;
    }

    public void setLocalAvailableBranchChoices(List<AircraftBranch> localAvailableBranchChoices) {
        this.localAvailableBranchChoices = localAvailableBranchChoices == null
                ? java.util.Collections.emptyList()
                : java.util.Collections.unmodifiableList(new java.util.ArrayList<>(localAvailableBranchChoices));
    }

    public List<BranchUpgradeChoice> getLocalAvailableUpgradeChoices() {
        return localAvailableUpgradeChoices;
    }

    public void setLocalAvailableUpgradeChoices(List<BranchUpgradeChoice> localAvailableUpgradeChoices) {
        this.localAvailableUpgradeChoices = localAvailableUpgradeChoices == null
                ? java.util.Collections.emptyList()
                : java.util.Collections.unmodifiableList(new java.util.ArrayList<>(localAvailableUpgradeChoices));
    }

    public BranchUpgradeChoice getLocalSelectedUpgradeChoice() {
        return localSelectedUpgradeChoice;
    }

    public void setLocalSelectedUpgradeChoice(BranchUpgradeChoice localSelectedUpgradeChoice) {
        this.localSelectedUpgradeChoice = localSelectedUpgradeChoice;
    }

    public boolean isLocalReady() {
        return localReady;
    }

    public void setLocalReady(boolean localReady) {
        this.localReady = localReady;
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

    public boolean isLocalHost() {
        return localHost;
    }

    public void setLocalHost(boolean localHost) {
        this.localHost = localHost;
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

    public ChapterId getChapterId() {
        return chapterId;
    }

    public void setChapterId(ChapterId chapterId) {
        this.chapterId = chapterId;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public void setGamePhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
    }

    public boolean isChapterTransitionFlash() {
        return chapterTransitionFlash;
    }

    public void setChapterTransitionFlash(boolean chapterTransitionFlash) {
        this.chapterTransitionFlash = chapterTransitionFlash;
    }

    public int getBulletHitAudioCount() {
        return bulletHitAudioCount;
    }

    public void setBulletHitAudioCount(int bulletHitAudioCount) {
        this.bulletHitAudioCount = bulletHitAudioCount;
    }

    public int getSupplyPickupAudioCount() {
        return supplyPickupAudioCount;
    }

    public void setSupplyPickupAudioCount(int supplyPickupAudioCount) {
        this.supplyPickupAudioCount = supplyPickupAudioCount;
    }
}
