package edu.hitsz.common;

public class AudioResourceSourceLoadingTest {

    public static void main(String[] args) {
        assert AudioResourceLoader.resourceExists("bgm.wav")
                : "bgm.wav should be discoverable from the unified audio resource roots";
        assert AudioResourceLoader.resourceExists("bgm_boss.wav")
                : "bgm_boss.wav should be discoverable from the unified audio resource roots";
        assert AudioResourceLoader.resourceExists("game_over.wav")
                : "game_over.wav should be discoverable from the unified audio resource roots";
    }
}
