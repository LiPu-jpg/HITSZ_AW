package edu.hitsz.client;

import edu.hitsz.common.ChapterId;

public class ChapterBackgroundSelectionTest {

    public static void main(String[] args) {
        assert ImageManager.backgroundFor(ChapterId.CH1, false) != null : "CH1 should resolve a background";
        assert ImageManager.backgroundFor(ChapterId.CH2, false) != null : "CH2 should resolve a background";
        assert ImageManager.backgroundFor(ChapterId.CH3, false) != null : "CH3 should resolve a background";

        assert ImageManager.backgroundFor(ChapterId.CH1, false) != ImageManager.backgroundFor(ChapterId.CH2, false)
                : "Different chapters should resolve different background images";
        assert ImageManager.backgroundFor(ChapterId.CH2, false) != ImageManager.backgroundFor(ChapterId.CH3, false)
                : "Different chapters should resolve different background images";
        assert ImageManager.backgroundFor(ChapterId.CH1, true) == ImageManager.BOSS_BACKGROUND_IMAGE
                : "Boss phase should override the chapter background";
    }
}
