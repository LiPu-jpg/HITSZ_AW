package edu.hitsz.common.protocol;

public final class SnapshotTypes {

    private SnapshotTypes() {
    }

    public static final class Enemy {
        public static final String MOB = "MOB";
        public static final String ELITE = "ELITE";
        public static final String ELITE_PLUS = "ELITE_PLUS";
        public static final String ACE = "ACE";
        public static final String BOSS = "BOSS";

        private Enemy() {
        }
    }

    public static final class Item {
        public static final String BLOOD = "BLOOD";
        public static final String FIRE = "FIRE";
        public static final String FIRE_PLUS = "FIRE_PLUS";
        public static final String BOMB = "BOMB";
        public static final String FREEZE = "FREEZE";

        private Item() {
        }
    }

    public static final class Bullet {
        public static final String HERO = "HERO";
        public static final String HERO_EXPLOSIVE = "HERO_EXPLOSIVE";
        public static final String ENEMY = "ENEMY";
        public static final String ENEMY_EXPLOSIVE = "ENEMY_EXPLOSIVE";

        private Bullet() {
        }
    }
}
