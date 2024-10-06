package me.jameesyy.slant.combat;

import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.Reporter;

public class NoHitDelay {
    private static boolean enabled;

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.noHitDelayEnabled = b;
        Reporter.reportToggled("No Hit Delay", b);
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
