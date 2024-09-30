package me.calclb.aimer.combat;

import me.calclb.aimer.Reporter;

public class NoHitDelay {
    // TODO implement
    private static boolean enabled;

    public static void setEnabled(boolean b) {
        enabled = b;
        Reporter.reportToggled("No Hit Delay", b);
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
