package me.jameesyy.slant.network;

import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.LagUtils;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;

public class Backtrack {
    public static boolean shouldSpoof = false;
    private static boolean enabled;
    private static int delay;
    private static int sensitivity;

    public static void tickCheck() {
        EntityPlayer target = PacketManager.getTarget();
        AxisAlignedBB targetpos = PacketManager.getTargetPos();

        if (target != null) {
            if (targetpos != null) {
                if (LagUtils.getDistanceToAxis(targetpos) + (double) (sensitivity - 100) / 100 < LagUtils.getDistanceToAxis(target.getEntityBoundingBox())) {
                    PacketManager.clearInboundQueue();
                } else {
                    shouldSpoof = true;
                }
            } else {
                shouldSpoof = false;
            }
        } else {
            shouldSpoof = false;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        if(!b) shouldSpoof = false;
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
