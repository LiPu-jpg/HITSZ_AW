package edu.hitsz.client;

import edu.hitsz.common.Difficulty;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class LauncherNoSkillSelectionTest {

    public static void main(String[] args) throws Exception {
        LauncherSelectionModel defaults = new LauncherSelectionModel();
        assert "CREATE".equals(invokeString(defaults, "getEntryMode")) : "Launcher should default to create-room mode";
        assert "".equals(invokeString(defaults, "getRoomCode")) : "Launcher should default to empty room code";
        assert Difficulty.NORMAL.name().equals(defaults.getDifficulty()) : "Create-room flow should default to NORMAL";
        assert !hasMethod(LauncherSelectionModel.class, "getSelectedSkill")
                : "Launcher selection should no longer expose lobby skill state";

        AtomicReference<LauncherSelectionModel> submitted = new AtomicReference<>();
        LauncherPanel panel = new LauncherPanel(submitted::set);
        assert "Create Room".equals(findButtonText(panel, "Create Room", "Join Room"))
                : "Launcher should show a create-room primary action by default";
        assert containsLabel(panel, "Mode: CREATE") : "Launcher should expose current mode as a visible label";
        assert !containsLabel(panel, "Skill:") : "Launcher should not show a lobby skill summary";
        assert !containsButton(panel, "Freeze") : "Launcher should not show lobby skill buttons";
        assert !containsButton(panel, "Bomb") : "Launcher should not show lobby skill buttons";
        assert !containsButton(panel, "Shield") : "Launcher should not show lobby skill buttons";

        invokeVoid(panel, "selectJoinMode");
        invokeVoid(panel, "setRoomCode", "654321");
        assert "Join Room".equals(findButtonText(panel, "Create Room", "Join Room"))
                : "Primary action should switch when mode changes";
        assert containsLabel(panel, "Mode: JOIN") : "Visible mode summary should update when switching mode";
        assert containsLabel(panel, "Room Code: 654321") : "Visible summary should show typed room code";
        panel.submitSelections();

        assert submitted.get() != null : "Launcher panel should submit a selection model";
        assert "JOIN".equals(invokeString(submitted.get(), "getEntryMode")) : "Launcher should submit join-room mode";
        assert "654321".equals(invokeString(submitted.get(), "getRoomCode")) : "Launcher should submit typed room code";
        assert !hasMethod(submitted.get().getClass(), "getSelectedSkill")
                : "Submitted launcher state should not carry a skill selection";
    }

    private static boolean containsLabel(LauncherPanel panel, String text) {
        for (java.awt.Component component : panel.getComponents()) {
            if (component instanceof javax.swing.JLabel && text.equals(((javax.swing.JLabel) component).getText())) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsButton(LauncherPanel panel, String text) {
        for (java.awt.Component component : panel.getComponents()) {
            if (component instanceof javax.swing.JButton && text.equals(((javax.swing.JButton) component).getText())) {
                return true;
            }
        }
        return false;
    }

    private static String findButtonText(LauncherPanel panel, String... candidates) {
        for (java.awt.Component component : panel.getComponents()) {
            if (component instanceof javax.swing.JButton) {
                String text = ((javax.swing.JButton) component).getText();
                for (String candidate : candidates) {
                    if (candidate.equals(text)) {
                        return text;
                    }
                }
            }
        }
        return null;
    }

    private static boolean hasMethod(Class<?> type, String methodName) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    private static String invokeString(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return (String) method.invoke(target);
    }

    private static void invokeVoid(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        method.invoke(target);
    }

    private static void invokeVoid(Object target, String methodName, String argument) throws Exception {
        Method method = target.getClass().getMethod(methodName, String.class);
        method.invoke(target, argument);
    }
}
