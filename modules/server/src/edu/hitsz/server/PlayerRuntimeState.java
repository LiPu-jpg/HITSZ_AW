package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.GameConstants;
import edu.hitsz.common.UpgradeChoice;
import edu.hitsz.server.skill.PlayerSkillState;
import edu.hitsz.server.skill.SkillType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlayerRuntimeState {

    private static final int INITIAL_LEVEL = 1;

    private final String playerId;
    private final PlayerSkillState skillState;
    private final ServerPlayerAircraft aircraft;
    private int level;
    private int score;
    private AircraftBranch aircraftBranch;
    private int targetX;
    private int targetY;
    private boolean branchUnlocked;
    private String selectedSkill;
    private List<AircraftBranch> availableBranchChoices;
    private List<UpgradeChoice> availableUpgradeChoices;
    private UpgradeChoice selectedUpgradeChoice;
    private int fireRateUpgradeLevel;
    private int bulletPowerUpgradeLevel;
    private int spreadShotUpgradeLevel;
    private int lightTrackingUpgradeLevel;
    private long lastPlayerShotTick = -1L;

    public PlayerRuntimeState(String playerId) {
        this.playerId = playerId;
        this.skillState = new PlayerSkillState();
        this.aircraft = new ServerPlayerAircraft(0, 0, 0, 0, GameplayBalance.PLAYER_INITIAL_HP);
        this.level = INITIAL_LEVEL;
        this.aircraftBranch = AircraftBranch.STARTER_BLUE;
        this.targetX = aircraft.getLocationX();
        this.targetY = aircraft.getLocationY();
        this.branchUnlocked = false;
        this.selectedSkill = null;
        this.availableBranchChoices = Collections.emptyList();
        this.availableUpgradeChoices = Collections.emptyList();
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getX() {
        return aircraft.getLocationX();
    }

    public int getY() {
        return aircraft.getLocationY();
    }

    public void setPosition(int x, int y) {
        int clampedX = clampX(x);
        int clampedY = clampY(y);
        aircraft.setLocation(clampedX, clampedY);
        setTargetPosition(clampedX, clampedY);
    }

    public void setTargetPosition(int x, int y) {
        this.targetX = clampX(x);
        this.targetY = clampY(y);
    }

    public int getTargetX() {
        return targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public int getHp() {
        return aircraft.getHp();
    }

    public void setHp(int hp) {
        aircraft.setHp(hp);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void decreaseHp(int damage) {
        aircraft.decreaseHp(damage);
    }

    public PlayerSkillState getSkillState() {
        return skillState;
    }

    public void increaseHp(int amount) {
        aircraft.increaseHp(amount);
    }

    public void increaseFirepower(int amount) {
        aircraft.increaseFirepower(amount);
    }

    public ServerPlayerAircraft getAircraft() {
        return aircraft;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int delta) {
        this.score += delta;
    }

    public String getSelectedSkill() {
        return selectedSkill;
    }

    public void setSelectedSkill(String selectedSkill) {
        this.selectedSkill = normalizeSelectedSkill(selectedSkill);
    }

    public AircraftBranch getAircraftBranch() {
        return aircraftBranch;
    }

    public boolean isBranchUnlocked() {
        return branchUnlocked;
    }

    public List<AircraftBranch> getAvailableBranchChoices() {
        return availableBranchChoices;
    }

    public void resetForNewRound(int x, int y) {
        aircraft.resetForRound(x, y);
        skillState.reset();
        level = INITIAL_LEVEL;
        score = 0;
        aircraftBranch = AircraftBranch.STARTER_BLUE;
        targetX = x;
        targetY = y;
        branchUnlocked = false;
        selectedSkill = null;
        availableBranchChoices = Collections.emptyList();
        availableUpgradeChoices = Collections.emptyList();
        selectedUpgradeChoice = null;
        fireRateUpgradeLevel = 0;
        bulletPowerUpgradeLevel = 0;
        spreadShotUpgradeLevel = 0;
        lightTrackingUpgradeLevel = 0;
        lastPlayerShotTick = -1L;
    }

    public void syncProgression(ProgressionPolicy policy) {
        int targetLevel = policy.levelForScore(score);
        if (targetLevel > level) {
            aircraft.increaseFirepower(targetLevel - level);
            level = targetLevel;
        }
    }

    public void openUpgradeSelection() {
        availableUpgradeChoices = Collections.unmodifiableList(Arrays.asList(UpgradeChoice.values()));
        selectedUpgradeChoice = null;
    }

    public void openBranchSelection(List<AircraftBranch> branchChoices) {
        if (branchUnlocked) {
            availableBranchChoices = Collections.emptyList();
            return;
        }
        List<AircraftBranch> normalizedChoices = branchChoices == null
                ? Collections.<AircraftBranch>emptyList()
                : branchChoices;
        availableBranchChoices = Collections.unmodifiableList(Arrays.asList(
                normalizedChoices.toArray(new AircraftBranch[normalizedChoices.size()])
        ));
    }

    public void clearUpgradeSelectionState() {
        availableUpgradeChoices = Collections.emptyList();
    }

    public void clearBranchSelectionState() {
        availableBranchChoices = Collections.emptyList();
    }

    public boolean hasPendingUpgradeChoice() {
        return !availableUpgradeChoices.isEmpty() && selectedUpgradeChoice == null;
    }

    public boolean hasPendingBranchChoice() {
        return !branchUnlocked && !availableBranchChoices.isEmpty();
    }

    public void applyBranchChoice(AircraftBranch branch) {
        if (branch == null
                || branch == AircraftBranch.STARTER_BLUE
                || branchUnlocked
                || !availableBranchChoices.contains(branch)) {
            return;
        }
        aircraftBranch = branch;
        branchUnlocked = true;
        selectedSkill = null;
        availableBranchChoices = Collections.emptyList();
    }

    public void applyUpgradeChoice(UpgradeChoice choice) {
        if (choice == null || !availableUpgradeChoices.contains(choice) || selectedUpgradeChoice != null) {
            return;
        }
        switch (choice) {
            case FIRE_RATE:
                fireRateUpgradeLevel++;
                break;
            case BULLET_POWER:
                bulletPowerUpgradeLevel++;
                aircraft.increaseBulletPower(GameplayBalance.PLAYER_BULLET_POWER_UPGRADE_BONUS);
                break;
            case SPREAD_SHOT:
                spreadShotUpgradeLevel++;
                aircraft.increaseShootNum(GameplayBalance.PLAYER_SPREAD_SHOT_UPGRADE_BONUS);
                break;
            case LIGHT_TRACKING:
                lightTrackingUpgradeLevel++;
                break;
            default:
                return;
        }
        selectedUpgradeChoice = choice;
    }

    public List<UpgradeChoice> getAvailableUpgradeChoices() {
        return availableUpgradeChoices;
    }

    public UpgradeChoice getSelectedUpgradeChoice() {
        return selectedUpgradeChoice;
    }

    public int getFireRateUpgradeLevel() {
        return fireRateUpgradeLevel;
    }

    public int getBulletPowerUpgradeLevel() {
        return bulletPowerUpgradeLevel;
    }

    public int getSpreadShotUpgradeLevel() {
        return spreadShotUpgradeLevel;
    }

    public int getLightTrackingUpgradeLevel() {
        return lightTrackingUpgradeLevel;
    }

    public int getPlayerShootCycle() {
        return Math.max(
                GameplayBalance.PLAYER_MIN_SHOOT_CYCLE,
                GameplayBalance.PLAYER_BASE_SHOOT_CYCLE
                        - fireRateUpgradeLevel * GameplayBalance.PLAYER_FIRE_RATE_UPGRADE_CYCLE_REDUCTION
        );
    }

    public boolean shouldShootAtTick(long tick) {
        if (lastPlayerShotTick < 0L) {
            return true;
        }
        return tick - lastPlayerShotTick >= getPlayerShootCycle();
    }

    public void markShotAtTick(long tick) {
        lastPlayerShotTick = tick;
    }

    public int trackingSpeedXForTarget(int targetX) {
        if (lightTrackingUpgradeLevel <= 0) {
            return 0;
        }
        int deltaX = targetX - getX();
        if (deltaX == 0) {
            return 0;
        }
        int maxTracking = Math.min(
                GameplayBalance.PLAYER_LIGHT_TRACKING_MAX_SPEED,
                lightTrackingUpgradeLevel * GameplayBalance.PLAYER_LIGHT_TRACKING_SPEED_STEP
        );
        return deltaX > 0 ? maxTracking : -maxTracking;
    }

    public void advanceTowardTarget() {
        int currentX = getX();
        int currentY = getY();
        int deltaX = targetX - currentX;
        int deltaY = targetY - currentY;
        if (Math.abs(deltaX) <= GameplayBalance.PLAYER_STOP_RADIUS
                && Math.abs(deltaY) <= GameplayBalance.PLAYER_STOP_RADIUS) {
            return;
        }

        int moveSpeed = GameplayBalance.playerMoveSpeed(aircraftBranch);
        int stepX = clamp(deltaX, moveSpeed);
        int stepY = clamp(deltaY, moveSpeed);
        aircraft.setLocation(clampX(currentX + stepX), clampY(currentY + stepY));
    }

    private int clamp(int delta, int limit) {
        if (delta > limit) {
            return limit;
        }
        if (delta < -limit) {
            return -limit;
        }
        return delta;
    }

    private int clampX(int x) {
        int halfWidth = aircraft.getWidth() / 2;
        return Math.max(halfWidth, Math.min(GameConstants.WINDOW_WIDTH - halfWidth, x));
    }

    private int clampY(int y) {
        int halfHeight = aircraft.getHeight() / 2;
        return Math.max(halfHeight, Math.min(GameConstants.WINDOW_HEIGHT - halfHeight, y));
    }

    private String normalizeSelectedSkill(String selectedSkill) {
        if (selectedSkill == null) {
            return null;
        }
        try {
            return SkillType.valueOf(selectedSkill).name();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
