package me.jameesyy.slant.combat;

import me.jameesyy.slant.Reporter;

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
