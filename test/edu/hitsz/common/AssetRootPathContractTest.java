package edu.hitsz.common;

public class AssetRootPathContractTest {

    public static void main(String[] args) {
        imageErrorMessagePointsToAssetsRoot();
        audioErrorMessagePointsToAssetsRoot();
    }

    private static void imageErrorMessagePointsToAssetsRoot() {
        boolean mentionsAssetsRoot = false;
        try {
            ImageResourceLoader.load("__missing_image__.png");
        } catch (IllegalStateException expected) {
            mentionsAssetsRoot = expected.getMessage().contains("src/assets/images");
        }
        assert mentionsAssetsRoot : "Image loader errors should point to src/assets/images";
    }

    private static void audioErrorMessagePointsToAssetsRoot() {
        boolean mentionsAssetsRoot = false;
        try {
            AudioResourceLoader.openAudioStream("__missing_audio__.wav");
        } catch (IllegalStateException expected) {
            mentionsAssetsRoot = expected.getMessage().contains("src/assets/audio");
        }
        assert mentionsAssetsRoot : "Audio loader errors should point to src/assets/audio";
    }
}
