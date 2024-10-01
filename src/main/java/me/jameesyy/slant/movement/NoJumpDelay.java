package me.jameesyy.slant.movement;

import me.jameesyy.slant.util.Reporter;

public class NoJumpDelay {
    private static boolean enabled;

    public static void setEnabled(boolean b) {
        enabled = b;
        Reporter.reportToggled("No Jump Delay", b);
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
