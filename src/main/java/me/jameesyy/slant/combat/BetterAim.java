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

public class BetterAim {
    private static Optional<EntityLivingBase> targetEntity = Optional.empty();
    private static boolean enabled;
    private static boolean doVerticalRotations;
    private static float range;
    private static TargetPriority targetPriority;
    private static float rangeSqr;
    private static float fov;
    private static Color targetHitboxColor;

    public static void setFov(float f) {
        BetterAim.fov = f;
        ModConfig.betterAimFov = f;
        Reporter.queueSetMsg("Better Aim", "FOV", f);
    }

    public static void setTargetPriority(TargetPriority tp) {
        BetterAim.targetPriority = tp;
        ModConfig.betterAimTargetPriority = tp.ordinal();
        Reporter.queueSetMsg("Better Aim", "Target Priority", tp);
    }

    public static float getActivationRadius() {
        return range;
    }

    public static void setActivationRadius(float range) {
        rangeSqr = range * range;
        BetterAim.range = range;
        ModConfig.betterAimActivationRadius = range;
        Reporter.queueSetMsg("Better Aim", "Activation Radius", range);
    }

    public static Optional<EntityLivingBase> getTargetEntity() {
        return targetEntity;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.betterAimEnabled = b;
        Reporter.queueReportMsg("Better Aim", b);
    }

    public static boolean doesVerticalRotations() {
        return doVerticalRotations;
    }

    public static void setVerticalRotations(boolean b) {
        BetterAim.doVerticalRotations = b;
        ModConfig.betterAimVerticalRotations = b;
        Reporter.queueSetMsg("Better Aim", "Vertical Rotations", b);
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

        float lowestHealth = Float.MAX_VALUE;
        double closestAngle = Double.MAX_VALUE;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) || entity == mc.thePlayer) continue;
            EntityLivingBase livingEntity = (EntityLivingBase) entity;

            if (!AntiBot.isRecommendedTarget(livingEntity, rangeSqr)) continue;

            double[] hitboxBounds = calculateHitboxBounds(mc.thePlayer, livingEntity);
            float minPitch = (float) hitboxBounds[2];
            float maxPitch = (float) hitboxBounds[3];
            if (mc.thePlayer.rotationPitch < minPitch || mc.thePlayer.rotationPitch > maxPitch) continue;

            double[] rotations = getRotationsNeeded(livingEntity);
            double yawDifference = normalizeAngle(rotations[0] - mc.thePlayer.rotationYaw);
            double pitchDifference = normalizeAngle(rotations[1] - mc.thePlayer.rotationPitch);
            double angleDifference = Math.sqrt(yawDifference * yawDifference + pitchDifference * pitchDifference);

            if (angleDifference > fov) continue;

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

    /**
     * @param entity The target entity
     * @return a 2-element array <code>[yaw, pitch]</code> describing the rotation delta needed to face a target
     */
    private static double[] getRotationsNeeded(Entity entity) {
        double diffX = entity.posX - Minecraft.getMinecraft().thePlayer.posX;
        double diffZ = entity.posZ - Minecraft.getMinecraft().thePlayer.posZ;
        double diffY = entity.posY + entity.getEyeHeight() - (Minecraft.getMinecraft().thePlayer.posY + Minecraft.getMinecraft().thePlayer.getEyeHeight());

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));

        return new double[]{yaw, pitch};
    }

    /**
     * @param player the avatar to rotate
     * @param target the entity to derive the hitbox from
     * @return a 4-element array <code>[minYawDelta, maxYawDelta, minPitchDelta, maxPitchDelta]</code> describing the acceptable range of yaw and pitch to position the cursor over the target hitbox
     */
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

    /**
     * @param angle the angle, in degrees
     * @return <code>angle</code>, clamped between <code>[-180, 180)</code> degrees.
     */
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

        if (Main.getBetterAimKey().isPressed()) {
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
        if(!BetterAim.isEnabled()) return;
        targetEntity.ifPresent(en -> {
            float red = targetHitboxColor.getRed() / 255f;
            float green = targetHitboxColor.getGreen() / 255f;
            float blue = targetHitboxColor.getBlue() / 255f;
            float alpha = targetHitboxColor.getRed() / 255f;
            Renderer.draw3dEntityESP(en, event.partialTicks, red, green, blue, alpha);
        });

    }

    public static void setTargetHitboxColor(Color color) {
        targetHitboxColor = color;
        ModConfig.betterAimTargetHitboxColor = color;
        Reporter.queueSetMsg("Better Aim", "Target Hitbox Color", color.toString());
    }

    public enum TargetPriority {
        INITIAL_HITSCAN,
        CLOSEST_FOV,
        LOWEST_HEALTH;
    }
}