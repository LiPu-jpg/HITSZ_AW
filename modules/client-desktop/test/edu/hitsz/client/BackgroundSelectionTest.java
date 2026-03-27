package edu.hitsz.client;

import edu.hitsz.common.Difficulty;

public class BackgroundSelectionTest {

    public static void main(String[] args) {
        assert ImageManager.EASY_BACKGROUND_IMAGE != null : "Easy background image should load";
        assert ImageManager.NORMAL_BACKGROUND_IMAGE != null : "Normal background image should load";
        assert ImageManager.HARD_BACKGROUND_IMAGE != null : "Hard background image should load";
        assert ImageManager.BOSS_BACKGROUND_IMAGE != null : "Boss background image should load";
        assert ImageManager.backgroundFor(Difficulty.EASY.name(), false) == ImageManager.EASY_BACKGROUND_IMAGE
                : "Easy difficulty should use the easy battle background";
        assert ImageManager.backgroundFor(Difficulty.NORMAL.name(), false) == ImageManager.NORMAL_BACKGROUND_IMAGE
                : "Normal difficulty should use the normal battle background";
        assert ImageManager.backgroundFor(Difficulty.HARD.name(), false) == ImageManager.HARD_BACKGROUND_IMAGE
                : "Hard difficulty should use the hard battle background";
        assert ImageManager.backgroundFor(Difficulty.NORMAL.name(), true) == ImageManager.BOSS_BACKGROUND_IMAGE
                : "Boss phase should override difficulty background";
    }
}
