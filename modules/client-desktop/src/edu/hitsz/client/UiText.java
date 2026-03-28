package edu.hitsz.client;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.ChapterId;

public final class UiText {

    private UiText() {
    }

    public static String chapterLabel(ChapterId chapterId) {
        int chapterIndex = 1;
        if (chapterId != null) {
            chapterIndex = chapterId.ordinal() + 1;
        }
        return "第" + chapterIndex + "关 / 共5关";
    }

    public static String difficultyLabel(String difficulty) {
        if ("EASY".equalsIgnoreCase(difficulty)) {
            return "简单";
        }
        if ("HARD".equalsIgnoreCase(difficulty)) {
            return "困难";
        }
        return "普通";
    }

    public static String entryModeLabel(String entryMode) {
        return "JOIN".equalsIgnoreCase(entryMode) ? "加入房间" : "创建房间";
    }

    public static String branchLabel(AircraftBranch branch) {
        if (branch == null) {
            return "-";
        }
        switch (branch) {
            case RED_SPEED:
                return "红色速度机";
            case GREEN_DEFENSE:
                return "绿色防御机";
            case BLACK_HEAVY:
                return "黑色重轰机";
            case STARTER_BLUE:
            default:
                return "初始蓝机";
        }
    }

    public static String skillLabel(String skillType) {
        if (skillType == null || skillType.trim().isEmpty()) {
            return "-";
        }
        switch (skillType.trim().toUpperCase()) {
            case "FREEZE":
                return "冻结";
            case "BOMB":
                return "爆炸";
            case "SHIELD":
                return "护盾";
            default:
                return skillType;
        }
    }

    public static String upgradeChoiceLabel(BranchUpgradeChoice choice) {
        switch (choice) {
            case LASER_DAMAGE:
                return "激光伤害提升";
            case LASER_WIDTH:
                return "激光宽度提升";
            case LASER_DURATION:
                return "激光持续时间提升";
            case MOVE_SPEED:
                return "移动速度提升";
            case SPREAD_COUNT:
                return "散射弹数量提升";
            case SPREAD_WIDTH:
                return "散射角度提升";
            case BULLET_DAMAGE:
                return "子弹伤害提升";
            case MAX_HP:
                return "生命上限提升";
            case AIRBURST_DAMAGE:
                return "空爆伤害提升";
            case AIRBURST_RADIUS:
                return "空爆范围提升";
            case AIRBURST_RANGE:
                return "空爆射程提升";
            default:
                return choice == null ? "-" : choice.name();
        }
    }
}
