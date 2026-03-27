package edu.hitsz.client;

public interface ClientCommandPublisher {

    void start();

    void stop();

    void publishCreateRoom(String difficulty);

    void publishJoinRoom(String roomCode);

    void publishStartGame();

    void publishMove(int x, int y);

    void publishSkill(String skillType);

    void publishReady(boolean ready);

    void publishLobbyConfig(String difficulty);

    void publishUpgradeChoice(String choice);
}
