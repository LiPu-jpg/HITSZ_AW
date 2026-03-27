package edu.hitsz.server;

import edu.hitsz.common.ChapterId;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;

public class ChapterProgressionState {

    private final ChapterCatalog chapterCatalog;
    private ChapterId chapterId;
    private GamePhase gamePhase;
    private int bossStage;
    private int nextBossScoreThreshold;
    private boolean bossEncounterActive;
    private long flashUntilMillis;

    public ChapterProgressionState() {
        this(new ChapterCatalog());
    }

    public ChapterProgressionState(ChapterCatalog chapterCatalog) {
        this.chapterCatalog = chapterCatalog;
        this.chapterId = chapterCatalog.initialChapter();
        this.gamePhase = GamePhase.LOBBY;
        this.bossStage = 0;
    }

    public ChapterId getChapterId() {
        return chapterId;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public int getBossStage() {
        return bossStage;
    }

    public int getNextBossScoreThreshold() {
        return nextBossScoreThreshold;
    }

    public boolean isBossEncounterActive() {
        return bossEncounterActive;
    }

    public long getFlashUntilMillis() {
        return flashUntilMillis;
    }

    public boolean isChapterTransitionFlashActive(long nowMillis) {
        return nowMillis < flashUntilMillis;
    }

    public void resetToLobby(Difficulty difficulty, ProgressionPolicy progressionPolicy) {
        chapterId = chapterCatalog.initialChapter();
        gamePhase = GamePhase.LOBBY;
        bossStage = 0;
        bossEncounterActive = false;
        flashUntilMillis = 0L;
        nextBossScoreThreshold = progressionPolicy.bossThreshold(difficulty, bossStage);
    }

    public void startBattle(Difficulty difficulty, ProgressionPolicy progressionPolicy) {
        chapterId = chapterCatalog.initialChapter();
        gamePhase = GamePhase.BATTLE;
        bossStage = 0;
        bossEncounterActive = false;
        flashUntilMillis = 0L;
        nextBossScoreThreshold = progressionPolicy.bossThreshold(difficulty, bossStage);
    }

    public boolean shouldSpawnBoss(int totalScore) {
        return gamePhase == GamePhase.BATTLE
                && !bossEncounterActive
                && totalScore >= nextBossScoreThreshold;
    }

    public void markBossSpawned() {
        bossEncounterActive = true;
        gamePhase = GamePhase.BATTLE;
    }

    public void reconcileBossPresence(boolean bossPresent, long nowMillis, ProgressionPolicy progressionPolicy) {
        if (bossEncounterActive && !bossPresent && gamePhase == GamePhase.BATTLE) {
            bossEncounterActive = false;
            gamePhase = GamePhase.UPGRADE_SELECTION;
            flashUntilMillis = nowMillis + progressionPolicy.chapterTransitionFlashMillis();
        } else if (bossPresent) {
            bossEncounterActive = true;
            gamePhase = GamePhase.BATTLE;
        }
    }

    public void onBossSpawned(Difficulty difficulty, ProgressionPolicy progressionPolicy) {
        bossEncounterActive = true;
        gamePhase = GamePhase.BATTLE;
        flashUntilMillis = 0L;
        bossStage++;
        nextBossScoreThreshold = progressionPolicy.bossThreshold(difficulty, bossStage);
    }

    public boolean advanceToNextChapter() {
        ChapterId nextChapterId = chapterCatalog.nextChapter(chapterId);
        if (nextChapterId == null) {
            return false;
        }
        chapterId = nextChapterId;
        gamePhase = GamePhase.BATTLE;
        bossEncounterActive = false;
        flashUntilMillis = 0L;
        return true;
    }
}
