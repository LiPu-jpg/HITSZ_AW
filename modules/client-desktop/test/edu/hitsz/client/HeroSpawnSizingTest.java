package edu.hitsz.client;

import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.common.EntityRenderSizing;
import edu.hitsz.common.GameConstants;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

public class HeroSpawnSizingTest {

    public static void main(String[] args) throws Exception {
        heroSpawnHeightShouldUseConfiguredSizing();
    }

    private static void heroSpawnHeightShouldUseConfiguredSizing() throws Exception {
        BufferedImage originalHeroImage = ImageManager.HERO_IMAGE;
        ImageManager.HERO_IMAGE = new BufferedImage(8, 444, BufferedImage.TYPE_INT_ARGB);
        try {
            resetSingleton();
            HeroAircraft heroAircraft = HeroAircraft.getSingleton();
            assert heroAircraft.getLocationY() == GameConstants.WINDOW_HEIGHT - EntityRenderSizing.HERO_HEIGHT
                    : "Hero spawn Y should use configured render sizing, not image height";
        } finally {
            resetSingleton();
            ImageManager.HERO_IMAGE = originalHeroImage;
        }
    }

    private static void resetSingleton() throws Exception {
        Field singletonField = HeroAircraft.class.getDeclaredField("singleton");
        singletonField.setAccessible(true);
        singletonField.set(null, null);
    }
}
