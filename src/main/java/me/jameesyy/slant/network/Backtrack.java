package me.jameesyy.slant.network;

import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.LagUtils;
import me.jameesyy.slant.util.Reporter;

import static me.jameesyy.slant.network.PacketManager.targetpos;

public class Backtrack {
    public static boolean shouldSpoof = false;
    private static boolean enabled;
    private static int delay;
    private static int sensitivity;

    public static void tickCheck() {
        if (!PacketManager.target.isPresent() || targetpos == null) {
            shouldSpoof = false;
            return;
        }

        if (LagUtils.getDistanceToAxis(targetpos) + (double) (sensitivity - 100) / 100 < LagUtils.getDistanceToAxis(PacketManager.target.get().getEntityBoundingBox())) PacketManager.clearInboundQueue();
        else shouldSpoof = true;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        Backtrack.enabled = b;
        ModConfig.backtrackEnabled = b;
        Reporter.reportToggled("Backtrack", b);
    }

    public static int getDelay() {
        return delay;
    }

    public static void setDelay(int delay) {
        Backtrack.delay = delay;
        ModConfig.backtrackDelayMs = delay;
        Reporter.reportSet("Backtrack", "Delay", delay);
    }

    public static int getSensitivity() {
        return sensitivity;
    }

    public static void setSensitivity(int sensitivity) {
        Backtrack.sensitivity = sensitivity;
        ModConfig.backtrackSensitivity = sensitivity;
        Reporter.reportSet("Backtrack", "Sensitivity", sensitivity);
    }
}
