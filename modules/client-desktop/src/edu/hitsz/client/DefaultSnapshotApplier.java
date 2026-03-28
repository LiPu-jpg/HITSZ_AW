package edu.hitsz.client;

import edu.hitsz.client.aircraft.AceEnemy;
import edu.hitsz.client.aircraft.AbstractAircraft;
import edu.hitsz.client.aircraft.BossEnemy;
import edu.hitsz.client.aircraft.EliteEnemy;
import edu.hitsz.client.aircraft.ElitePlusEnemy;
import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.client.aircraft.MobEnemy;
import edu.hitsz.client.aircraft.OtherPlayer;
import edu.hitsz.client.basic.AbstractItem;
import edu.hitsz.client.basic.BloodSupply;
import edu.hitsz.client.basic.BombSupply;
import edu.hitsz.client.basic.FirePlusSupply;
import edu.hitsz.client.basic.FireSupply;
import edu.hitsz.client.basic.FreezeSupply;
import edu.hitsz.client.bullet.BaseBullet;
import edu.hitsz.client.bullet.EnemyBullet;
import edu.hitsz.client.bullet.ExplosiveEnemyBullet;
import edu.hitsz.client.bullet.ExplosiveHeroBullet;
import edu.hitsz.client.bullet.HeroBullet;
import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.protocol.SnapshotTypes;
import edu.hitsz.common.protocol.dto.BulletSnapshot;
import edu.hitsz.common.protocol.dto.EnemySnapshot;
import edu.hitsz.common.protocol.dto.ItemSnapshot;
import edu.hitsz.common.protocol.dto.ExplosionSnapshot;
import edu.hitsz.common.protocol.dto.LaserSnapshot;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

public class DefaultSnapshotApplier implements SnapshotApplier {

    @Override
    public void apply(WorldSnapshot snapshot, ClientWorldState state, String localSessionId) {
        java.util.List<AbstractAircraft> nextPlayers = new java.util.LinkedList<>();
        java.util.List<AbstractAircraft> nextEnemies = new java.util.LinkedList<>();
        java.util.List<BaseBullet> nextHeroBullets = new java.util.LinkedList<>();
        java.util.List<BaseBullet> nextEnemyBullets = new java.util.LinkedList<>();
        java.util.List<LaserSnapshot> nextActiveLasers = new java.util.LinkedList<>();
        java.util.List<ExplosionSnapshot> nextExplosionSnapshots = new java.util.LinkedList<>();
        java.util.List<AbstractItem> nextItems = new java.util.LinkedList<>();
        int nextLocalHp = 0;
        int nextLocalMaxHp = 1000;
        int nextLocalScore = 0;
        int nextLocalLevel = 1;
        String nextLocalSelectedSkill = null;
        AircraftBranch nextLocalAircraftBranch = AircraftBranch.STARTER_BLUE;
        long nextLocalSkillCooldownRemainingMillis = 0L;
        long nextLocalSkillCooldownTotalMillis = 0L;
        java.util.List<AircraftBranch> nextLocalAvailableBranchChoices = java.util.Collections.emptyList();
        java.util.List<BranchUpgradeChoice> nextLocalAvailableUpgradeChoices = java.util.Collections.emptyList();
        BranchUpgradeChoice nextLocalSelectedUpgradeChoice = null;
        boolean nextLocalReady = false;
        boolean nextLocalBranchUnlocked = false;
        PlayerSnapshot localPlayerSnapshot = null;

        for (PlayerSnapshot playerSnapshot : snapshot.getPlayerSnapshots()) {
            if (playerSnapshot.getSessionId().equals(localSessionId)) {
                localPlayerSnapshot = playerSnapshot;
                nextLocalHp = playerSnapshot.getHp();
                nextLocalMaxHp = playerSnapshot.getMaxHp();
                nextLocalScore = playerSnapshot.getScore();
                nextLocalLevel = playerSnapshot.getLevel();
                nextLocalSelectedSkill = playerSnapshot.getSelectedSkill();
                nextLocalAircraftBranch = playerSnapshot.getAircraftBranch();
                nextLocalSkillCooldownRemainingMillis = playerSnapshot.getSkillCooldownRemainingMillis();
                nextLocalSkillCooldownTotalMillis = playerSnapshot.getSkillCooldownTotalMillis();
                nextLocalAvailableBranchChoices = playerSnapshot.getAvailableBranchChoices();
                nextLocalAvailableUpgradeChoices = playerSnapshot.getAvailableUpgradeChoices();
                nextLocalSelectedUpgradeChoice = playerSnapshot.getSelectedUpgradeChoice();
                nextLocalReady = playerSnapshot.isReady();
                nextLocalBranchUnlocked = playerSnapshot.isBranchUnlocked();
                continue;
            }
            if (playerSnapshot.getHp() <= 0) {
                continue;
            }
            nextPlayers.add(new OtherPlayer(
                    playerSnapshot.getPlayerId(),
                    playerSnapshot.getX(),
                    playerSnapshot.getY(),
                    0,
                    0,
                    playerSnapshot.getHp(),
                    playerSnapshot.getAircraftBranch()
            ));
        }

        for (EnemySnapshot enemySnapshot : snapshot.getEnemySnapshots()) {
            nextEnemies.add(createEnemy(enemySnapshot));
        }
        for (BulletSnapshot bulletSnapshot : snapshot.getHeroBulletSnapshots()) {
            nextHeroBullets.add(createBullet(bulletSnapshot));
        }
        for (BulletSnapshot bulletSnapshot : snapshot.getEnemyBulletSnapshots()) {
            nextEnemyBullets.add(createBullet(bulletSnapshot));
        }
        nextActiveLasers.addAll(snapshot.getLaserSnapshots());
        nextExplosionSnapshots.addAll(snapshot.getExplosionSnapshots());
        for (ItemSnapshot itemSnapshot : snapshot.getItemSnapshots()) {
            nextItems.add(createItem(itemSnapshot));
        }

        state.getPlayerAircrafts().clear();
        state.getEnemyAircrafts().clear();
        state.getHeroBullets().clear();
        state.getEnemyBullets().clear();
        state.setActiveLasers(nextActiveLasers);
        state.setExplosionSnapshots(nextExplosionSnapshots);
        state.getItems().clear();
        state.setLocalHp(nextLocalHp);
        state.setLocalMaxHp(nextLocalMaxHp);
        state.setLocalScore(nextLocalScore);
        state.setLocalLevel(nextLocalLevel);
        state.setLocalSelectedSkill(nextLocalSelectedSkill);
        state.setLocalAircraftBranch(nextLocalAircraftBranch);
        state.setLocalSkillCooldownRemainingMillis(nextLocalSkillCooldownRemainingMillis);
        state.setLocalSkillCooldownTotalMillis(nextLocalSkillCooldownTotalMillis);
        state.setLocalAvailableBranchChoices(nextLocalAvailableBranchChoices);
        state.setLocalAvailableUpgradeChoices(nextLocalAvailableUpgradeChoices);
        state.setLocalSelectedUpgradeChoice(nextLocalSelectedUpgradeChoice);
        state.setLocalReady(nextLocalReady);
        state.setLocalBranchUnlocked(nextLocalBranchUnlocked);
        state.setGameStarted(snapshot.isGameStarted());
        state.setReadyPlayerCount(snapshot.getReadyPlayerCount());
        state.setConnectedPlayerCount(snapshot.getConnectedPlayerCount());
        state.setDifficulty(snapshot.getDifficulty());
        state.setRoomCode(snapshot.getRoomCode());
        state.setHostSessionId(snapshot.getHostSessionId());
        state.setLocalHost(localSessionId != null && localSessionId.equals(snapshot.getHostSessionId()));
        state.setTotalScore(snapshot.getTotalScore());
        state.setBossActive(snapshot.isBossActive());
        state.setNextBossScoreThreshold(snapshot.getNextBossScoreThreshold());
        state.setChapterId(snapshot.getChapterId());
        state.setGamePhase(snapshot.getGamePhase());
        state.setChapterTransitionFlash(snapshot.isChapterTransitionFlash());
        state.setBulletHitAudioCount(snapshot.getBulletHitAudioCount());
        state.setSupplyPickupAudioCount(snapshot.getSupplyPickupAudioCount());
        if (localPlayerSnapshot != null) {
            HeroAircraft heroAircraft = HeroAircraft.getSingleton();
            heroAircraft.setAircraftBranch(localPlayerSnapshot.getAircraftBranch());
            heroAircraft.setLocation(localPlayerSnapshot.getX(), localPlayerSnapshot.getY());
            heroAircraft.setHp(localPlayerSnapshot.getHp());
            if (localPlayerSnapshot.getHp() > 0) {
                nextPlayers.add(heroAircraft);
            }
        }
        state.getPlayerAircrafts().addAll(nextPlayers);
        state.getEnemyAircrafts().addAll(nextEnemies);
        state.getHeroBullets().addAll(nextHeroBullets);
        state.getEnemyBullets().addAll(nextEnemyBullets);
        state.getItems().addAll(nextItems);
    }

    private AbstractAircraft createEnemy(EnemySnapshot enemySnapshot) {
        String type = enemySnapshot.getType();
        if (SnapshotTypes.Enemy.ELITE.equals(type)) {
            return new EliteEnemy(enemySnapshot.getX(), enemySnapshot.getY(), 0, 0, enemySnapshot.getHp());
        }
        if (SnapshotTypes.Enemy.ELITE_PLUS.equals(type)) {
            return new ElitePlusEnemy(enemySnapshot.getX(), enemySnapshot.getY(), 0, 0, enemySnapshot.getHp());
        }
        if (SnapshotTypes.Enemy.ACE.equals(type)) {
            return new AceEnemy(enemySnapshot.getX(), enemySnapshot.getY(), 0, 0, enemySnapshot.getHp());
        }
        if (SnapshotTypes.Enemy.BOSS.equals(type)) {
            return new BossEnemy(enemySnapshot.getX(), enemySnapshot.getY(), 0, 0, enemySnapshot.getHp());
        }
        if (SnapshotTypes.Enemy.MOB.equals(type)) {
            return new MobEnemy(enemySnapshot.getX(), enemySnapshot.getY(), 0, 0, enemySnapshot.getHp());
        }
        throw new IllegalArgumentException("Unknown enemy snapshot type: " + type);
    }

    private BaseBullet createBullet(BulletSnapshot bulletSnapshot) {
        if (SnapshotTypes.Bullet.ENEMY_EXPLOSIVE.equals(bulletSnapshot.getType())) {
            return new ExplosiveEnemyBullet(bulletSnapshot.getX(), bulletSnapshot.getY(), 0, 0, 0);
        }
        if (SnapshotTypes.Bullet.ENEMY.equals(bulletSnapshot.getType())) {
            return new EnemyBullet(bulletSnapshot.getX(), bulletSnapshot.getY(), 0, 0, 0);
        }
        if (SnapshotTypes.Bullet.HERO_EXPLOSIVE.equals(bulletSnapshot.getType())) {
            return new ExplosiveHeroBullet(bulletSnapshot.getX(), bulletSnapshot.getY(), 0, 0, 0);
        }
        if (SnapshotTypes.Bullet.HERO.equals(bulletSnapshot.getType())) {
            return new HeroBullet(bulletSnapshot.getX(), bulletSnapshot.getY(), 0, 0, 0);
        }
        throw new IllegalArgumentException("Unknown bullet snapshot type: " + bulletSnapshot.getType());
    }

    private AbstractItem createItem(ItemSnapshot itemSnapshot) {
        switch (itemSnapshot.getType()) {
            case SnapshotTypes.Item.BLOOD:
                return new BloodSupply(itemSnapshot.getX(), itemSnapshot.getY(), 0, 0);
            case SnapshotTypes.Item.FIRE:
                return new FireSupply(itemSnapshot.getX(), itemSnapshot.getY(), 0, 0);
            case SnapshotTypes.Item.FIRE_PLUS:
                return new FirePlusSupply(itemSnapshot.getX(), itemSnapshot.getY(), 0, 0);
            case SnapshotTypes.Item.BOMB:
                return new BombSupply(itemSnapshot.getX(), itemSnapshot.getY(), 0, 0);
            case SnapshotTypes.Item.FREEZE:
                return new FreezeSupply(itemSnapshot.getX(), itemSnapshot.getY(), 0, 0);
            default:
                throw new IllegalArgumentException("Unknown item snapshot type: " + itemSnapshot.getType());
        }
    }
}
