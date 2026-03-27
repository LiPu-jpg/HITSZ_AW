package edu.hitsz.server;

public class ServerImageClasspathLoadingTest {

    public static void main(String[] args) {
        assert ServerImageManager.HERO_IMAGE != null : "Server hero image metadata should load from runtime resources";
        assert ServerImageManager.MOB_ENEMY_IMAGE != null : "Server enemy image metadata should load from runtime resources";
    }
}
