package edu.hitsz.server;

import edu.hitsz.common.Difficulty;

public class DifficultyScalingTest {

    public static void main(String[] args) {
        ProgressionPolicy policy = ProgressionPolicy.defaultPolicy();

        int easySpawnCycle = policy.currentSpawnCycle(Difficulty.EASY, 0, 0);
        int hardSpawnCycle = policy.currentSpawnCycle(Difficulty.HARD, 0, 0);
        int progressedHardSpawnCycle = policy.currentSpawnCycle(Difficulty.HARD, 240, 1);

        assert hardSpawnCycle < easySpawnCycle : "Hard mode should spawn enemies faster than easy mode";
        assert progressedHardSpawnCycle < hardSpawnCycle
                : "Spawn cycle should continue accelerating as score and boss stage increase";
        assert policy.currentEnemyMax(Difficulty.HARD, 240, 1) > policy.currentEnemyMax(Difficulty.EASY, 0, 0)
                : "Hard mode plus progression should raise the enemy cap";
    }
}
