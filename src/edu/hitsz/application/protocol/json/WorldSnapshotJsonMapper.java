package edu.hitsz.application.protocol.json;

import edu.hitsz.application.protocol.dto.PlayerSnapshot;
import edu.hitsz.application.protocol.dto.WorldSnapshot;

import java.util.List;

public class WorldSnapshotJsonMapper {

    public String toJson(WorldSnapshot snapshot) {
        StringBuilder builder = new StringBuilder();
        builder.append('{')
                .append("\"tick\":").append(snapshot.getTick()).append(',')
                .append("\"players\":[");

        List<PlayerSnapshot> players = snapshot.getPlayerSnapshots();
        for (int i = 0; i < players.size(); i++) {
            PlayerSnapshot player = players.get(i);
            if (i > 0) {
                builder.append(',');
            }
            builder.append('{')
                    .append("\"sessionId\":").append(SimpleJsonSupport.quote(player.getSessionId())).append(',')
                    .append("\"playerId\":").append(SimpleJsonSupport.quote(player.getPlayerId())).append(',')
                    .append("\"localPlayer\":").append(player.isLocalPlayer()).append(',')
                    .append("\"x\":").append(player.getX()).append(',')
                    .append("\"y\":").append(player.getY()).append(',')
                    .append("\"hp\":").append(player.getHp())
                    .append('}');
        }

        builder.append("]}");
        return builder.toString();
    }

    public WorldSnapshot fromJson(String json) {
        WorldSnapshot snapshot = new WorldSnapshot(SimpleJsonSupport.extractLong(json, "tick"));
        String playersArray = SimpleJsonSupport.extractJsonValue(json, "players");
        for (String playerJson : SimpleJsonSupport.splitTopLevelArray(playersArray)) {
            snapshot.addPlayerSnapshot(new PlayerSnapshot(
                    SimpleJsonSupport.extractString(playerJson, "sessionId"),
                    SimpleJsonSupport.extractString(playerJson, "playerId"),
                    SimpleJsonSupport.extractBoolean(playerJson, "localPlayer"),
                    SimpleJsonSupport.extractInt(playerJson, "x"),
                    SimpleJsonSupport.extractInt(playerJson, "y"),
                    SimpleJsonSupport.extractInt(playerJson, "hp")
            ));
        }
        return snapshot;
    }
}
