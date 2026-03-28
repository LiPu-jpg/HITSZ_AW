package edu.hitsz.client;

import edu.hitsz.common.AudioResourceLoader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;

final class ClipAudioPlaybackBackend implements AudioPlaybackBackend {

    private Clip loopClip;
    private String currentLoopTrack;

    @Override
    public synchronized void apply(AudioSnapshotDecision decision) {
        setLoopTrack(decision.getLoopTrack());
        if (decision.shouldPlayExplosion()) {
            playOnce("bomb_explosion.wav");
        }
        if (decision.shouldPlayBulletHit()) {
            playOnce("bullet_hit.wav");
        }
        if (decision.shouldPlaySupplyPickup()) {
            playOnce("get_supply.wav");
        }
        if (decision.shouldPlayGameOver()) {
            playOnce("game_over.wav");
        }
    }

    @Override
    public synchronized void shutdown() {
        stopLoop();
    }

    private void setLoopTrack(String trackName) {
        if (trackName == null || trackName.isEmpty()) {
            stopLoop();
            return;
        }
        if (trackName.equals(currentLoopTrack) && loopClip != null && loopClip.isActive()) {
            return;
        }
        stopLoop();
        Clip clip = openClip(trackName);
        if (clip == null) {
            return;
        }
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        loopClip = clip;
        currentLoopTrack = trackName;
    }

    private void playOnce(String trackName) {
        Clip clip = openClip(trackName);
        if (clip == null) {
            return;
        }
        clip.addLineListener(event -> {
            if (javax.sound.sampled.LineEvent.Type.STOP.equals(event.getType())) {
                clip.close();
            }
        });
        clip.start();
    }

    private void stopLoop() {
        if (loopClip != null) {
            loopClip.stop();
            loopClip.close();
            loopClip = null;
        }
        currentLoopTrack = null;
    }

    private Clip openClip(String trackName) {
        if (!AudioResourceLoader.resourceExists(trackName)) {
            return null;
        }
        try (AudioInputStream inputStream = AudioResourceLoader.openAudioStream(trackName)) {
            Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(inputStream);
            return clip;
        } catch (LineUnavailableException | IOException | IllegalStateException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
