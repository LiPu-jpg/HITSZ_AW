package edu.hitsz.client;

public class ClientImageClasspathLoadingTest {

    public static void main(String[] args) {
        assert ImageManager.BACKGROUND_IMAGE != null : "Background image should load from runtime resources";
        assert ImageManager.HERO_IMAGE != null : "Hero image should load from runtime resources";
    }
}
