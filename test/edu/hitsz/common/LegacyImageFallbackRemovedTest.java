package edu.hitsz.common;

public class LegacyImageFallbackRemovedTest {

    public static void main(String[] args) {
        legacyRootImageNamesAreNoLongerLoadable();
        finalAssetsAreLoadedFromImagesRoot();
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

    private static void finalAssetsAreLoadedFromImagesRoot() {
        assert ImageResourceLoader.load("初始飞机.png") != null
                : "Final assets should now be loaded directly from the images root";
    }
}
