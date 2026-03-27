package edu.hitsz.common.protocol;

public enum MessageType {
    HELLO,
    CREATE_ROOM,
    JOIN_ROOM,
    START_GAME,
    INPUT_MOVE,
    INPUT_SKILL,
    INPUT_READY,
    INPUT_LOBBY_CONFIG,
    INPUT_UPGRADE_CHOICE,
    INPUT_BRANCH_CHOICE,
    PING,
    WELCOME,
    WORLD_SNAPSHOT,
    PLAYER_JOINED,
    PLAYER_LEFT,
    GAME_OVER,
    ERROR
}
