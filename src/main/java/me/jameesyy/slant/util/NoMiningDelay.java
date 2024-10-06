package me.jameesyy.slant.util;

import me.jameesyy.slant.ModConfig;

public class NoMiningDelay {

    private static boolean enabled;



    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.noMiningDelayEnabled = b;
        Reporter.reportToggled("No Mining Delay", b);
    }
}
