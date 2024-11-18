package me.jameesyy.slant.movement;

import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.Reporter;

public class NoJumpDelay {
    private static boolean enabled;

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.noJumpDelayEnabled = b;
        Reporter.queueReportMsg("No Jump Delay", b);
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
