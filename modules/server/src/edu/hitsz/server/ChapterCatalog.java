package edu.hitsz.server;

import edu.hitsz.common.ChapterId;

public final class ChapterCatalog {

    public ChapterId initialChapter() {
        return ChapterId.CH1;
    }

    public ChapterId nextChapter(ChapterId chapterId) {
        if (chapterId == null) {
            return null;
        }
        switch (chapterId) {
            case CH1:
                return ChapterId.CH2;
            case CH2:
                return ChapterId.CH3;
            case CH3:
                return ChapterId.CH4;
            case CH4:
                return ChapterId.CH5;
            default:
                return null;
        }
    }

    public boolean hasNextChapter(ChapterId chapterId) {
        return nextChapter(chapterId) != null;
    }
}
