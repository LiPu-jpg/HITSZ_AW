package edu.hitsz.client;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.ChapterId;

public class UiTextTest {

    public static void main(String[] args) {
        chapterLabelUsesChineseAndFiveStageCount();
        branchAndDifficultyLabelsUseChinese();
        upgradeLabelsUseChinese();
    }

    private static void chapterLabelUsesChineseAndFiveStageCount() {
        assert "第1关 / 共5关".equals(UiText.chapterLabel(ChapterId.CH1))
                : "CH1 should render as the first of five stages";
        assert "第5关 / 共5关".equals(UiText.chapterLabel(ChapterId.CH5))
                : "CH5 should render as the terminal stage label";
    }

    private static void branchAndDifficultyLabelsUseChinese() {
        assert "初始蓝机".equals(UiText.branchLabel(AircraftBranch.STARTER_BLUE))
                : "Starter branch should be rendered in Chinese";
        assert "红色速度机".equals(UiText.branchLabel(AircraftBranch.RED_SPEED))
                : "Red-speed branch should be rendered in Chinese";
        assert "简单".equals(UiText.difficultyLabel("EASY"))
                : "Easy difficulty should be localized";
        assert "普通".equals(UiText.difficultyLabel("NORMAL"))
                : "Normal difficulty should be localized";
        assert "困难".equals(UiText.difficultyLabel("HARD"))
                : "Hard difficulty should be localized";
    }

    private static void upgradeLabelsUseChinese() {
        assert "激光伤害提升".equals(UiText.upgradeChoiceLabel(BranchUpgradeChoice.LASER_DAMAGE))
                : "Laser upgrade text should be localized";
        assert "空爆范围提升".equals(UiText.upgradeChoiceLabel(BranchUpgradeChoice.AIRBURST_RADIUS))
                : "Airburst upgrade text should be localized";
    }
}
