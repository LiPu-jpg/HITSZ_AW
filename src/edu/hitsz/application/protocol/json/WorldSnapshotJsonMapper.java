package edu.hitsz.application.protocol.json;

import edu.hitsz.application.protocol.dto.BulletSnapshot;
import edu.hitsz.application.protocol.dto.EnemySnapshot;
import edu.hitsz.application.protocol.dto.ItemSnapshot;
import edu.hitsz.application.protocol.dto.PlayerSnapshot;
import edu.hitsz.application.protocol.dto.WorldSnapshot;

import java.util.List;

public class WorldSnapshotJsonMapper {

    public String toJson(WorldSnapshot snapshot) {
        StringBuilder builder = new StringBuilder();
        builder.append('{')
                .append("\"tick\":").append(snapshot.getTick()).append(',')
                .append("\"players\":").append(playersToJson(snapshot.getPlayerSnapshots())).append(',')
                .append("\"enemies\":").append(enemiesToJson(snapshot.getEnemySnapshots())).append(',')
                .append("\"heroBullets\":").append(bulletsToJson(snapshot.getHeroBulletSnapshots())).append(',')
                .append("\"enemyBullets\":").append(bulletsToJson(snapshot.getEnemyBulletSnapshots())).append(',')
                .append("\"items\":").append(itemsToJson(snapshot.getItemSnapshots()))
                .append('}');
        return builder.toString();
    }

    public WorldSnapshot fromJson(String json) {
        WorldSnapshot snapshot = new WorldSnapshot(SimpleJsonSupport.extractLong(json, "tick"));
        for (String playerJson : SimpleJsonSupport.splitTopLevelArray(SimpleJsonSupport.extractJsonValue(json, "players"))) {
            snapshot.addPlayerSnapshot(new PlayerSnapshot(
                    SimpleJsonSupport.extractString(playerJson, "sessionId"),
                    SimpleJsonSupport.extractString(playerJson, "playerId"),
                    SimpleJsonSupport.extractBoolean(playerJson, "localPlayer"),
                    SimpleJsonSupport.extractInt(playerJson, "x"),
                    SimpleJsonSupport.extractInt(playerJson, "y"),
                    SimpleJsonSupport.extractInt(playerJson, "hp"),
                    SimpleJsonSupport.extractInt(playerJson, "score")
            ));
        }
        for (String enemyJson : SimpleJsonSupport.splitTopLevelArray(SimpleJsonSupport.extractJsonValue(json, "enemies"))) {
            snapshot.addEnemySnapshot(new EnemySnapshot(
                    SimpleJsonSupport.extractString(enemyJson, "type"),
                    SimpleJsonSupport.extractInt(enemyJson, "x"),
                    SimpleJsonSupport.extractInt(enemyJson, "y"),
                    SimpleJsonSupport.extractInt(enemyJson, "hp")
            ));
        }
        for (String bulletJson : SimpleJsonSupport.splitTopLevelArray(SimpleJsonSupport.extractJsonValue(json, "heroBullets"))) {
            snapshot.addHeroBulletSnapshot(new BulletSnapshot(
                    SimpleJsonSupport.extractString(bulletJson, "type"),
                    SimpleJsonSupport.extractInt(bulletJson, "x"),
                    SimpleJsonSupport.extractInt(bulletJson, "y")
            ));
        }
        for (String bulletJson : SimpleJsonSupport.splitTopLevelArray(SimpleJsonSupport.extractJsonValue(json, "enemyBullets"))) {
            snapshot.addEnemyBulletSnapshot(new BulletSnapshot(
                    SimpleJsonSupport.extractString(bulletJson, "type"),
                    SimpleJsonSupport.extractInt(bulletJson, "x"),
                    SimpleJsonSupport.extractInt(bulletJson, "y")
            ));
        }
        for (String itemJson : SimpleJsonSupport.splitTopLevelArray(SimpleJsonSupport.extractJsonValue(json, "items"))) {
            snapshot.addItemSnapshot(new ItemSnapshot(
                    SimpleJsonSupport.extractString(itemJson, "type"),
                    SimpleJsonSupport.extractInt(itemJson, "x"),
                    SimpleJsonSupport.extractInt(itemJson, "y")
            ));
        }
        return snapshot;
    }

    private String playersToJson(List<PlayerSnapshot> players) {
        StringBuilder builder = new StringBuilder("[");
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
                    .append("\"hp\":").append(player.getHp()).append(',')
                    .append("\"score\":").append(player.getScore())
                    .append('}');
        }
        builder.append(']');
        return builder.toString();
    }

    private String enemiesToJson(List<EnemySnapshot> enemies) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < enemies.size(); i++) {
            EnemySnapshot enemy = enemies.get(i);
            if (i > 0) {
                builder.append(',');
            }
            builder.append('{')
                    .append("\"type\":").append(SimpleJsonSupport.quote(enemy.getType())).append(',')
                    .append("\"x\":").append(enemy.getX()).append(',')
                    .append("\"y\":").append(enemy.getY()).append(',')
                    .append("\"hp\":").append(enemy.getHp())
                    .append('}');
        }
        builder.append(']');
        return builder.toString();
    }

    private String bulletsToJson(List<BulletSnapshot> bullets) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < bullets.size(); i++) {
            BulletSnapshot bullet = bullets.get(i);
            if (i > 0) {
                builder.append(',');
            }
            builder.append('{')
                    .append("\"type\":").append(SimpleJsonSupport.quote(bullet.getType())).append(',')
                    .append("\"x\":").append(bullet.getX()).append(',')
                    .append("\"y\":").append(bullet.getY())
                    .append('}');
        }
        builder.append(']');
        return builder.toString();
    }

    private String itemsToJson(List<ItemSnapshot> items) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            ItemSnapshot item = items.get(i);
            if (i > 0) {
                builder.append(',');
            }
            builder.append('{')
                    .append("\"type\":").append(SimpleJsonSupport.quote(item.getType())).append(',')
                    .append("\"x\":").append(item.getX()).append(',')
                    .append("\"y\":").append(item.getY())
                    .append('}');
        }
        builder.append(']');
        return builder.toString();
    }
}
