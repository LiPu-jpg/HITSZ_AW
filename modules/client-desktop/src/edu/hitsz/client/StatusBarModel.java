package edu.hitsz.client;

public final class StatusBarModel {

    private StatusBarModel() {
    }

    public static double hpFillRatio(int hp, int maxHp) {
        if (maxHp <= 0) {
            return 0.0d;
        }
        return clamp((double) hp / (double) maxHp);
    }

    public static double cooldownFillRatio(long cooldownRemainingMillis, long cooldownTotalMillis) {
        if (cooldownTotalMillis <= 0L) {
            return 0.0d;
        }
        long recoveredMillis = Math.max(0L, cooldownTotalMillis - Math.max(0L, cooldownRemainingMillis));
        return clamp((double) recoveredMillis / (double) cooldownTotalMillis);
    }

    public static String cooldownLabel(String skillType, long cooldownRemainingMillis, long cooldownTotalMillis) {
        if (skillType == null || skillType.trim().isEmpty() || cooldownTotalMillis <= 0L) {
            return "无技能";
        }
        if (cooldownRemainingMillis <= 0L) {
            return UiText.skillLabel(skillType) + " 可用";
        }
        return String.format("%s %.1f秒", UiText.skillLabel(skillType), cooldownRemainingMillis / 1000.0d);
    }

    public static String hpLabel(int hp, int maxHp) {
        return hp + " / " + Math.max(0, maxHp);
    }

    private static double clamp(double value) {
        if (value < 0.0d) {
            return 0.0d;
        }
        if (value > 1.0d) {
            return 1.0d;
        }
        return value;
    }
}
