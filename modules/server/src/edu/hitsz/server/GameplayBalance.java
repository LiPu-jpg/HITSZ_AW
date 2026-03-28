package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.Difficulty;

/**
 * Centralized gameplay tuning values for the authoritative server.
 * Adjust values here when balancing enemy HP, score pacing, skill timings, and battle tempo.
 */
public final class GameplayBalance {

    public static final int WORLD_TICK_INTERVAL_MILLIS = 40;

    public static final int PLAYER_INITIAL_HP = 1000;
    public static final int PLAYER_BASE_SHOOT_NUM = 1;
    public static final int PLAYER_MAX_SHOOT_NUM = 3;
    public static final int PLAYER_BASE_SHOOT_CYCLE = 20;
    public static final int PLAYER_MIN_SHOOT_CYCLE = 8;
    public static final int PLAYER_BASE_MOVE_SPEED = 8;
    public static final int PLAYER_STOP_RADIUS = 6;
    public static final int PLAYER_BASE_BULLET_POWER = 30;
    public static final int PLAYER_MAX_BULLET_POWER = 60;
    public static final int PLAYER_BULLET_POWER_STEP = 10;
    public static final int PLAYER_BULLET_SPEED = 5;
    public static final int PLAYER_FIRE_RATE_UPGRADE_CYCLE_REDUCTION = 2;
    public static final int PLAYER_BULLET_POWER_UPGRADE_BONUS = 10;
    public static final int PLAYER_SPREAD_SHOT_UPGRADE_BONUS = 1;
    public static final int GREEN_DEFENSE_SPREAD_BULLET_COUNT = 3;
    public static final int GREEN_DEFENSE_SPREAD_X_SPEED_STEP = 2;
    public static final int RED_SPEED_MOVE_SPEED_UPGRADE_BONUS = 1;
    public static final int RED_SPEED_LASER_DAMAGE_UPGRADE_BONUS = 8;
    public static final int RED_SPEED_LASER_WIDTH_UPGRADE_BONUS = 6;
    public static final int RED_SPEED_LASER_DURATION_UPGRADE_BONUS = 1;
    public static final int GREEN_DEFENSE_SPREAD_COUNT_UPGRADE_BONUS = 1;
    public static final int GREEN_DEFENSE_SPREAD_WIDTH_UPGRADE_BONUS = 1;
    public static final int GREEN_DEFENSE_BULLET_DAMAGE_UPGRADE_BONUS = 8;
    public static final int BRANCH_MAX_HP_UPGRADE_BONUS = 120;
    public static final int BLACK_HEAVY_AIRBURST_DAMAGE_UPGRADE_BONUS = 12;
    public static final int BLACK_HEAVY_AIRBURST_RADIUS_UPGRADE_BONUS = 20;
    public static final int BLACK_HEAVY_AIRBURST_RANGE_UPGRADE_BONUS = 40;
    public static final int PLAYER_LIGHT_TRACKING_SPEED_STEP = 1;
    public static final int PLAYER_LIGHT_TRACKING_MAX_SPEED = 4;
    public static final double RED_SPEED_LASER_ANGLE = -Math.PI / 2.0;
    public static final int RED_SPEED_LASER_WIDTH = 24;
    public static final int RED_SPEED_LASER_DURATION_TICKS = 4;
    public static final int BOSS_WARNING_LASER_WIDTH = 18;
    public static final int BOSS_WARNING_LASER_LENGTH = 620;
    public static final int BOSS_WARNING_LASER_WARNING_TICKS = 38;
    public static final int BOSS_WARNING_LASER_FIRING_TICKS = 4;
    public static final int BOSS_WARNING_LASER_DAMAGE = 80;
    public static final int BLACK_HEAVY_AIRBURST_RADIUS = 90;
    public static final int BLACK_HEAVY_AIRBURST_MAX_RANGE = 280;
    public static final int BLACK_HEAVY_AIRBURST_PROJECTILE_SPEED = 28;
    public static final int BLACK_HEAVY_EXPLOSION_DURATION_TICKS = 3;

    public static final int CH2_ELITE_SPREAD_X_SPEED_STEP = 2;
    public static final int CH2_BOSS_SPREAD_X_SPEED_STEP = 2;
    public static final int CH3_ELITE_SPREAD_X_SPEED_STEP = 3;
    public static final int CH3_BOSS_SPREAD_X_SPEED_STEP = 2;
    public static final int CH4_ELITE_VOLLEY_COUNT = 5;
    public static final int CH4_ELITE_VOLLEY_STEP = 2;
    public static final int CH4_ELITE_PLUS_VOLLEY_COUNT = 7;
    public static final int CH4_ELITE_PLUS_VOLLEY_STEP = 2;
    public static final int CH4_ACE_VOLLEY_COUNT = 9;
    public static final int CH4_ACE_VOLLEY_STEP = 2;
    public static final int CH4_BOSS_VOLLEY_COUNT = 11;
    public static final int CH4_BOSS_VOLLEY_STEP = 2;
    public static final int CH5_ELITE_VOLLEY_COUNT = 5;
    public static final int CH5_ELITE_VOLLEY_STEP = 2;
    public static final int CH5_ELITE_PLUS_VOLLEY_COUNT = 7;
    public static final int CH5_ELITE_PLUS_VOLLEY_STEP = 2;
    public static final int CH5_ACE_VOLLEY_COUNT = 9;
    public static final int CH5_ACE_VOLLEY_STEP = 2;
    public static final int CH5_BOSS_VOLLEY_COUNT = 11;
    public static final int CH5_BOSS_VOLLEY_STEP = 2;

    public static final double ITEM_DROP_PROBABILITY = 0.6;

    public static final int MOB_ENEMY_HP = 30;
    public static final int MOB_ENEMY_SPEED_Y = 10;
    public static final int MOB_ENEMY_SCORE = 10;

    public static final int ELITE_ENEMY_HP = 60;
    public static final int ELITE_ENEMY_SPEED_Y = 8;
    public static final int ELITE_ENEMY_BULLET_POWER = 30;
    public static final int ELITE_ENEMY_SCORE = 20;

    public static final int ELITE_PLUS_ENEMY_HP = 80;
    public static final int ELITE_PLUS_ENEMY_SPEED_Y = 8;
    public static final int ELITE_PLUS_ENEMY_BULLET_POWER = 35;
    public static final int ELITE_PLUS_ENEMY_SCORE = 25;

    public static final int ACE_ENEMY_HP = 100;
    public static final int ACE_ENEMY_SPEED_Y = 7;
    public static final int ACE_ENEMY_BULLET_POWER = 42;
    public static final int ACE_ENEMY_SCORE = 35;

    public static final int BOSS_ENEMY_HP = 240;
    public static final int BOSS_ENEMY_SPEED_X = 4;
    public static final int BOSS_ENEMY_SPEED_Y = 0;
    public static final int BOSS_ENEMY_BULLET_POWER = 50;
    public static final int BOSS_ENEMY_SCORE = 100;

    public static final int SCORE_PER_LEVEL = 80;
    public static final int MAX_LEVEL = 5;
    public static final int BOSS_THRESHOLD_STEP = 200;
    public static final long CHAPTER_TRANSITION_FLASH_MILLIS = 1200L;

    public static final int EASY_BASE_SPAWN_CYCLE = 26;
    public static final int NORMAL_BASE_SPAWN_CYCLE = 20;
    public static final int HARD_BASE_SPAWN_CYCLE = 16;
    public static final int MIN_SPAWN_CYCLE = 8;
    public static final int SPAWN_CYCLE_SCORE_DIVISOR = 120;
    public static final int SPAWN_CYCLE_BOSS_STAGE_ACCELERATION = 2;

    public static final int EASY_BASE_SHOOT_CYCLE = 24;
    public static final int NORMAL_BASE_SHOOT_CYCLE = 20;
    public static final int HARD_BASE_SHOOT_CYCLE = 16;
    public static final int MIN_SHOOT_CYCLE = 8;
    public static final int SHOOT_CYCLE_SCORE_DIVISOR = 180;
    public static final int SHOOT_CYCLE_BOSS_STAGE_ACCELERATION = 1;

    public static final int EASY_BASE_ENEMY_MAX = 4;
    public static final int NORMAL_BASE_ENEMY_MAX = 5;
    public static final int HARD_BASE_ENEMY_MAX = 6;
    public static final int MAX_ENEMY_MAX = 10;
    public static final int ENEMY_MAX_SCORE_DIVISOR = 200;
    public static final int ENEMY_MAX_BOSS_STAGE_BONUS = 1;

    public static final double EASY_BASE_ELITE_PROBABILITY = 0.20;
    public static final double NORMAL_BASE_ELITE_PROBABILITY = 0.30;
    public static final double HARD_BASE_ELITE_PROBABILITY = 0.40;
    public static final double MAX_ELITE_PROBABILITY = 0.80;
    public static final double ELITE_PROBABILITY_SCORE_DIVISOR = 1000.0;
    public static final double ELITE_PROBABILITY_BOSS_STAGE_BONUS = 0.05;

    public static final int EASY_INITIAL_BOSS_THRESHOLD = 240;
    public static final int NORMAL_INITIAL_BOSS_THRESHOLD = 200;
    public static final int HARD_INITIAL_BOSS_THRESHOLD = 160;

    public static final int ELITE_PLUS_UNLOCK_SCORE = 150;
    public static final double ELITE_PLUS_BASE_PROBABILITY = 0.10;
    public static final double ELITE_PLUS_MAX_PROBABILITY = 0.22;
    public static final double ELITE_PLUS_SCORE_DIVISOR = 2000.0;
    public static final double ELITE_PLUS_HARD_BONUS = 0.04;
    public static final double ELITE_PLUS_EASY_BONUS = -0.02;

    public static final int ACE_UNLOCK_SCORE = 300;
    public static final double ACE_BASE_PROBABILITY = 0.05;
    public static final double ACE_MAX_PROBABILITY = 0.15;
    public static final double ACE_SCORE_DIVISOR = 3000.0;
    public static final double ACE_HARD_BONUS = 0.03;
    public static final double ACE_EASY_BONUS = -0.01;

    public static final long FREEZE_BASE_DURATION_MILLIS = 1000L;
    public static final long FREEZE_LEVEL_BONUS_DURATION_MILLIS = 500L;
    public static final long FREEZE_SKILL_COOLDOWN_MILLIS = 6000L;
    public static final int BOMB_BASE_DAMAGE = 40;
    public static final int BOMB_LEVEL_BONUS_DAMAGE = 20;
    public static final long BOMB_SKILL_COOLDOWN_MILLIS = 5000L;
    public static final long SHIELD_BASE_DURATION_MILLIS = 800L;
    public static final long SHIELD_LEVEL_BONUS_DURATION_MILLIS = 400L;
    public static final long SHIELD_SKILL_COOLDOWN_MILLIS = 7000L;
    public static final int FIRE_SUPPLY_FIREPOWER_BONUS = 1;
    public static final int FIRE_PLUS_SUPPLY_FIREPOWER_BONUS = 2;

    private GameplayBalance() {
    }

    public static int baseSpawnCycle(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return EASY_BASE_SPAWN_CYCLE;
            case HARD:
                return HARD_BASE_SPAWN_CYCLE;
            default:
                return NORMAL_BASE_SPAWN_CYCLE;
        }
    }

    public static int baseShootCycle(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return EASY_BASE_SHOOT_CYCLE;
            case HARD:
                return HARD_BASE_SHOOT_CYCLE;
            default:
                return NORMAL_BASE_SHOOT_CYCLE;
        }
    }

    public static int baseEnemyMax(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return EASY_BASE_ENEMY_MAX;
            case HARD:
                return HARD_BASE_ENEMY_MAX;
            default:
                return NORMAL_BASE_ENEMY_MAX;
        }
    }

    public static double baseEliteProbability(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return EASY_BASE_ELITE_PROBABILITY;
            case HARD:
                return HARD_BASE_ELITE_PROBABILITY;
            default:
                return NORMAL_BASE_ELITE_PROBABILITY;
        }
    }

    public static int initialBossThreshold(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return EASY_INITIAL_BOSS_THRESHOLD;
            case HARD:
                return HARD_INITIAL_BOSS_THRESHOLD;
            default:
                return NORMAL_INITIAL_BOSS_THRESHOLD;
        }
    }

    public static int playerMoveSpeed(AircraftBranch branch) {
        if (branch == null) {
            return PLAYER_BASE_MOVE_SPEED;
        }
        switch (branch) {
            case RED_SPEED:
                return PLAYER_BASE_MOVE_SPEED + 2;
            case STARTER_BLUE:
            default:
                return PLAYER_BASE_MOVE_SPEED;
        }
    }
}
