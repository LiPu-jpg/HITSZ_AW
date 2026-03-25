package edu.hitsz.application.client;

public interface ClientCommandPublisher {

    void start();

    void stop();

    void publishMove(int x, int y);

    void publishSkill(String skillType);
}
