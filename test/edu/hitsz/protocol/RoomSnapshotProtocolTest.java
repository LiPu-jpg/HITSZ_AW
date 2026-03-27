package edu.hitsz.protocol;

import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;

import java.lang.reflect.Method;

public class RoomSnapshotProtocolTest {

    public static void main(String[] args) throws Exception {
        String json = "{"
                + "\"tick\":3,"
                + "\"gameStarted\":false,"
                + "\"readyPlayerCount\":1,"
                + "\"connectedPlayerCount\":2,"
                + "\"difficulty\":\"HARD\","
                + "\"roomCode\":\"654321\","
                + "\"hostSessionId\":\"session-host\","
                + "\"totalScore\":120,"
                + "\"bossActive\":false,"
                + "\"nextBossScoreThreshold\":500,"
                + "\"players\":[],"
                + "\"enemies\":[],"
                + "\"heroBullets\":[],"
                + "\"enemyBullets\":[],"
                + "\"items\":[]"
                + "}";

        WorldSnapshot snapshot = new WorldSnapshotJsonMapper().fromJson(json);

        assert "654321".equals(invokeString(snapshot, "getRoomCode"))
                : "WorldSnapshot should deserialize roomCode";
        assert "session-host".equals(invokeString(snapshot, "getHostSessionId"))
                : "WorldSnapshot should deserialize hostSessionId";
    }

    private static String invokeString(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return (String) method.invoke(target);
    }
}
