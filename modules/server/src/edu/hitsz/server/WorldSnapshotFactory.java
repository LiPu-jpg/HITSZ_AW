package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.protocol.SnapshotTypes;
import edu.hitsz.common.protocol.dto.BulletSnapshot;
import edu.hitsz.common.protocol.dto.EnemySnapshot;
import edu.hitsz.common.protocol.dto.ItemSnapshot;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.server.aircraft.AbstractAircraft;
import edu.hitsz.server.aircraft.AceEnemy;
import edu.hitsz.server.aircraft.BossEnemy;
import edu.hitsz.server.aircraft.EliteEnemy;
import edu.hitsz.server.aircraft.ElitePlusEnemy;
import edu.hitsz.server.aircraft.MobEnemy;
import edu.hitsz.server.basic.AbstractItem;
import edu.hitsz.server.basic.BloodSupply;
import edu.hitsz.server.basic.BombSupply;
import edu.hitsz.server.basic.FirePlusSupply;
import edu.hitsz.server.basic.FireSupply;
import edu.hitsz.server.basic.FreezeSupply;
import edu.hitsz.server.bullet.BaseBullet;
import edu.hitsz.server.skill.SkillType;

public class WorldSnapshotFactory {

    public WorldSnapshot create(ServerWorldState worldState) {
        return create(worldState, System.currentTimeMillis());
    }

    public WorldSnapshot create(ServerWorldState worldState, long nowMillis) {
        WorldSnapshot snapshot = new WorldSnapshot(worldState.getTick());
        snapshot.setDifficulty(worldState.getDifficulty().name());
        snapshot.setTotalScore(worldState.getTotalScore());
        snapshot.setBossActive(worldState.isBossActive());
        snapshot.setNextBossScoreThreshold(worldState.getNextBossScoreThreshold());
        snapshot.setChapterId(worldState.getChapterId());
        snapshot.setGamePhase(worldState.getGamePhase());
        snapshot.setFirstBossBranchSelection(worldState.getChapterProgressionState().isFirstBossBranchSelection());
        snapshot.setChapterTransitionFlash(
                worldState.getChapterProgressionState().isChapterTransitionFlashActive(nowMillis)
        );
        for (PlayerSession session : worldState.getPlayerSessions()) {
            PlayerRuntimeState playerState = session.getPlayerState();
            snapshot.addPlayerSnapshot(new PlayerSnapshot(
                    session.getSessionId(),
                    session.getPlayerId(),
                    playerState.getX(),
                    playerState.getY(),
                    playerState.getHp(),
                    playerState.getScore(),
                    session.isReady(),
                    playerState.getLevel(),
                    playerState.getSelectedSkill(),
                    skillCooldownRemainingMillis(playerState, nowMillis),
                    playerState.getAircraft().getMaxHp(),
                    playerState.getAvailableUpgradeChoices(),
                    playerState.getSelectedUpgradeChoice(),
                    playerState.getAircraftBranch(),
                    playerState.getAvailableBranchChoices(),
                    playerState.isBranchUnlocked()
            ));
        }
        for (AbstractAircraft enemyAircraft : worldState.getEnemyAircrafts()) {
            snapshot.addEnemySnapshot(new EnemySnapshot(
                    enemyTypeOf(enemyAircraft),
                    enemyAircraft.getLocationX(),
                    enemyAircraft.getLocationY(),
                    enemyAircraft.getHp()
            ));
        }
        for (BaseBullet bullet : worldState.getHeroBullets()) {
            snapshot.addHeroBulletSnapshot(new BulletSnapshot(
                    SnapshotTypes.Bullet.HERO,
                    bullet.getLocationX(),
                    bullet.getLocationY()
            ));
        }
        for (BaseBullet bullet : worldState.getEnemyBullets()) {
            snapshot.addEnemyBulletSnapshot(new BulletSnapshot(
                    SnapshotTypes.Bullet.ENEMY,
                    bullet.getLocationX(),
                    bullet.getLocationY()
            ));
        }
        for (AbstractItem item : worldState.getItems()) {
            snapshot.addItemSnapshot(new ItemSnapshot(
                    itemTypeOf(item),
                    item.getLocationX(),
                    item.getLocationY()
            ));
        }
        return snapshot;
    }

    private long skillCooldownRemainingMillis(PlayerRuntimeState playerState, long nowMillis) {
        String selectedSkill = playerState.getSelectedSkill();
        if (selectedSkill == null) {
            return 0L;
        }
        try {
            return playerState.getSkillState().getSkillCooldownRemainingMillis(
                    SkillType.valueOf(selectedSkill),
                    nowMillis
            );
        } catch (IllegalArgumentException ex) {
            return 0L;
        }
    }

    private String itemTypeOf(AbstractItem item) {
        if (item instanceof BloodSupply) {
            return SnapshotTypes.Item.BLOOD;
        }
        if (item instanceof FireSupply) {
            return SnapshotTypes.Item.FIRE;
        }
        if (item instanceof FirePlusSupply) {
            return SnapshotTypes.Item.FIRE_PLUS;
        }
        if (item instanceof BombSupply) {
            return SnapshotTypes.Item.BOMB;
        }
        if (item instanceof FreezeSupply) {
            return SnapshotTypes.Item.FREEZE;
        }
        throw new IllegalArgumentException("Unknown item snapshot type for " + item.getClass().getName());
    }

    private String enemyTypeOf(AbstractAircraft enemyAircraft) {
        if (enemyAircraft instanceof BossEnemy) {
            return SnapshotTypes.Enemy.BOSS;
        }
        if (enemyAircraft instanceof AceEnemy) {
            return SnapshotTypes.Enemy.ACE;
        }
        if (enemyAircraft instanceof ElitePlusEnemy) {
            return SnapshotTypes.Enemy.ELITE_PLUS;
        }
        if (enemyAircraft instanceof EliteEnemy) {
            return SnapshotTypes.Enemy.ELITE;
        }
        if (enemyAircraft instanceof MobEnemy) {
            return SnapshotTypes.Enemy.MOB;
        }
        throw new IllegalArgumentException("Unknown enemy snapshot type for " + enemyAircraft.getClass().getName());
    }
}
