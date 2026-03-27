package edu.hitsz.client;

import edu.hitsz.common.Difficulty;

public class LauncherSelectionModel {

    private String entryMode = "CREATE";
    private String roomCode = "";
    private String difficulty = Difficulty.NORMAL.name();

    public String getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(String entryMode) {
        this.entryMode = entryMode;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode == null ? "" : roomCode.trim();
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public LauncherSelectionModel copy() {
        LauncherSelectionModel copy = new LauncherSelectionModel();
        copy.setEntryMode(entryMode);
        copy.setRoomCode(roomCode);
        copy.setDifficulty(difficulty);
        return copy;
    }
}
