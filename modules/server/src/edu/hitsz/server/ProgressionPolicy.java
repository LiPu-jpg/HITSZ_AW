package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.Difficulty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProgressionPolicy {

    private static final List<AircraftBranch> FIRST_BOSS_BRANCH_CHOICES = Collections.unmodifiableList(Arrays.asList(
            AircraftBranch.RED_SPEED,
            AircraftBranch.GREEN_DEFENSE,
            AircraftBranch.BLACK_HEAVY
    ));

    public static ProgressionPolicy defaultPolicy() {
        return new ProgressionPolicy();
    }

    public int levelForScore(int score) {
        /** 分数转等级的规则。具体阈值由 GameplayBalance.SCORE_PER_LEVEL 控制。 */
        return Math.min(GameplayBalance.MAX_LEVEL, 1 + Math.max(0, score) / GameplayBalance.SCORE_PER_LEVEL);
    }

    public int currentSpawnCycle(Difficulty difficulty, int totalScore, int bossStage) {
        /** 刷怪周期：基础值 - 分数加速 - Boss 阶段加速。 */
        int base = GameplayBalance.baseSpawnCycle(difficulty);
        int accelerated = base
                - totalScore / GameplayBalance.SPAWN_CYCLE_SCORE_DIVISOR
                - bossStage * GameplayBalance.SPAWN_CYCLE_BOSS_STAGE_ACCELERATION;
        return Math.max(GameplayBalance.MIN_SPAWN_CYCLE, accelerated);
    }

    public int currentShootCycle(Difficulty difficulty, int totalScore, int bossStage) {
        /** 敌机射击周期：基础值 - 分数加速 - Boss 阶段加速。 */
        int base = GameplayBalance.baseShootCycle(difficulty);
        int accelerated = base
                - totalScore / GameplayBalance.SHOOT_CYCLE_SCORE_DIVISOR
                - bossStage * GameplayBalance.SHOOT_CYCLE_BOSS_STAGE_ACCELERATION;
        return Math.max(GameplayBalance.MIN_SHOOT_CYCLE, accelerated);
    }

    public int currentEnemyMax(Difficulty difficulty, int totalScore, int bossStage) {
        /** 场上敌机上限：基础值 + 分数带来的压力提升 + Boss 阶段额外提升。 */
        int base = GameplayBalance.baseEnemyMax(difficulty);
        return Math.min(
                GameplayBalance.MAX_ENEMY_MAX,
                base + totalScore / GameplayBalance.ENEMY_MAX_SCORE_DIVISOR
                        + bossStage * GameplayBalance.ENEMY_MAX_BOSS_STAGE_BONUS
        );
    }

    public double currentEliteProbability(Difficulty difficulty, int totalScore, int bossStage) {
        /** 精英概率：基础值 + 分数成长 + Boss 阶段成长。 */
        double base = GameplayBalance.baseEliteProbability(difficulty);
        return Math.min(
                GameplayBalance.MAX_ELITE_PROBABILITY,
                base + totalScore / GameplayBalance.ELITE_PROBABILITY_SCORE_DIVISOR
                        + bossStage * GameplayBalance.ELITE_PROBABILITY_BOSS_STAGE_BONUS
        );
    }

    public int bossThreshold(Difficulty difficulty, int bossStage) {
        /** 第 N 次 Boss 触发分数。 */
        return GameplayBalance.initialBossThreshold(difficulty)
                + bossStage * GameplayBalance.BOSS_THRESHOLD_STEP;
    }

    public long chapterTransitionFlashMillis() {
        return GameplayBalance.CHAPTER_TRANSITION_FLASH_MILLIS;
    }

    public List<AircraftBranch> firstBossBranchChoices() {
        return FIRST_BOSS_BRANCH_CHOICES;
    }
}
