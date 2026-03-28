package edu.hitsz.common.protocol.dto;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerSnapshot {

    private static final int LEGACY_DEFAULT_MAX_HP = 1000;

    private final String sessionId;
    private final String playerId;
    private final int x;
    private final int y;
    private final int hp;
    private final int score;
    private final boolean ready;
    private final int level;
    private final String selectedSkill;
    private final long skillCooldownRemainingMillis;
    private final long skillCooldownTotalMillis;
    private final int maxHp;
    private final List<BranchUpgradeChoice> availableUpgradeChoices;
    private final BranchUpgradeChoice selectedUpgradeChoice;
    private final AircraftBranch aircraftBranch;
    private final List<AircraftBranch> availableBranchChoices;
    private final boolean branchUnlocked;

    public PlayerSnapshot(String sessionId, String playerId, int x, int y, int hp, int score) {
        this(sessionId, playerId, x, y, hp, score, false, 1, null, 0L, 0L, LEGACY_DEFAULT_MAX_HP, Collections.emptyList(), null, null, Collections.emptyList(), false);
    }

    public PlayerSnapshot(String sessionId, String playerId, int x, int y, int hp, int score, boolean ready) {
        this(sessionId, playerId, x, y, hp, score, ready, 1, null, 0L, 0L, LEGACY_DEFAULT_MAX_HP, Collections.emptyList(), null, null, Collections.emptyList(), false);
    }

    public PlayerSnapshot(
            String sessionId,
            String playerId,
            int x,
            int y,
            int hp,
            int score,
            boolean ready,
            int level,
            String selectedSkill
    ) {
        this(sessionId, playerId, x, y, hp, score, ready, level, selectedSkill, 0L, 0L, LEGACY_DEFAULT_MAX_HP, Collections.emptyList(), null, null, Collections.emptyList(), false);
    }

    public PlayerSnapshot(
            String sessionId,
            String playerId,
            int x,
            int y,
            int hp,
            int score,
            boolean ready,
            int level,
            String selectedSkill,
            long skillCooldownRemainingMillis
    ) {
        this(sessionId, playerId, x, y, hp, score, ready, level, selectedSkill, skillCooldownRemainingMillis, 0L, LEGACY_DEFAULT_MAX_HP, Collections.emptyList(), null, null, Collections.emptyList(), false);
    }

    public PlayerSnapshot(
            String sessionId,
            String playerId,
            int x,
            int y,
            int hp,
            int score,
            boolean ready,
            int level,
            String selectedSkill,
            long skillCooldownRemainingMillis,
            long skillCooldownTotalMillis
    ) {
        this(sessionId, playerId, x, y, hp, score, ready, level, selectedSkill, skillCooldownRemainingMillis, skillCooldownTotalMillis, LEGACY_DEFAULT_MAX_HP, Collections.emptyList(), null, null, Collections.emptyList(), false);
    }

    public PlayerSnapshot(
            String sessionId,
            String playerId,
            int x,
            int y,
            int hp,
            int score,
            boolean ready,
            int level,
            String selectedSkill,
            long skillCooldownRemainingMillis,
            long skillCooldownTotalMillis,
            int maxHp,
            List<BranchUpgradeChoice> availableUpgradeChoices,
            BranchUpgradeChoice selectedUpgradeChoice
    ) {
        this(
                sessionId,
                playerId,
                x,
                y,
                hp,
                score,
                ready,
                level,
                selectedSkill,
                skillCooldownRemainingMillis,
                skillCooldownTotalMillis,
                maxHp,
                availableUpgradeChoices,
                selectedUpgradeChoice,
                null,
                Collections.emptyList(),
                false
        );
    }

    public PlayerSnapshot(
            String sessionId,
            String playerId,
            int x,
            int y,
            int hp,
            int score,
            boolean ready,
            int level,
            String selectedSkill,
            long skillCooldownRemainingMillis,
            int maxHp,
            List<BranchUpgradeChoice> availableUpgradeChoices,
            BranchUpgradeChoice selectedUpgradeChoice
    ) {
        this(
                sessionId,
                playerId,
                x,
                y,
                hp,
                score,
                ready,
                level,
                selectedSkill,
                skillCooldownRemainingMillis,
                0L,
                maxHp,
                availableUpgradeChoices,
                selectedUpgradeChoice
        );
    }

    public PlayerSnapshot(
            String sessionId,
            String playerId,
            int x,
            int y,
            int hp,
            int score,
            boolean ready,
            int level,
            String selectedSkill,
            long skillCooldownRemainingMillis,
            long skillCooldownTotalMillis,
            int maxHp,
            List<BranchUpgradeChoice> availableUpgradeChoices,
            BranchUpgradeChoice selectedUpgradeChoice,
            AircraftBranch aircraftBranch,
            List<AircraftBranch> availableBranchChoices,
            boolean branchUnlocked
    ) {
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.score = score;
        this.ready = ready;
        this.level = level;
        this.selectedSkill = selectedSkill;
        this.skillCooldownRemainingMillis = skillCooldownRemainingMillis;
        this.skillCooldownTotalMillis = skillCooldownTotalMillis;
        this.maxHp = maxHp;
        List<BranchUpgradeChoice> normalizedChoices = availableUpgradeChoices == null
                ? Collections.emptyList()
                : availableUpgradeChoices;
        this.availableUpgradeChoices = Collections.unmodifiableList(new ArrayList<>(normalizedChoices));
        this.selectedUpgradeChoice = selectedUpgradeChoice;
        this.aircraftBranch = aircraftBranch;
        List<AircraftBranch> normalizedBranchChoices = availableBranchChoices == null
                ? Collections.emptyList()
                : availableBranchChoices;
        this.availableBranchChoices = Collections.unmodifiableList(new ArrayList<>(normalizedBranchChoices));
        this.branchUnlocked = branchUnlocked;
    }

    public PlayerSnapshot(
            String sessionId,
            String playerId,
            int x,
            int y,
            int hp,
            int score,
            boolean ready,
            int level,
            String selectedSkill,
            long skillCooldownRemainingMillis,
            int maxHp,
            List<BranchUpgradeChoice> availableUpgradeChoices,
            BranchUpgradeChoice selectedUpgradeChoice,
            AircraftBranch aircraftBranch,
            List<AircraftBranch> availableBranchChoices,
            boolean branchUnlocked
    ) {
        this(
                sessionId,
                playerId,
                x,
                y,
                hp,
                score,
                ready,
                level,
                selectedSkill,
                skillCooldownRemainingMillis,
                0L,
                maxHp,
                availableUpgradeChoices,
                selectedUpgradeChoice,
                aircraftBranch,
                availableBranchChoices,
                branchUnlocked
        );
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHp() {
        return hp;
    }

    public int getScore() {
        return score;
    }

    public boolean isReady() {
        return ready;
    }

    public int getLevel() {
        return level;
    }

    public String getSelectedSkill() {
        return selectedSkill;
    }

    public long getSkillCooldownRemainingMillis() {
        return skillCooldownRemainingMillis;
    }

    public long getSkillCooldownTotalMillis() {
        return skillCooldownTotalMillis;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public List<BranchUpgradeChoice> getAvailableUpgradeChoices() {
        return availableUpgradeChoices;
    }

    public BranchUpgradeChoice getSelectedUpgradeChoice() {
        return selectedUpgradeChoice;
    }

    public AircraftBranch getAircraftBranch() {
        return aircraftBranch;
    }

    public List<AircraftBranch> getAvailableBranchChoices() {
        return availableBranchChoices;
    }

    public boolean isBranchUnlocked() {
        return branchUnlocked;
    }
}
