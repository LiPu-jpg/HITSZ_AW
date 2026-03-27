package edu.hitsz.client;

public interface ClientCommandPublisher {

    void start();

    void stop();

    void publishCreateRoom(String difficulty, String selectedSkill);

    default void publishCreateRoom(String difficulty) {
        publishCreateRoom(difficulty, null);
    }

    void publishJoinRoom(String roomCode, String selectedSkill);

    default void publishJoinRoom(String roomCode) {
        publishJoinRoom(roomCode, null);
    }

    void publishStartGame();

    void publishMove(int x, int y);

    void publishSkill(String skillType);

    void publishReady(boolean ready);

    void publishLobbyConfig(String difficulty, String selectedSkill);

    default void publishLobbyConfig(String difficulty) {
        publishLobbyConfig(difficulty, null);
    }

    void publishUpgradeChoice(String choice);
}
