package me.calclb.aimer.movement;

import me.calclb.aimer.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

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
