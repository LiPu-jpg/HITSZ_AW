package edu.hitsz.client;

public class GameAudioStateMachineTest {

    public static void main(String[] args) {
        switchesLoopTrackForBossPhase();
        emitsExplosionAndGameOverCuesOnce();
    }

    private static void switchesLoopTrackForBossPhase() {
        GameAudioStateMachine machine = new GameAudioStateMachine();

        AudioSnapshotDecision idle = machine.onSnapshot(false, false, 1000, 0);
        assert idle.getLoopTrack() == null : "No battle BGM should play before the game starts";

        AudioSnapshotDecision battle = machine.onSnapshot(true, false, 1000, 0);
        assert "bgm.wav".equals(battle.getLoopTrack())
                : "Battle phase should select the standard BGM";

        AudioSnapshotDecision boss = machine.onSnapshot(true, true, 1000, 0);
        assert "bgm_boss.wav".equals(boss.getLoopTrack())
                : "Boss phase should switch to the boss BGM";
    }

    private static void emitsExplosionAndGameOverCuesOnce() {
        GameAudioStateMachine machine = new GameAudioStateMachine();
        machine.onSnapshot(true, false, 1000, 0);

        AudioSnapshotDecision explosion = machine.onSnapshot(true, false, 1000, 1);
        assert explosion.shouldPlayExplosion()
                : "A newly observed explosion should trigger an explosion cue";
        assert !explosion.shouldPlayGameOver()
                : "Explosion cue should not imply game over";

        AudioSnapshotDecision gameOver = machine.onSnapshot(true, false, 0, 1);
        assert gameOver.shouldPlayGameOver()
                : "Dropping local HP to zero should emit the game-over cue";

        AudioSnapshotDecision repeatedGameOver = machine.onSnapshot(true, false, 0, 1);
        assert !repeatedGameOver.shouldPlayGameOver()
                : "Game-over cue should not repeat while already dead";
    }
}
