package edu.hitsz.client;

public class ClientImageClasspathLoadingTest {

    public static void main(String[] args) {
        assert ImageManager.BACKGROUND_IMAGE != null : "Background image should load from runtime resources";
        assert ImageManager.HERO_IMAGE != null : "Hero image should load from runtime resources";
        assert ImageManager.STARTER_BLUE_IMAGE != null : "Starter-blue image should load from runtime resources";
        assert ImageManager.RED_SPEED_IMAGE != null : "Red-speed image should load from runtime resources";
        assert ImageManager.GREEN_DEFENSE_IMAGE != null : "Green-defense image should load from runtime resources";
        assert ImageManager.BLACK_HEAVY_IMAGE != null : "Black-heavy image should load from runtime resources";
    }
}
