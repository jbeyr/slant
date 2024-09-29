package me.calclb.aimer.movement;

public class NoJumpDelay {
    private static boolean enabled;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }
}
