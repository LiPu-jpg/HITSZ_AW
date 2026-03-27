package edu.hitsz.server;

import edu.hitsz.server.aircraft.AbstractAircraft;
import edu.hitsz.server.aircraft.AceEnemy;
import edu.hitsz.server.aircraft.ElitePlusEnemy;
import edu.hitsz.server.aircraft.MobEnemy;

import java.lang.reflect.Method;

public class EnemyTierSelectionTest {

    public static void main(String[] args) throws Exception {
        ServerWorldState worldState = new ServerWorldState();
        Method createEnemy = ServerWorldState.class.getDeclaredMethod("createEnemy");
        createEnemy.setAccessible(true);

        boolean sawMob = false;
        for (int i = 0; i < 50; i++) {
            AbstractAircraft enemy = (AbstractAircraft) createEnemy.invoke(worldState);
            if (enemy instanceof MobEnemy) {
                sawMob = true;
                break;
            }
        }
        assert sawMob : "Early progression should still allow basic mob enemies";

        PlayerSession session = worldState.getSessionRegistry().create("session-local", "player-local");
        session.getPlayerState().setScore(180);
        worldState.syncProgressionState();

        boolean sawElitePlus = false;
        for (int i = 0; i < 100; i++) {
            AbstractAircraft enemy = (AbstractAircraft) createEnemy.invoke(worldState);
            if (enemy instanceof ElitePlusEnemy) {
                sawElitePlus = true;
                break;
            }
        }
        assert sawElitePlus : "Mid progression should unlock ElitePlus enemies";

        session.getPlayerState().setScore(360);
        worldState.syncProgressionState();

        boolean sawAce = false;
        for (int i = 0; i < 120; i++) {
            AbstractAircraft enemy = (AbstractAircraft) createEnemy.invoke(worldState);
            if (enemy instanceof AceEnemy) {
                sawAce = true;
                break;
            }
        }
        assert sawAce : "Late progression should unlock Ace enemies";
    }
}
