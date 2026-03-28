package edu.hitsz.common.protocol.json;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.ChapterId;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.protocol.dto.BulletSnapshot;
import edu.hitsz.common.protocol.dto.ExplosionSnapshot;
import edu.hitsz.common.protocol.dto.EnemySnapshot;
import edu.hitsz.common.protocol.dto.ItemSnapshot;
import edu.hitsz.common.protocol.dto.LaserSnapshot;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

import java.util.List;

public class WorldSnapshotJsonMapper {

    private static final int LEGACY_DEFAULT_MAX_HP = 1000;

    public String toJson(WorldSnapshot snapshot) {
        StringBuilder builder = new StringBuilder();
        builder.append('{')
                .append("\"tick\":").append(snapshot.getTick()).append(',')
                .append("\"gameStarted\":").append(snapshot.isGameStarted()).append(',')
                .append("\"readyPlayerCount\":").append(snapshot.getReadyPlayerCount()).append(',')
                .append("\"connectedPlayerCount\":").append(snapshot.getConnectedPlayerCount()).append(',')
                .append("\"difficulty\":").append(SimpleJsonSupport.quote(snapshot.getDifficulty())).append(',')
                .append("\"roomCode\":").append(SimpleJsonSupport.quote(snapshot.getRoomCode())).append(',')
                .append("\"hostSessionId\":").append(SimpleJsonSupport.quote(snapshot.getHostSessionId())).append(',')
                .append("\"totalScore\":").append(snapshot.getTotalScore()).append(',')
                .append("\"bossActive\":").append(snapshot.isBossActive()).append(',')
                .append("\"nextBossScoreThreshold\":").append(snapshot.getNextBossScoreThreshold()).append(',')
                .append("\"gamePhase\":").append(SimpleJsonSupport.quote(snapshot.getGamePhase().name())).append(',')
                .append("\"chapterId\":").append(SimpleJsonSupport.quote(snapshot.getChapterId().name())).append(',')
                .append("\"chapterTransitionFlash\":").append(snapshot.isChapterTransitionFlash()).append(',')
                .append("\"firstBossBranchSelection\":").append(snapshot.isFirstBossBranchSelection()).append(',')
                .append("\"bulletHitAudioCount\":").append(snapshot.getBulletHitAudioCount()).append(',')
                .append("\"supplyPickupAudioCount\":").append(snapshot.getSupplyPickupAudioCount()).append(',')
                .append("\"players\":").append(playersToJson(snapshot.getPlayerSnapshots())).append(',')
                .append("\"enemies\":").append(enemiesToJson(snapshot.getEnemySnapshots())).append(',')
                .append("\"heroBullets\":").append(bulletsToJson(snapshot.getHeroBulletSnapshots())).append(',')
                .append("\"enemyBullets\":").append(bulletsToJson(snapshot.getEnemyBulletSnapshots())).append(',')
                .append("\"lasers\":").append(lasersToJson(snapshot.getLaserSnapshots())).append(',')
                .append("\"explosions\":").append(explosionsToJson(snapshot.getExplosionSnapshots())).append(',')
                .append("\"items\":").append(itemsToJson(snapshot.getItemSnapshots()))
                .append('}');
        return builder.toString();
    }

    public WorldSnapshot fromJson(String json) {
        WorldSnapshot snapshot = new WorldSnapshot(SimpleJsonSupport.extractLong(json, "tick"));
        snapshot.setGameStarted(SimpleJsonSupport.extractBoolean(json, "gameStarted"));
        snapshot.setReadyPlayerCount(SimpleJsonSupport.extractInt(json, "readyPlayerCount"));
        snapshot.setConnectedPlayerCount(SimpleJsonSupport.extractInt(json, "connectedPlayerCount"));
        snapshot.setDifficulty(SimpleJsonSupport.extractString(json, "difficulty"));
        snapshot.setRoomCode(SimpleJsonSupport.extractString(json, "roomCode"));
        snapshot.setHostSessionId(SimpleJsonSupport.extractString(json, "hostSessionId"));
        snapshot.setTotalScore(SimpleJsonSupport.extractInt(json, "totalScore"));
        snapshot.setBossActive(SimpleJsonSupport.extractBoolean(json, "bossActive"));
        snapshot.setNextBossScoreThreshold(SimpleJsonSupport.extractInt(json, "nextBossScoreThreshold"));
        snapshot.setGamePhase(GamePhase.valueOf(SimpleJsonSupport.extractStringOrDefault(
                json,
                "gamePhase",
                GamePhase.LOBBY.name()
        )));
        snapshot.setChapterId(ChapterId.valueOf(SimpleJsonSupport.extractStringOrDefault(
                json,
                "chapterId",
                ChapterId.CH1.name()
        )));
        snapshot.setChapterTransitionFlash(SimpleJsonSupport.extractBooleanOrDefault(json, "chapterTransitionFlash", false));
        snapshot.setFirstBossBranchSelection(SimpleJsonSupport.extractBooleanOrDefault(json, "firstBossBranchSelection", false));
        snapshot.setBulletHitAudioCount(SimpleJsonSupport.extractIntOrDefault(json, "bulletHitAudioCount", 0));
        snapshot.setSupplyPickupAudioCount(SimpleJsonSupport.extractIntOrDefault(json, "supplyPickupAudioCount", 0));
        for (String playerJson : SimpleJsonSupport.splitTopLevelArray(SimpleJsonSupport.extractJsonValue(json, "players"))) {
            snapshot.addPlayerSnapshot(new PlayerSnapshot(
                    SimpleJsonSupport.extractString(playerJson, "sessionId"),
                    SimpleJsonSupport.extractString(playerJson, "playerId"),
                    SimpleJsonSupport.extractInt(playerJson, "x"),
                    SimpleJsonSupport.extractInt(playerJson, "y"),
                    SimpleJsonSupport.extractInt(playerJson, "hp"),
                    SimpleJsonSupport.extractInt(playerJson, "score"),
                    SimpleJsonSupport.extractBoolean(playerJson, "ready"),
                    SimpleJsonSupport.extractInt(playerJson, "level"),
                    SimpleJsonSupport.extractString(playerJson, "selectedSkill"),
                    SimpleJsonSupport.extractLongOrDefault(playerJson, "skillCooldownRemainingMillis", 0L),
                    SimpleJsonSupport.extractLongOrDefault(playerJson, "skillCooldownTotalMillis", 0L),
                    SimpleJsonSupport.extractIntOrDefault(playerJson, "maxHp", LEGACY_DEFAULT_MAX_HP),
                    splitUpgradeChoices(SimpleJsonSupport.extractJsonValueOrDefault(playerJson, "availableUpgradeChoices", "[]")),
                    parseUpgradeChoice(SimpleJsonSupport.extractStringOrDefault(playerJson, "selectedUpgradeChoice", null)),
                    parseAircraftBranch(SimpleJsonSupport.extractStringOrDefault(playerJson, "aircraftBranch", null)),
                    splitAircraftBranches(SimpleJsonSupport.extractJsonValueOrDefault(playerJson, "availableBranchChoices", "[]")),
                    SimpleJsonSupport.extractBooleanOrDefault(playerJson, "branchUnlocked", false)
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
        for (String laserJson : SimpleJsonSupport.splitTopLevelArray(
                SimpleJsonSupport.extractJsonValueOrDefault(json, "lasers", "[]")
        )) {
            snapshot.addLaserSnapshot(new LaserSnapshot(
                    SimpleJsonSupport.extractString(laserJson, "ownerSessionId"),
                    SimpleJsonSupport.extractInt(laserJson, "originX"),
                    SimpleJsonSupport.extractInt(laserJson, "originY"),
                    Double.parseDouble(SimpleJsonSupport.extractJsonValue(laserJson, "angle")),
                    SimpleJsonSupport.extractInt(laserJson, "width"),
                    SimpleJsonSupport.extractInt(laserJson, "length"),
                    SimpleJsonSupport.extractInt(laserJson, "durationTicks"),
                    SimpleJsonSupport.extractInt(laserJson, "damage"),
                    SimpleJsonSupport.extractStringOrDefault(laserJson, "style", "PLAYER_RED_SPEED"),
                    Double.parseDouble(SimpleJsonSupport.extractJsonValueOrDefault(laserJson, "chargeRatio", "1.0"))
            ));
        }
        for (String explosionJson : SimpleJsonSupport.splitTopLevelArray(
                SimpleJsonSupport.extractJsonValueOrDefault(json, "explosions", "[]")
        )) {
            snapshot.addExplosionSnapshot(new ExplosionSnapshot(
                    SimpleJsonSupport.extractInt(explosionJson, "x"),
                    SimpleJsonSupport.extractInt(explosionJson, "y"),
                    SimpleJsonSupport.extractInt(explosionJson, "radius"),
                    SimpleJsonSupport.extractInt(explosionJson, "durationTicks")
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
                    .append("\"x\":").append(player.getX()).append(',')
                    .append("\"y\":").append(player.getY()).append(',')
                    .append("\"hp\":").append(player.getHp()).append(',')
                    .append("\"score\":").append(player.getScore()).append(',')
                    .append("\"ready\":").append(player.isReady()).append(',')
                    .append("\"level\":").append(player.getLevel()).append(',')
                    .append("\"selectedSkill\":").append(SimpleJsonSupport.quote(player.getSelectedSkill())).append(',')
                    .append("\"skillCooldownRemainingMillis\":").append(player.getSkillCooldownRemainingMillis()).append(',')
                    .append("\"skillCooldownTotalMillis\":").append(player.getSkillCooldownTotalMillis()).append(',')
                    .append("\"maxHp\":").append(player.getMaxHp()).append(',')
                    .append("\"availableUpgradeChoices\":").append(upgradeChoicesToJson(player.getAvailableUpgradeChoices())).append(',')
                    .append("\"selectedUpgradeChoice\":").append(SimpleJsonSupport.quote(enumName(player.getSelectedUpgradeChoice()))).append(',')
                    .append("\"aircraftBranch\":").append(SimpleJsonSupport.quote(enumName(player.getAircraftBranch()))).append(',')
                    .append("\"availableBranchChoices\":").append(branchChoicesToJson(player.getAvailableBranchChoices())).append(',')
                    .append("\"branchUnlocked\":").append(player.isBranchUnlocked())
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

    private String lasersToJson(List<LaserSnapshot> lasers) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < lasers.size(); i++) {
            LaserSnapshot laser = lasers.get(i);
            if (i > 0) {
                builder.append(',');
            }
            builder.append('{')
                    .append("\"ownerSessionId\":").append(SimpleJsonSupport.quote(laser.getOwnerSessionId())).append(',')
                    .append("\"originX\":").append(laser.getOriginX()).append(',')
                    .append("\"originY\":").append(laser.getOriginY()).append(',')
                    .append("\"angle\":").append(laser.getAngle()).append(',')
                    .append("\"width\":").append(laser.getWidth()).append(',')
                    .append("\"length\":").append(laser.getLength()).append(',')
                    .append("\"durationTicks\":").append(laser.getDurationTicks()).append(',')
                    .append("\"damage\":").append(laser.getDamage()).append(',')
                    .append("\"style\":").append(SimpleJsonSupport.quote(laser.getStyle())).append(',')
                    .append("\"chargeRatio\":").append(laser.getChargeRatio())
                    .append('}');
        }
        builder.append(']');
        return builder.toString();
    }

    private String explosionsToJson(List<ExplosionSnapshot> explosions) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < explosions.size(); i++) {
            ExplosionSnapshot explosion = explosions.get(i);
            if (i > 0) {
                builder.append(',');
            }
            builder.append('{')
                    .append("\"x\":").append(explosion.getX()).append(',')
                    .append("\"y\":").append(explosion.getY()).append(',')
                    .append("\"radius\":").append(explosion.getRadius()).append(',')
                    .append("\"durationTicks\":").append(explosion.getDurationTicks())
                    .append('}');
        }
        builder.append(']');
        return builder.toString();
    }

    private String upgradeChoicesToJson(List<BranchUpgradeChoice> values) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(SimpleJsonSupport.quote(values.get(i).name()));
        }
        builder.append(']');
        return builder.toString();
    }

    private List<BranchUpgradeChoice> splitUpgradeChoices(String jsonArray) {
        if (jsonArray == null || "null".equals(jsonArray.trim())) {
            return new java.util.LinkedList<>();
        }
        List<BranchUpgradeChoice> choices = new java.util.LinkedList<>();
        for (String choiceJson : SimpleJsonSupport.splitTopLevelArray(jsonArray)) {
            choices.add(BranchUpgradeChoice.valueOf(SimpleJsonSupport.unquote(choiceJson)));
        }
        return choices;
    }

    private BranchUpgradeChoice parseUpgradeChoice(String value) {
        return value == null ? null : BranchUpgradeChoice.valueOf(value);
    }

    private String branchChoicesToJson(List<AircraftBranch> values) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(SimpleJsonSupport.quote(values.get(i).name()));
        }
        builder.append(']');
        return builder.toString();
    }

    private List<AircraftBranch> splitAircraftBranches(String jsonArray) {
        if (jsonArray == null || "null".equals(jsonArray.trim())) {
            return new java.util.LinkedList<>();
        }
        List<AircraftBranch> branches = new java.util.LinkedList<>();
        for (String branchJson : SimpleJsonSupport.splitTopLevelArray(jsonArray)) {
            branches.add(AircraftBranch.valueOf(SimpleJsonSupport.unquote(branchJson)));
        }
        return branches;
    }

    private AircraftBranch parseAircraftBranch(String value) {
        return value == null ? null : AircraftBranch.valueOf(value);
    }

    private String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
