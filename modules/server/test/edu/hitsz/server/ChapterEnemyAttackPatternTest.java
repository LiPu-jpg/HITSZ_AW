package edu.hitsz.server;

import edu.hitsz.common.ChapterId;
import edu.hitsz.server.aircraft.BossEnemy;
import edu.hitsz.server.aircraft.EliteEnemy;
import edu.hitsz.server.bullet.BaseBullet;
import edu.hitsz.server.bullet.ExplosiveEnemyBullet;

import java.util.List;

public class ChapterEnemyAttackPatternTest {

    public static void main(String[] args) {
        eliteAndBossPatternsChangeAcrossChapters();
    }

    private static void eliteAndBossPatternsChangeAcrossChapters() {
        ServerWorldState worldState = new ServerWorldState();
        worldState.startBattle();

        EliteEnemy elite = new EliteEnemy(200, 200, 0, 8, 60);
        BossEnemy boss = new BossEnemy(256, 120, 4, 0, 240);

        List<BaseBullet> ch1EliteVolley = worldState.buildEnemyVolleyForCurrentChapter(elite);
        List<BaseBullet> ch1BossVolley = worldState.buildEnemyVolleyForCurrentChapter(boss);
        assert worldState.getChapterId() == ChapterId.CH1 : "Precondition failed: world should start at CH1";
        assert ch1EliteVolley.size() == 1 : "CH1 elite should keep the baseline single-shot pattern";
        assert ch1BossVolley.size() == 7 : "CH1 boss should keep the baseline 7-shot fan";

        assert worldState.getChapterProgressionState().advanceToNextChapter()
                : "Test should be able to advance to CH2";
        List<BaseBullet> ch2EliteVolley = worldState.buildEnemyVolleyForCurrentChapter(elite);
        List<BaseBullet> ch2BossVolley = worldState.buildEnemyVolleyForCurrentChapter(boss);
        assert worldState.getChapterId() == ChapterId.CH2 : "Precondition failed: world should now be at CH2";
        assert ch2EliteVolley.size() == 3 : "CH2 elite should upgrade to a three-shot spread";
        assert ch2BossVolley.size() == 9 : "CH2 boss should upgrade to a denser fan";

        assert worldState.getChapterProgressionState().advanceToNextChapter()
                : "Test should be able to advance to CH3";
        List<BaseBullet> ch3EliteVolley = worldState.buildEnemyVolleyForCurrentChapter(elite);
        List<BaseBullet> ch3BossVolley = worldState.buildEnemyVolleyForCurrentChapter(boss);
        assert worldState.getChapterId() == ChapterId.CH3 : "Precondition failed: world should now be at CH3";
        assert ch3EliteVolley.size() == 3 : "CH3 elite should keep a three-shot lane count";
        assert ch3BossVolley.size() == 7 : "CH3 boss should keep a heavy seven-shot barrage";
        assert allExplosive(ch3EliteVolley) : "CH3 elite volleys should use explosive enemy bullets";
        assert allExplosive(ch3BossVolley) : "CH3 boss volleys should use explosive enemy bullets";

        assert worldState.getChapterProgressionState().advanceToNextChapter()
                : "Test should be able to advance to CH4";
        List<BaseBullet> ch4EliteVolley = worldState.buildEnemyVolleyForCurrentChapter(elite);
        List<BaseBullet> ch4BossVolley = worldState.buildEnemyVolleyForCurrentChapter(boss);
        assert worldState.getChapterId() == ChapterId.CH4 : "Precondition failed: world should now be at CH4";
        assert ch4EliteVolley.size() == 5 : "CH4 elite should upgrade to a five-shot spread";
        assert ch4BossVolley.size() == 11 : "CH4 boss should use a very dense eleven-shot fan";
        assert !allExplosive(ch4EliteVolley) : "CH4 elite volleys should stay kinetic";
        assert !allExplosive(ch4BossVolley) : "CH4 boss volleys should stay kinetic";

        assert worldState.getChapterProgressionState().advanceToNextChapter()
                : "Test should be able to advance to CH5";
        List<BaseBullet> ch5EliteVolley = worldState.buildEnemyVolleyForCurrentChapter(elite);
        List<BaseBullet> ch5BossVolley = worldState.buildEnemyVolleyForCurrentChapter(boss);
        assert worldState.getChapterId() == ChapterId.CH5 : "Precondition failed: world should now be at CH5";
        assert ch5EliteVolley.size() == 5 : "CH5 elite should keep a five-shot lane count";
        assert ch5BossVolley.size() == 11 : "CH5 boss should keep the densest eleven-shot barrage";
        assert allExplosive(ch5EliteVolley) : "CH5 elite volleys should escalate to explosive enemy bullets";
        assert allExplosive(ch5BossVolley) : "CH5 boss volleys should escalate to explosive enemy bullets";
    }

    private static boolean allExplosive(List<BaseBullet> bullets) {
        for (BaseBullet bullet : bullets) {
            if (!(bullet instanceof ExplosiveEnemyBullet)) {
                return false;
            }
        }
        return true;
    }
}
