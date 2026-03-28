package edu.hitsz.server;

import edu.hitsz.common.ChapterId;
import edu.hitsz.server.aircraft.BossEnemy;

import java.lang.reflect.Method;

public class BossWarningLaserPhaseTest {

    public static void main(String[] args) throws Exception {
        ch3BossShowsWarningLaserBeforeFiringDamage();
    }

    private static void ch3BossShowsWarningLaserBeforeFiringDamage() throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        session.getPlayerState().resetForNewRound(220, 680);
        worldState.startBattle();
        worldState.getChapterProgressionState().advanceToNextChapter();
        worldState.getChapterProgressionState().advanceToNextChapter();

        BossEnemy bossEnemy = new BossEnemy(220, 120, 0, 0, 240);
        worldState.getEnemyAircrafts().add(bossEnemy);

        Method shootAction = ServerWorldState.class.getDeclaredMethod("shootAction", long.class);
        shootAction.setAccessible(true);
        shootAction.invoke(worldState, 0L);

        assert worldState.getChapterId() == ChapterId.CH3 : "Precondition failed: world should be in CH3";
        assert worldState.getActiveLasers().size() == 1 : "CH3 boss should create one warning laser state";
        LaserBeamState warningLaser = worldState.getActiveLasers().get(0);
        assert "BOSS_WARNING".equals(warningLaser.getStyle()) : "First boss laser phase should be warning";
        assert warningLaser.getDamage() == 0 : "Warning laser should not deal damage";

        Method crashCheckAction = ServerWorldState.class.getDeclaredMethod("crashCheckAction", long.class);
        crashCheckAction.setAccessible(true);
        crashCheckAction.invoke(worldState, 0L);
        assert session.getPlayerState().getHp() == GameplayBalance.PLAYER_INITIAL_HP
                : "Warning laser should not damage the locked player";

        boolean fired = false;
        for (int i = 0; i < GameplayBalance.BOSS_WARNING_LASER_WARNING_TICKS + 2; i++) {
            worldState.stepWorld((long) (i + 1) * GameplayBalance.WORLD_TICK_INTERVAL_MILLIS);
            if (!worldState.getActiveLasers().isEmpty()
                    && "BOSS_FIRING".equals(worldState.getActiveLasers().get(0).getStyle())) {
                fired = true;
                break;
            }
        }

        assert fired : "Warning laser should transition into firing phase after the warning duration";
        int hpBeforeDamage = session.getPlayerState().getHp();
        crashCheckAction.invoke(worldState, (long) GameplayBalance.BOSS_WARNING_LASER_WARNING_TICKS * GameplayBalance.WORLD_TICK_INTERVAL_MILLIS);
        assert session.getPlayerState().getHp() < hpBeforeDamage
                : "Boss firing laser should damage the locked player once the warning phase ends";
    }
}
