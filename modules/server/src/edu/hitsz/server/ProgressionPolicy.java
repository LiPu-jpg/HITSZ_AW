package edu.hitsz.server;

import edu.hitsz.common.Difficulty;

public class ProgressionPolicy {

    public static ProgressionPolicy defaultPolicy() {
        return new ProgressionPolicy();
    }

    public int levelForScore(int score) {
        return Math.min(GameplayBalance.MAX_LEVEL, 1 + Math.max(0, score) / GameplayBalance.SCORE_PER_LEVEL);
    }

    public int currentSpawnCycle(Difficulty difficulty, int totalScore, int bossStage) {
        int base = GameplayBalance.baseSpawnCycle(difficulty);
        int accelerated = base
                - totalScore / GameplayBalance.SPAWN_CYCLE_SCORE_DIVISOR
                - bossStage * GameplayBalance.SPAWN_CYCLE_BOSS_STAGE_ACCELERATION;
        return Math.max(GameplayBalance.MIN_SPAWN_CYCLE, accelerated);
    }

    public int currentShootCycle(Difficulty difficulty, int totalScore, int bossStage) {
        int base = GameplayBalance.baseShootCycle(difficulty);
        int accelerated = base
                - totalScore / GameplayBalance.SHOOT_CYCLE_SCORE_DIVISOR
                - bossStage * GameplayBalance.SHOOT_CYCLE_BOSS_STAGE_ACCELERATION;
        return Math.max(GameplayBalance.MIN_SHOOT_CYCLE, accelerated);
    }

    public int currentEnemyMax(Difficulty difficulty, int totalScore, int bossStage) {
        int base = GameplayBalance.baseEnemyMax(difficulty);
        return Math.min(
                GameplayBalance.MAX_ENEMY_MAX,
                base + totalScore / GameplayBalance.ENEMY_MAX_SCORE_DIVISOR
                        + bossStage * GameplayBalance.ENEMY_MAX_BOSS_STAGE_BONUS
        );
    }

    public double currentEliteProbability(Difficulty difficulty, int totalScore, int bossStage) {
        double base = GameplayBalance.baseEliteProbability(difficulty);
        return Math.min(
                GameplayBalance.MAX_ELITE_PROBABILITY,
                base + totalScore / GameplayBalance.ELITE_PROBABILITY_SCORE_DIVISOR
                        + bossStage * GameplayBalance.ELITE_PROBABILITY_BOSS_STAGE_BONUS
        );
    }

    public int bossThreshold(Difficulty difficulty, int bossStage) {
        return GameplayBalance.initialBossThreshold(difficulty)
                + bossStage * GameplayBalance.BOSS_THRESHOLD_STEP;
    }

    public long chapterTransitionFlashMillis() {
        return GameplayBalance.CHAPTER_TRANSITION_FLASH_MILLIS;
    }
}
