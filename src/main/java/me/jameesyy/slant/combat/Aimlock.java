package me.jameesyy.slant.combat;

import me.jameesyy.slant.Main;
import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.AntiBot;
import me.jameesyy.slant.util.Renderer;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.Optional;

public class Aimlock {
    private static Optional<EntityLivingBase> targetEntity = Optional.empty();
    private static boolean enabled;
    private static boolean doVerticalRotations;
    private static float range;
    private static TargetPriority targetPriority;
    private static float rangeSqr;
    private static boolean isHittingBlock;
    private static float fov = 60f;
    private static Color targetHitboxColor;

    public static void setFov(float f) {
        Aimlock.fov = f;
        ModConfig.aimlockFov = f;
        Reporter.reportSet("Aimlock", "FOV", f);
    }

    public static boolean isHittingBlock() {
        return isHittingBlock;
    }

    public static void setIsHittingBlock(boolean b) {
        Aimlock.isHittingBlock = b;
    }

    public static void setTargetPriority(TargetPriority tp) {
        Aimlock.targetPriority = tp;
        ModConfig.aimlockTargetPriority = tp.ordinal();
        Reporter.reportSet("Aimlock", "Target Priority", tp);
    }

    public static float getActivationRadius() {
        return range;
    }

    public static void setActivationRadius(float range) {
        rangeSqr = range * range;
        Aimlock.range = range;
        ModConfig.aimlockActivationRadius = range;
        Reporter.reportSet("Aimlock", "Activation Radius", range);
    }

    public static Optional<EntityLivingBase> getTargetEntity() {
        return targetEntity;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    // region new acceleration model

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.aimlockEnabled = b;
        Reporter.reportToggled("Aimlock", b);
    }

    public static boolean doesVerticalRotations() {
        return doVerticalRotations;
    }

    public static void setVerticalRotations(boolean b) {
        Aimlock.doVerticalRotations = b;
        ModConfig.aimlockVerticalRotations = b;
        Reporter.reportSet("Aimlock", "Vertical Rotations", b);
    }

    private static Optional<EntityLivingBase> findTarget() {
        Minecraft mc = Minecraft.getMinecraft();

        Optional<EntityLivingBase> bestTarget = Optional.empty();
        if (targetPriority == TargetPriority.INITIAL_HITSCAN && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            if (mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
                bestTarget = Optional.of((EntityLivingBase) mc.objectMouseOver.entityHit);
                if (AntiBot.isRecommendedTarget(bestTarget.get(), rangeSqr)) {
                    return bestTarget;
                }
            }
        }
        if (!bestTarget.isPresent() && targetPriority == TargetPriority.INITIAL_HITSCAN) return Optional.empty();

        // If no target found with initial hitscan or if using a different priority mode
        float lowestHealth = Float.MAX_VALUE;
        double closestAngle = Double.MAX_VALUE;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) || entity == mc.thePlayer) continue;
            EntityLivingBase livingEntity = (EntityLivingBase) entity;

            if (!AntiBot.isRecommendedTarget(livingEntity, rangeSqr)) continue;

            // Check if player's pitch is within the target's hitbox boundaries
            double[] hitboxBounds = calculateHitboxBounds(mc.thePlayer, livingEntity);
            float minPitch = (float) hitboxBounds[2];
            float maxPitch = (float) hitboxBounds[3];
            if (mc.thePlayer.rotationPitch < minPitch || mc.thePlayer.rotationPitch > maxPitch) continue;

            // FOV check
            double[] rotations = getRotationsNeeded(livingEntity);
            double yawDifference = normalizeAngle(rotations[0] - mc.thePlayer.rotationYaw);
            double pitchDifference = normalizeAngle(rotations[1] - mc.thePlayer.rotationPitch);
            double angleDifference = Math.sqrt(yawDifference * yawDifference + pitchDifference * pitchDifference);

            if (angleDifference > fov) continue; // Assuming 'fov' is a defined constant or variable

            switch (targetPriority) {
                case LOWEST_HEALTH:
                    float health = livingEntity.getHealth();
                    if (health < lowestHealth) {
                        lowestHealth = health;
                        bestTarget = Optional.of(livingEntity);
                    }
                    break;
                case CLOSEST_FOV:
                default:
                    if (angleDifference < closestAngle) {
                        closestAngle = angleDifference;
                        bestTarget = Optional.of(livingEntity);
                    }
                    break;
            }
        }

        return bestTarget;
    }

    // Helper method to calculate rotations to a target
    private static double[] getRotationsNeeded(Entity entity) {
        double diffX = entity.posX - Minecraft.getMinecraft().thePlayer.posX;
        double diffZ = entity.posZ - Minecraft.getMinecraft().thePlayer.posZ;
        double diffY = entity.posY + entity.getEyeHeight() - (Minecraft.getMinecraft().thePlayer.posY + Minecraft.getMinecraft().thePlayer.getEyeHeight());

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        return new double[]{yaw, pitch};
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

    private static double normalizeAngle(double angle) {
        angle = angle % 360.0;
        if (angle >= 180.0) {
            angle -= 360.0;
        }
        if (angle < -180.0) {
            angle += 360.0;
        }
        return Math.abs(angle);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {

        if (Main.getAimlockKey().isPressed()) {
            targetEntity = Optional.empty();
            setEnabled(!enabled);
        }

        if (!enabled) return;
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        targetEntity = findTarget();
    }

    @SubscribeEvent
    public void onRenderTick(RenderWorldLastEvent event) {
        if(!Aimlock.isEnabled()) return;
        targetEntity.ifPresent(en -> {
            Renderer.setupRendering();

            float red = targetHitboxColor.getRed() / 255f;
            float green = targetHitboxColor.getGreen() / 255f;
            float blue = targetHitboxColor.getBlue() / 255f;
            float alpha = targetHitboxColor.getRed() / 255f;

            Renderer.drawEntityESP(en, event.partialTicks, red, green, blue, alpha, 0.6f);
            Renderer.resetRendering();
        });

    }

    public static void setTargetHitboxColor(Color color) {
        targetHitboxColor = color;
        ModConfig.aimlockTargetHitboxColor = color;
        Reporter.reportSet("Aimlock", "Target Hitbox Color", color.toString());
    }

    public enum TargetPriority {
        INITIAL_HITSCAN,
        CLOSEST_FOV,
        LOWEST_HEALTH;
    }

    // endregion
}