package edu.hitsz.server;

import edu.hitsz.server.aircraft.AbstractAircraft;
import edu.hitsz.server.basic.AbstractItem;
import edu.hitsz.server.bullet.BaseBullet;

import java.util.Collections;
import java.util.List;

public class WorldSnapshotFactoryTypeSafetyTest {

    public static void main(String[] args) {
        expectIllegalArgumentForUnknownEnemy();
        expectIllegalArgumentForUnknownItem();
    }

    private static void expectIllegalArgumentForUnknownEnemy() {
        ServerWorldState worldState = new ServerWorldState();
        worldState.getEnemyAircrafts().add(new UnknownEnemy());
        try {
            new WorldSnapshotFactory().create(worldState);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Unknown enemy snapshot types should fail fast on the server");
    }

    private static void expectIllegalArgumentForUnknownItem() {
        ServerWorldState worldState = new ServerWorldState();
        worldState.getItems().add(new UnknownItem());
        try {
            new WorldSnapshotFactory().create(worldState);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Unknown item snapshot types should fail fast on the server");
    }

    private static final class UnknownEnemy extends AbstractAircraft {
        private UnknownEnemy() {
            super(100, 100, 0, 0, 10);
            this.width = 30;
            this.height = 30;
        }

        @Override
        public List<BaseBullet> shoot() {
            return Collections.emptyList();
        }
    }

    private static final class UnknownItem extends AbstractItem {
        private UnknownItem() {
            super(100, 100, 0, 0);
            this.width = 20;
            this.height = 20;
        }
    }
}
