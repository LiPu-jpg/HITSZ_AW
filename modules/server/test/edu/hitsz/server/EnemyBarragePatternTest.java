package edu.hitsz.server;

import edu.hitsz.server.aircraft.AceEnemy;
import edu.hitsz.server.aircraft.BossEnemy;
import edu.hitsz.server.aircraft.EliteEnemy;
import edu.hitsz.server.aircraft.ElitePlusEnemy;

public class EnemyBarragePatternTest {

    public static void main(String[] args) {
        assert new EliteEnemy(200, 100, 0, 8, 60).shoot().size() == 1
                : "Elite enemy should keep a single straight shot";
        assert new ElitePlusEnemy(200, 100, 0, 8, 80).shoot().size() == 3
                : "ElitePlus enemy should fire a triple spread";
        assert new AceEnemy(200, 100, 0, 8, 100).shoot().size() == 5
                : "Ace enemy should fire a denser barrage";
        assert new BossEnemy(256, 120, 4, 0, 240).shoot().size() >= 7
                : "Boss enemy should fire the heaviest barrage";
    }
}
