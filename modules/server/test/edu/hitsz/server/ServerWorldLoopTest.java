package edu.hitsz.server;

import edu.hitsz.server.ServerGameLoop;
import edu.hitsz.server.ServerWorldState;

public class ServerWorldLoopTest {

    public static void main(String[] args) {
        ServerWorldState world = new ServerWorldState();
        ServerGameLoop loop = new ServerGameLoop(world);

        long before = world.getTick();
        loop.stepOnce();
        assert world.getTick() == before + 1 : "Server loop must advance world tick";
    }
}
