package edu.hitsz.client;

import java.lang.reflect.Method;

public class StatusBarModelTest {

    public static void main(String[] args) throws Exception {
        hpRatioUsesCurrentAndMaxHp();
        cooldownRatioUsesRemainingAndTotal();
        cooldownLabelMatchesState();
    }

    private static void hpRatioUsesCurrentAndMaxHp() throws Exception {
        double ratio = invokeIntDouble("hpFillRatio", 750, 1000);
        assert Math.abs(ratio - 0.75d) < 0.0001d
                : "HP ratio should use current and max hp";

        double clampedRatio = invokeIntDouble("hpFillRatio", 1400, 1000);
        assert Math.abs(clampedRatio - 1.0d) < 0.0001d
                : "HP ratio should clamp at 1.0";
    }

    private static void cooldownRatioUsesRemainingAndTotal() throws Exception {
        double ratio = invokeLongDouble("cooldownFillRatio", 2500L, 6000L);
        assert Math.abs(ratio - (3500.0d / 6000.0d)) < 0.0001d
                : "Cooldown bar should fill based on recovered cooldown progress";

        double readyRatio = invokeLongDouble("cooldownFillRatio", 0L, 6000L);
        assert Math.abs(readyRatio - 1.0d) < 0.0001d
                : "Cooldown bar should be full when ready";
    }

    private static void cooldownLabelMatchesState() throws Exception {
        String cooling = invokeString("cooldownLabel", "FREEZE", 2500L, 6000L);
        assert "冻结 2.5秒".equals(cooling)
                : "Cooldown label should show localized skill name and remaining seconds";

        String ready = invokeString("cooldownLabel", "FREEZE", 0L, 6000L);
        assert "冻结 可用".equals(ready)
                : "Cooldown label should show ready state";

        String unavailable = invokeString("cooldownLabel", null, 0L, 0L);
        assert "无技能".equals(unavailable)
                : "Cooldown label should handle aircrafts without an active skill";
    }

    private static double invokeIntDouble(String methodName, int left, int right) throws Exception {
        Class<?> clazz = Class.forName("edu.hitsz.client.StatusBarModel");
        Method method = clazz.getDeclaredMethod(methodName, int.class, int.class);
        method.setAccessible(true);
        return (Double) method.invoke(null, left, right);
    }

    private static double invokeLongDouble(String methodName, long left, long right) throws Exception {
        Class<?> clazz = Class.forName("edu.hitsz.client.StatusBarModel");
        Method method = clazz.getDeclaredMethod(methodName, long.class, long.class);
        method.setAccessible(true);
        return (Double) method.invoke(null, left, right);
    }

    private static String invokeString(String methodName, Object... args) throws Exception {
        Class<?> clazz = Class.forName("edu.hitsz.client.StatusBarModel");
        Method method = clazz.getDeclaredMethod(methodName, String.class, long.class, long.class);
        method.setAccessible(true);
        return (String) method.invoke(null, args);
    }
}
