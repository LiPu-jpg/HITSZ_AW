package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.GamePhase;
import edu.hitsz.server.aircraft.AbstractAircraft;
import edu.hitsz.server.aircraft.AceEnemy;
import edu.hitsz.server.aircraft.BossEnemy;
import edu.hitsz.server.aircraft.EliteEnemy;
import edu.hitsz.server.aircraft.ElitePlusEnemy;
import edu.hitsz.server.aircraft.MobEnemy;
import edu.hitsz.server.basic.AbstractFlyingObject;
import edu.hitsz.server.basic.AbstractItem;
import edu.hitsz.server.basic.BloodSupply;
import edu.hitsz.server.basic.BombSupply;
import edu.hitsz.server.basic.FirePlusSupply;
import edu.hitsz.server.basic.FireSupply;
import edu.hitsz.server.basic.FreezeSupply;
import edu.hitsz.server.bullet.BaseBullet;
import edu.hitsz.common.GameConstants;
import edu.hitsz.common.protocol.dto.ExplosionSnapshot;
import edu.hitsz.server.skill.DefaultServerSkillResolver;
import edu.hitsz.server.skill.ServerSkillResolver;
import edu.hitsz.server.skill.SkillScalingConfig;
import edu.hitsz.server.skill.SkillType;
import edu.hitsz.server.skill.WorldEffectState;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ServerWorldState {

    private long tick;
    private final SessionRegistry sessionRegistry;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<LaserBeamState> activeLasers;
    private final List<AirburstProjectileState> airburstProjectiles;
    private final List<ActiveExplosionState> activeExplosionStates;
    private final List<AbstractItem> items;
    private final WorldEffectState worldEffectState;
    private final ServerSkillResolver skillResolver;
    private final ProgressionPolicy progressionPolicy;
    private final ChapterProgressionState chapterProgressionState;
    private final Random random;
    private Difficulty difficulty;
    private int totalScore;
    private boolean bossActive;
    private int enemySpawnCounter;
    private int shootCounter;

    public ServerWorldState() {
        this.sessionRegistry = new SessionRegistry();
        this.enemyAircrafts = new LinkedList<>();
        this.heroBullets = new LinkedList<>();
        this.enemyBullets = new LinkedList<>();
        this.activeLasers = new LinkedList<>();
        this.airburstProjectiles = new LinkedList<>();
        this.activeExplosionStates = new LinkedList<>();
        this.items = new LinkedList<>();
        this.worldEffectState = new WorldEffectState();
        this.skillResolver = new DefaultServerSkillResolver(SkillScalingConfig.defaultConfig());
        this.progressionPolicy = ProgressionPolicy.defaultPolicy();
        this.chapterProgressionState = new ChapterProgressionState();
        this.random = new Random();
        this.difficulty = Difficulty.NORMAL;
        this.chapterProgressionState.resetToLobby(difficulty, progressionPolicy);
    }

    public long getTick() {
        return tick;
    }

    public void advanceTick() {
        tick++;
    }

    public SessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }

    public Collection<PlayerSession> getPlayerSessions() {
        return Collections.unmodifiableCollection(sessionRegistry.activeSessions());
    }

    public Collection<PlayerSession> getAllPlayerSessions() {
        return Collections.unmodifiableCollection(sessionRegistry.allSessions());
    }

    public List<AbstractAircraft> getEnemyAircrafts() {
        return enemyAircrafts;
    }

    public List<BaseBullet> getHeroBullets() {
        return heroBullets;
    }

    public List<BaseBullet> getEnemyBullets() {
        return enemyBullets;
    }

    public List<LaserBeamState> getActiveLasers() {
        return activeLasers;
    }

    public List<ExplosionSnapshot> getExplosionSnapshots() {
        List<ExplosionSnapshot> snapshots = new LinkedList<>();
        for (ActiveExplosionState explosionState : activeExplosionStates) {
            snapshots.add(explosionState.toSnapshot());
        }
        return Collections.unmodifiableList(snapshots);
    }

    public List<AbstractItem> getItems() {
        return items;
    }

    public WorldEffectState getWorldEffectState() {
        return worldEffectState;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public ChapterProgressionState getChapterProgressionState() {
        return chapterProgressionState;
    }

    public edu.hitsz.common.ChapterId getChapterId() {
        return chapterProgressionState.getChapterId();
    }

    public edu.hitsz.common.GamePhase getGamePhase() {
        return chapterProgressionState.getGamePhase();
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.totalScore = 0;
        this.bossActive = false;
        this.chapterProgressionState.resetToLobby(difficulty, progressionPolicy);
    }

    public int getTotalScore() {
        return totalScore;
    }

    public boolean isBossActive() {
        return bossActive;
    }

    public int getNextBossScoreThreshold() {
        return chapterProgressionState.getNextBossScoreThreshold();
    }

    public long getFlashUntilMillis() {
        return chapterProgressionState.getFlashUntilMillis();
    }

    public void stepWorld(long nowMillis) {
        syncProgressionState(nowMillis);
        advanceExplosionAction();
        if (chapterProgressionState.getGamePhase() == GamePhase.UPGRADE_SELECTION
                || chapterProgressionState.getGamePhase() == GamePhase.BRANCH_SELECTION) {
            activeLasers.clear();
            return;
        }
        movePlayersAction();
        enemySpawnCounter++;
        createEnemyAction();
        shootAction(nowMillis);
        burstAction(nowMillis);
        bulletsMoveAction();
        stepEnemies(nowMillis);
        itemsMoveAction();
        crashCheckAction(nowMillis);
        postProcessAction();
        syncProgressionState(nowMillis);
    }

    public void stepEnemies(long nowMillis) {
        if (worldEffectState.isFrozen(nowMillis)) {
            return;
        }
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (!enemyAircraft.notValid()) {
                enemyAircraft.forward();
            }
        }
    }

    private void movePlayersAction() {
        for (PlayerSession session : getPlayerSessions()) {
            PlayerRuntimeState playerState = session.getPlayerState();
            if (playerState.getAircraft().notValid()) {
                continue;
            }
            playerState.advanceTowardTarget();
        }
    }

    public void applyDamageToPlayer(PlayerSession session, int damage, long nowMillis) {
        if (session.getPlayerState().getSkillState().isShieldActive(nowMillis)) {
            return;
        }
        session.getPlayerState().decreaseHp(damage);
    }

    public void resetRoundState() {
        tick = 0L;
        enemySpawnCounter = 0;
        shootCounter = 0;
        enemyAircrafts.clear();
        heroBullets.clear();
        enemyBullets.clear();
        activeLasers.clear();
        airburstProjectiles.clear();
        activeExplosionStates.clear();
        items.clear();
        worldEffectState.reset();
        totalScore = 0;
        bossActive = false;
        chapterProgressionState.resetToLobby(difficulty, progressionPolicy);
    }

    public void startBattle() {
        chapterProgressionState.startBattle(difficulty, progressionPolicy);
    }

    public boolean advanceAfterBossSelection() {
        for (PlayerSession session : getAllPlayerSessions()) {
            session.getPlayerState().clearBranchSelectionState();
            session.getPlayerState().clearUpgradeSelectionState();
        }
        if (chapterProgressionState.isFirstBossBranchSelection()) {
            chapterProgressionState.markFirstBossBranchSelectionCompleted();
        }
        enemySpawnCounter = 0;
        shootCounter = 0;
        heroBullets.clear();
        enemyBullets.clear();
        activeLasers.clear();
        airburstProjectiles.clear();
        activeExplosionStates.clear();
        items.clear();
        bossActive = false;
        return chapterProgressionState.advanceToNextChapter();
    }

    public boolean hasAnyAlivePlayer() {
        for (PlayerSession session : getAllPlayerSessions()) {
            if (!session.getPlayerState().getAircraft().notValid()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyConnectedAlivePlayer() {
        for (PlayerSession session : getPlayerSessions()) {
            if (!session.getPlayerState().getAircraft().notValid()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyDisconnectedAlivePlayer() {
        for (PlayerSession session : getAllPlayerSessions()) {
            if (!session.isConnected() && !session.getPlayerState().getAircraft().notValid()) {
                return true;
            }
        }
        return false;
    }

    private void createEnemyAction() {
        int currentSpawnCycle = progressionPolicy.currentSpawnCycle(difficulty, totalScore, chapterProgressionState.getBossStage());
        if (enemySpawnCounter < currentSpawnCycle) {
            return;
        }
        enemySpawnCounter = 0;
        if (chapterProgressionState.isBossEncounterActive()) {
            return;
        }
        if (enemyAircrafts.size() >= progressionPolicy.currentEnemyMax(difficulty, totalScore, chapterProgressionState.getBossStage())) {
            return;
        }
        enemyAircrafts.add(createEnemy());
    }

    private AbstractAircraft createEnemy() {
        int locationX = random.nextInt(GameConstants.WINDOW_WIDTH - EntitySizing.MOB_ENEMY_WIDTH)
                + EntitySizing.MOB_ENEMY_WIDTH / 2;
        int locationY = random.nextInt(Math.max(1, (int) (GameConstants.WINDOW_HEIGHT * 0.05)));
        double roll = random.nextDouble();
        double aceChance = aceEnemyProbability();
        double elitePlusChance = elitePlusEnemyProbability();
        double eliteChance = progressionPolicy.currentEliteProbability(difficulty, totalScore, chapterProgressionState.getBossStage());

        if (roll < aceChance) {
            return new AceEnemy(locationX, locationY, 0, GameplayBalance.ACE_ENEMY_SPEED_Y, GameplayBalance.ACE_ENEMY_HP);
        }
        if (roll < aceChance + elitePlusChance) {
            return new ElitePlusEnemy(
                    locationX,
                    locationY,
                    0,
                    GameplayBalance.ELITE_PLUS_ENEMY_SPEED_Y,
                    GameplayBalance.ELITE_PLUS_ENEMY_HP
            );
        }
        if (roll < aceChance + elitePlusChance + eliteChance) {
            return new EliteEnemy(locationX, locationY, 0, GameplayBalance.ELITE_ENEMY_SPEED_Y, GameplayBalance.ELITE_ENEMY_HP);
        }
        return new MobEnemy(locationX, locationY, 0, GameplayBalance.MOB_ENEMY_SPEED_Y, GameplayBalance.MOB_ENEMY_HP);
    }

    private void shootAction(long nowMillis) {
        for (PlayerSession session : getPlayerSessions()) {
            PlayerRuntimeState playerState = session.getPlayerState();
            if (playerState.getAircraft().notValid() || !playerState.shouldShootAtTick(tick)) {
                continue;
            }
        if (playerState.usesLaserWeapon()) {
            replaceActiveLaser(playerState.createLaserBeam(session.getSessionId()));
        } else if (playerState.usesSpreadWeapon()) {
            heroBullets.addAll(playerState.getAircraft().shootSpread(
                    session.getSessionId(),
                    playerState.trackingSpeedXForTarget(nearestEnemyX(playerState.getX())),
                    playerState.currentGreenSpreadBulletCount(),
                    playerState.currentGreenSpreadWidthStep()
            ));
        } else if (playerState.usesAirburstWeapon()) {
            AbstractAircraft targetEnemy = nearestEnemy(playerState);
            int targetX = targetEnemy == null ? playerState.getX() : targetEnemy.getLocationX();
            int targetY = targetEnemy == null
                    ? -GameConstants.WINDOW_HEIGHT
                    : targetEnemy.getLocationY();
            airburstProjectiles.add(playerState.createAirburstProjectile(session.getSessionId(), targetX, targetY));
        } else {
            heroBullets.addAll(playerState.getAircraft().shoot(
                    session.getSessionId(),
                    playerState.trackingSpeedXForTarget(nearestEnemyX(playerState.getX()))
            ));
            }
            playerState.markShotAtTick(tick);
        }

        shootCounter++;
        if (shootCounter < progressionPolicy.currentShootCycle(difficulty, totalScore, chapterProgressionState.getBossStage())) {
            return;
        }

        shootCounter = 0;
        if (worldEffectState.isFrozen(nowMillis)) {
            return;
        }

        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (!enemyAircraft.notValid()) {
                enemyBullets.addAll(enemyAircraft.shoot());
            }
        }
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void itemsMoveAction() {
        for (AbstractItem item : items) {
            item.forward();
        }
    }

    private void crashCheckAction(long nowMillis) {
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (PlayerSession session : getPlayerSessions()) {
                ServerPlayerAircraft playerAircraft = session.getPlayerState().getAircraft();
                if (playerAircraft.notValid()) {
                    continue;
                }
                if (playerAircraft.crash(bullet) || bullet.crash(playerAircraft)) {
                    applyDamageToPlayer(session, bullet.getPower(), nowMillis);
                    bullet.vanish();
                    break;
                }
            }
        }

        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        awardScore(bullet, enemyAircraft);
                    }
                    break;
                }
            }
        }

        for (LaserBeamState laser : activeLasers) {
            if (laser.isExpired()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    continue;
                }
                if (laserHitsEnemy(laser, enemyAircraft)) {
                    enemyAircraft.decreaseHp(laser.getDamage());
                    if (enemyAircraft.notValid()) {
                        awardScore(laser.getOwnerSessionId(), enemyAircraft);
                    }
                }
            }
        }

        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft.notValid()) {
                continue;
            }
            for (PlayerSession session : getPlayerSessions()) {
                ServerPlayerAircraft playerAircraft = session.getPlayerState().getAircraft();
                if (playerAircraft.notValid()) {
                    continue;
                }
                if (enemyAircraft.crash(playerAircraft) || playerAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    applyDamageToPlayer(session, Integer.MAX_VALUE, nowMillis);
                    break;
                }
            }
        }

        for (AbstractItem item : items) {
            if (item.notValid()) {
                continue;
            }
            for (PlayerSession session : getPlayerSessions()) {
                ServerPlayerAircraft playerAircraft = session.getPlayerState().getAircraft();
                if (playerAircraft.notValid()) {
                    continue;
                }
                if (item.crash(playerAircraft) || playerAircraft.crash(item)) {
                    applyItemEffect(item, session, nowMillis);
                    item.vanish();
                    break;
                }
            }
        }
    }

    private void awardScore(BaseBullet bullet, AbstractAircraft enemyAircraft) {
        if (!(bullet instanceof ServerHeroBullet)) {
            return;
        }
        awardScore(((ServerHeroBullet) bullet).getOwnerSessionId(), enemyAircraft);
    }

    private void awardScore(String ownerSessionId, AbstractAircraft enemyAircraft) {
        PlayerSession session = sessionRegistry.find(ownerSessionId);
        if (session == null) {
            return;
        }
        registerEnemyDestroyed(session, enemyAircraft);
    }

    private void generateSupply(AbstractAircraft enemyAircraft) {
        if (enemyAircraft == null || enemyAircraft.getClass() != EliteEnemy.class) {
            return;
        }
        if (random.nextDouble() >= GameplayBalance.ITEM_DROP_PROBABILITY) {
            return;
        }

        int locationX = enemyAircraft.getLocationX();
        int locationY = enemyAircraft.getLocationY();
        double itemType = random.nextDouble();
        if (itemType < 0.20) {
            items.add(new BloodSupply(locationX, locationY, 0, 5));
            return;
        }
        if (itemType < 0.40) {
            items.add(new FireSupply(locationX, locationY, 0, 5));
            return;
        }
        if (itemType < 0.60) {
            items.add(new FirePlusSupply(locationX, locationY, 0, 5));
            return;
        }
        if (itemType < 0.80) {
            items.add(new BombSupply(locationX, locationY, 0, 5));
            return;
        }
        items.add(new FreezeSupply(locationX, locationY, 0, 5));
    }

    private void applyItemEffect(AbstractItem item, PlayerSession session, long nowMillis) {
        if (item instanceof BloodSupply) {
            session.getPlayerState().increaseHp(100);
            return;
        }
        if (item instanceof FireSupply) {
            session.getPlayerState().increaseFirepower(GameplayBalance.FIRE_SUPPLY_FIREPOWER_BONUS);
            return;
        }
        if (item instanceof FirePlusSupply) {
            session.getPlayerState().increaseFirepower(GameplayBalance.FIRE_PLUS_SUPPLY_FIREPOWER_BONUS);
            return;
        }
        if (item instanceof BombSupply) {
            skillResolver.applySkill(SkillType.BOMB, session, this, nowMillis);
            return;
        }
        if (item instanceof FreezeSupply) {
            skillResolver.applySkill(SkillType.FREEZE, session, this, nowMillis);
        }
    }

    public void syncProgressionState() {
        syncProgressionState(System.currentTimeMillis());
    }

    public void syncProgressionState(long nowMillis) {
        GamePhase previousPhase = chapterProgressionState.getGamePhase();
        totalScore = 0;
        for (PlayerSession session : getAllPlayerSessions()) {
            session.getPlayerState().syncProgression(progressionPolicy);
            totalScore += session.getPlayerState().getScore();
        }
        chapterProgressionState.reconcileBossPresence(containsBossEnemy(), nowMillis, progressionPolicy);
        if (previousPhase != chapterProgressionState.getGamePhase()) {
            if (chapterProgressionState.getGamePhase() == GamePhase.BRANCH_SELECTION) {
                openBranchSelectionForAlivePlayers();
            } else if (chapterProgressionState.getGamePhase() == GamePhase.UPGRADE_SELECTION) {
                openUpgradeSelectionForAlivePlayers();
            }
        }
        bossActive = chapterProgressionState.isBossEncounterActive();
        if (chapterProgressionState.shouldSpawnBoss(totalScore)) {
            spawnBoss();
        }
    }

    private void openBranchSelectionForAlivePlayers() {
        for (PlayerSession session : getAllPlayerSessions()) {
            if (!session.getPlayerState().getAircraft().notValid()) {
                session.getPlayerState().openBranchSelection(progressionPolicy.firstBossBranchChoices());
            }
        }
    }

    private void openUpgradeSelectionForAlivePlayers() {
        for (PlayerSession session : getAllPlayerSessions()) {
            if (!session.getPlayerState().getAircraft().notValid()) {
                session.getPlayerState().openUpgradeSelection();
            }
        }
    }

    public void registerEnemyDestroyed(PlayerSession session, AbstractAircraft enemyAircraft) {
        if (session != null) {
            session.getPlayerState().addScore(scoreForEnemy(enemyAircraft));
        }
        generateSupply(enemyAircraft);
    }

    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        for (LaserBeamState laser : activeLasers) {
            laser.advanceLifetime();
        }
        activeLasers.removeIf(this::shouldRemoveLaser);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        items.removeIf(AbstractFlyingObject::notValid);
        bossActive = containsBossEnemy();
    }

    private void burstAction(long nowMillis) {
        for (AirburstProjectileState projectile : airburstProjectiles) {
            ExplosionSnapshot explosion = projectile.resolveBurst();
            activeExplosionStates.add(new ActiveExplosionState(explosion));
            applyAirburstDamage(projectile, explosion, nowMillis);
        }
        airburstProjectiles.clear();
    }

    private void advanceExplosionAction() {
        for (ActiveExplosionState explosionState : activeExplosionStates) {
            explosionState.advanceLifetime();
        }
        activeExplosionStates.removeIf(ActiveExplosionState::isExpired);
    }

    private void spawnBoss() {
        enemyAircrafts.clear();
        enemyBullets.clear();
        activeLasers.clear();
        items.clear();
        enemyAircrafts.add(new BossEnemy(
                GameConstants.WINDOW_WIDTH / 2,
                120,
                GameplayBalance.BOSS_ENEMY_SPEED_X,
                GameplayBalance.BOSS_ENEMY_SPEED_Y,
                GameplayBalance.BOSS_ENEMY_HP
        ));
        chapterProgressionState.onBossSpawned(difficulty, progressionPolicy);
        bossActive = true;
    }

    private boolean containsBossEnemy() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (!enemyAircraft.notValid() && enemyAircraft instanceof BossEnemy) {
                return true;
            }
        }
        return false;
    }

    private int scoreForEnemy(AbstractAircraft enemyAircraft) {
        if (enemyAircraft instanceof BossEnemy) {
            return GameplayBalance.BOSS_ENEMY_SCORE;
        }
        if (enemyAircraft instanceof AceEnemy) {
            return GameplayBalance.ACE_ENEMY_SCORE;
        }
        if (enemyAircraft instanceof ElitePlusEnemy) {
            return GameplayBalance.ELITE_PLUS_ENEMY_SCORE;
        }
        if (enemyAircraft instanceof EliteEnemy) {
            return GameplayBalance.ELITE_ENEMY_SCORE;
        }
        return GameplayBalance.MOB_ENEMY_SCORE;
    }

    private double elitePlusEnemyProbability() {
        if (totalScore < GameplayBalance.ELITE_PLUS_UNLOCK_SCORE) {
            return 0.0;
        }
        double difficultyBonus = difficulty == Difficulty.HARD
                ? GameplayBalance.ELITE_PLUS_HARD_BONUS
                : difficulty == Difficulty.EASY ? GameplayBalance.ELITE_PLUS_EASY_BONUS : 0.0;
        return Math.max(
                0.0,
                Math.min(
                        GameplayBalance.ELITE_PLUS_MAX_PROBABILITY,
                        GameplayBalance.ELITE_PLUS_BASE_PROBABILITY
                                + totalScore / GameplayBalance.ELITE_PLUS_SCORE_DIVISOR
                                + difficultyBonus
                )
        );
    }

    private double aceEnemyProbability() {
        if (totalScore < GameplayBalance.ACE_UNLOCK_SCORE) {
            return 0.0;
        }
        double difficultyBonus = difficulty == Difficulty.HARD
                ? GameplayBalance.ACE_HARD_BONUS
                : difficulty == Difficulty.EASY ? GameplayBalance.ACE_EASY_BONUS : 0.0;
        return Math.max(
                0.0,
                Math.min(
                        GameplayBalance.ACE_MAX_PROBABILITY,
                        GameplayBalance.ACE_BASE_PROBABILITY
                                + totalScore / GameplayBalance.ACE_SCORE_DIVISOR
                                + difficultyBonus
                )
        );
    }

    private int nearestEnemyX(int fallbackX) {
        AbstractAircraft nearestEnemy = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft.notValid()) {
                continue;
            }
            int distance = Math.abs(enemyAircraft.getLocationX() - fallbackX);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestEnemy = enemyAircraft;
            }
        }
        return nearestEnemy == null ? fallbackX : nearestEnemy.getLocationX();
    }

    private AbstractAircraft nearestEnemy(PlayerRuntimeState playerState) {
        AbstractAircraft nearestEnemy = null;
        long nearestDistance = Long.MAX_VALUE;
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft.notValid()) {
                continue;
            }
            long deltaX = enemyAircraft.getLocationX() - playerState.getX();
            long deltaY = enemyAircraft.getLocationY() - playerState.getY();
            long distance = deltaX * deltaX + deltaY * deltaY;
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestEnemy = enemyAircraft;
            }
        }
        return nearestEnemy;
    }

    private void applyAirburstDamage(AirburstProjectileState projectile, ExplosionSnapshot explosion, long nowMillis) {
        int radius = projectile.getBurstRadius();
        int radiusSquared = radius * radius;
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft.notValid()) {
                continue;
            }
            long deltaX = enemyAircraft.getLocationX() - explosion.getX();
            long deltaY = enemyAircraft.getLocationY() - explosion.getY();
            long distanceSquared = deltaX * deltaX + deltaY * deltaY;
            if (distanceSquared <= radiusSquared) {
                enemyAircraft.decreaseHp(projectile.getDamage());
                if (enemyAircraft.notValid()) {
                    awardScore(projectile.getOwnerSessionId(), enemyAircraft);
                }
            }
        }
    }

    private void replaceActiveLaser(LaserBeamState laser) {
        activeLasers.removeIf(existing -> existing.getOwnerSessionId().equals(laser.getOwnerSessionId()));
        activeLasers.add(laser);
    }

    private boolean laserHitsEnemy(LaserBeamState laser, AbstractAircraft enemyAircraft) {
        double bodyLeft = enemyAircraft.getLocationX() - enemyAircraft.getWidth() / 2.0;
        double bodyRight = enemyAircraft.getLocationX() + enemyAircraft.getWidth() / 2.0;
        double bodyTop = enemyAircraft.getLocationY() - enemyAircraft.getHeight() / 4.0;
        double bodyBottom = enemyAircraft.getLocationY() + enemyAircraft.getHeight() / 4.0;
        double distance = distanceFromSegmentToBodyRectangle(
                laser.getOriginX(),
                laser.getOriginY(),
                laser.getEndX(),
                laser.getEndY(),
                bodyLeft,
                bodyTop,
                bodyRight,
                bodyBottom
        );
        return distance <= laser.getWidth() / 2.0;
    }

    private double distanceToSegment(double pointX,
                                     double pointY,
                                     double startX,
                                     double startY,
                                     double endX,
                                     double endY) {
        double deltaX = endX - startX;
        double deltaY = endY - startY;
        double lengthSquared = deltaX * deltaX + deltaY * deltaY;
        if (lengthSquared <= 0.0) {
            return Math.hypot(pointX - startX, pointY - startY);
        }
        double projection = ((pointX - startX) * deltaX + (pointY - startY) * deltaY) / lengthSquared;
        double clampedProjection = Math.max(0.0, Math.min(1.0, projection));
        double nearestX = startX + clampedProjection * deltaX;
        double nearestY = startY + clampedProjection * deltaY;
        return Math.hypot(pointX - nearestX, pointY - nearestY);
    }

    private double distanceFromSegmentToBodyRectangle(double startX,
                                                      double startY,
                                                      double endX,
                                                      double endY,
                                                      double left,
                                                      double top,
                                                      double right,
                                                      double bottom) {
        if (pointInRectangle(startX, startY, left, top, right, bottom)
                || pointInRectangle(endX, endY, left, top, right, bottom)
                || segmentsIntersect(startX, startY, endX, endY, left, top, right, top)
                || segmentsIntersect(startX, startY, endX, endY, right, top, right, bottom)
                || segmentsIntersect(startX, startY, endX, endY, right, bottom, left, bottom)
                || segmentsIntersect(startX, startY, endX, endY, left, bottom, left, top)) {
            return 0.0;
        }

        double minDistance = Math.min(
                pointToRectangleDistance(startX, startY, left, top, right, bottom),
                pointToRectangleDistance(endX, endY, left, top, right, bottom)
        );
        minDistance = Math.min(minDistance, distanceToSegment(left, top, startX, startY, endX, endY));
        minDistance = Math.min(minDistance, distanceToSegment(right, top, startX, startY, endX, endY));
        minDistance = Math.min(minDistance, distanceToSegment(right, bottom, startX, startY, endX, endY));
        minDistance = Math.min(minDistance, distanceToSegment(left, bottom, startX, startY, endX, endY));
        return minDistance;
    }

    private double pointToRectangleDistance(double pointX,
                                            double pointY,
                                            double left,
                                            double top,
                                            double right,
                                            double bottom) {
        double nearestX = Math.max(left, Math.min(right, pointX));
        double nearestY = Math.max(top, Math.min(bottom, pointY));
        return Math.hypot(pointX - nearestX, pointY - nearestY);
    }

    private boolean pointInRectangle(double pointX,
                                     double pointY,
                                     double left,
                                     double top,
                                     double right,
                                     double bottom) {
        return pointX >= left && pointX <= right && pointY >= top && pointY <= bottom;
    }

    private boolean segmentsIntersect(double ax,
                                      double ay,
                                      double bx,
                                      double by,
                                      double cx,
                                      double cy,
                                      double dx,
                                      double dy) {
        double ab1 = cross(ax, ay, bx, by, cx, cy);
        double ab2 = cross(ax, ay, bx, by, dx, dy);
        double cd1 = cross(cx, cy, dx, dy, ax, ay);
        double cd2 = cross(cx, cy, dx, dy, bx, by);

        if (ab1 == 0.0 && onSegment(ax, ay, bx, by, cx, cy)) {
            return true;
        }
        if (ab2 == 0.0 && onSegment(ax, ay, bx, by, dx, dy)) {
            return true;
        }
        if (cd1 == 0.0 && onSegment(cx, cy, dx, dy, ax, ay)) {
            return true;
        }
        if (cd2 == 0.0 && onSegment(cx, cy, dx, dy, bx, by)) {
            return true;
        }
        return (ab1 > 0.0) != (ab2 > 0.0) && (cd1 > 0.0) != (cd2 > 0.0);
    }

    private double cross(double ax,
                         double ay,
                         double bx,
                         double by,
                         double px,
                         double py) {
        return (bx - ax) * (py - ay) - (by - ay) * (px - ax);
    }

    private boolean onSegment(double ax,
                              double ay,
                              double bx,
                              double by,
                              double px,
                              double py) {
        return px >= Math.min(ax, bx)
                && px <= Math.max(ax, bx)
                && py >= Math.min(ay, by)
                && py <= Math.max(ay, by);
    }

    private boolean shouldRemoveLaser(LaserBeamState laser) {
        if (laser.isExpired()) {
            return true;
        }
        PlayerSession owner = sessionRegistry.find(laser.getOwnerSessionId());
        return owner == null || owner.getPlayerState().getAircraft().notValid();
    }
}
