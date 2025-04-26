package me.jameesyy.slant.network;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.Main;
import me.jameesyy.slant.Main;
import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.combat.AutoWeapon;
import me.jameesyy.slant.util.LagUtils;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Optional;

public class Backtrack {
    public static boolean shouldSpoof = false;
    private static boolean enabled;
    private static int delay;
    private static int sensitivity;
    private static float minRange;

    public static void tickCheck() {
        EntityPlayer target = PacketManager.getTarget();
        Optional<AxisAlignedBB> targetBox = PacketManager.getTargetBox();

        if (target != null) {
            // First check if target is beyond minimum range
            if (Minecraft.getMinecraft().thePlayer.getDistanceToEntity(target) < minRange) {
                shouldSpoof = false;
                PacketManager.processWholePacketQueue();
                return;
            }

            // Continue with normal backtrack logic if beyond min range
            if (targetBox.isPresent()) {
                if (LagUtils.getDistanceToAxis(targetBox.get()) + (double) (sensitivity - 100) / 100 < LagUtils.getDistanceToAxis(target.getEntityBoundingBox())) {
                    PacketManager.processWholePacketQueue();
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
        Reporter.queueEnableMsg("Backtrack", b);
    }

    public static int getDelay() {
        return delay;
    }

    public static void setDelay(int delay) {
        Backtrack.delay = delay;
        ModConfig.backtrackDelayMs = delay;
        Reporter.queueSetMsg("Backtrack", "Delay", delay);
    }

    public static int getSensitivity() {
        return sensitivity;
    }

    public static void setSensitivity(int sensitivity) {
        Backtrack.sensitivity = sensitivity;
        ModConfig.backtrackSensitivity = sensitivity;
        Reporter.queueSetMsg("Backtrack", "Sensitivity", sensitivity);
    }

    public static double getMinRange() {
        return minRange;
    }

    public static void setMinRange(float range) {
        Backtrack.minRange = range;
        ModConfig.backtrackMinRange = range;
        Reporter.queueSetMsg("Backtrack", "Min Range", range);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Main.getBacktrackKey().isPressed()) {
            Backtrack.setEnabled(!enabled);
        }
    }

}