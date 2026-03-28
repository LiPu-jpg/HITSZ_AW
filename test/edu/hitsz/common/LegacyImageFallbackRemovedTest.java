package edu.hitsz.common;

public class LegacyImageFallbackRemovedTest {

    public static void main(String[] args) {
        legacyRootImageNamesAreNoLongerLoadable();
    }

    private static void legacyRootImageNamesAreNoLongerLoadable() {
        boolean threw = false;
        try {
            ImageResourceLoader.load("hero.png");
        } catch (IllegalStateException expected) {
            threw = true;
        }
        assert threw : "Legacy root image names should no longer be loadable once final assets are enforced";
    }
}
