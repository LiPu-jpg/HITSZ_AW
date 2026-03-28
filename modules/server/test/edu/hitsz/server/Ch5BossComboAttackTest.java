package edu.hitsz.server;

import edu.hitsz.common.ChapterId;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.protocol.SnapshotTypes;
import edu.hitsz.server.aircraft.BossEnemy;
import edu.hitsz.server.bullet.BaseBullet;
import edu.hitsz.server.bullet.ExplosiveEnemyBullet;

import java.lang.reflect.Method;

public class Ch5BossComboAttackTest {

    public static void main(String[] args) throws Exception {
        ch5BossCombinesExplosiveBarrageWithWarningLaser();
    }

    private static void ch5BossCombinesExplosiveBarrageWithWarningLaser() throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        session.getPlayerState().resetForNewRound(220, 680);
        worldState.startBattle();
        worldState.getChapterProgressionState().advanceToNextChapter();
        worldState.getChapterProgressionState().advanceToNextChapter();
        worldState.getChapterProgressionState().advanceToNextChapter();
        worldState.getChapterProgressionState().advanceToNextChapter();

        BossEnemy bossEnemy = new BossEnemy(220, 120, 0, 0, 240);
        worldState.getEnemyAircrafts().add(bossEnemy);

        Method shootAction = ServerWorldState.class.getDeclaredMethod("shootAction", long.class);
        shootAction.setAccessible(true);

        int volleyCycle = GameplayBalance.baseShootCycle(Difficulty.NORMAL);
        for (int i = 0; i < volleyCycle; i++) {
            shootAction.invoke(worldState, (long) i * GameplayBalance.WORLD_TICK_INTERVAL_MILLIS);
        }

        assert worldState.getChapterId() == ChapterId.CH5 : "Precondition failed: world should now be in CH5";
        assert worldState.getEnemyBullets().size() == GameplayBalance.CH5_BOSS_VOLLEY_COUNT
                : "CH5 boss should still emit the configured explosive volley";
        assert allExplosive(worldState.getEnemyBullets())
                : "CH5 boss volley should use explosive enemy bullets";
        assert worldState.getActiveLasers().size() == 1
                : "CH5 boss should also create one hostile warning laser";
        assert SnapshotTypes.Laser.BOSS_WARNING.equals(worldState.getActiveLasers().get(0).getStyle())
                : "CH5 combo should start with the warning phase of the hostile laser";
    }

    private static boolean allExplosive(Iterable<BaseBullet> bullets) {
        for (BaseBullet bullet : bullets) {
            if (!(bullet instanceof ExplosiveEnemyBullet)) {
                return false;
            }
        }
        return true;
    }
}
