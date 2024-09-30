package me.jameesyy.slant.combat;

import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.Reporter;
import me.jameesyy.slant.util.AntiBot;
import me.jameesyy.slant.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Aimlock {
    private static EntityLivingBase targetEntity;
    private static float rangeSqr;
    private static float maxYawTickRotation;
    private static float rotationSpeed;
    private static boolean doVerticalRotations;
    private static boolean enabled;

    public static float getActivationRadiusSqr() {
        return rangeSqr;
    }

    public static void setActivationRadius(float range) {
        rangeSqr = range*range;
        Reporter.reportSet("Aimlock", "Activation Radius", range);
    }

    public static EntityLivingBase getTargetEntity() {
        return targetEntity;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.aimlockEnabled = b;
        Reporter.reportToggled("Aimlock", b);
    }

    public static void setMaxYawTickRotation(float maxtickrot) {
        Aimlock.maxYawTickRotation = maxtickrot;
        Reporter.reportSet("Aimlock", "Max Yaw Tick Rotation", maxtickrot);
    }

    public static void setRotationSpeed(float rotationSpeed) {
        Aimlock.rotationSpeed = rotationSpeed;
        Reporter.reportSet("Aimlock", "Rotation Speed", rotationSpeed);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static float getMaxYawTickRotation() {
        return maxYawTickRotation;
    }

    public static float getRotationSpeed() {
        return rotationSpeed;
    }

    public static boolean shouldDoVerticalRotations() {
        return doVerticalRotations;
    }

    public static void setVerticalRotations(boolean doVerticalRotations) {
        Aimlock.doVerticalRotations = doVerticalRotations;
        Reporter.reportSet("Aimlock", "Vertical Rotations", doVerticalRotations);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        // Toggle aimlock and reset target when key is pressed
        if (Main.getAimlockKey().isPressed()) {
            targetEntity = null;
            setEnabled(!enabled);
        }

        if (!enabled) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null) return;


        // Reset target if not looking at an entity
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
            targetEntity = null;
            return;
        }

        // Check if the entity hit is a valid target
        if (mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
            EntityLivingBase potentialTarget = (EntityLivingBase) mc.objectMouseOver.entityHit;

            if (AntiBot.isRecommendedTarget(potentialTarget, rangeSqr)) {
                targetEntity = potentialTarget;
            } else {
                targetEntity = null;
            }
        } else {
            targetEntity = null;
        }
    }

    public static boolean isInAValidStateToAim() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) return false;
        if (mc.currentScreen != null) return false;
        return enabled;
    }

    public static double[] calculateHitboxBounds(EntityPlayerSP player, Entity target) {
        double eyeHeight = player.getEyeHeight();
        double dx = target.posX - player.posX;
        double dy = target.posY - (player.posY + eyeHeight);
        double dz = target.posZ - player.posZ;

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        double targetWidth = target.width;
        double targetHeight = target.height;

        double centerYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        double centerPitch = -Math.toDegrees(Math.atan2(dy + targetHeight / 2, horizontalDistance));

        double yawRange = Math.toDegrees(Math.atan(targetWidth / (2 * horizontalDistance)));
        double pitchRange = Math.toDegrees(Math.atan(targetHeight / (2 * horizontalDistance)));

        double minYaw = centerYaw - yawRange;
        double maxYaw = centerYaw + yawRange;
        double minPitch = centerPitch - pitchRange;
        double maxPitch = centerPitch + pitchRange;

        return new double[]{minYaw, maxYaw, minPitch, maxPitch};
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}