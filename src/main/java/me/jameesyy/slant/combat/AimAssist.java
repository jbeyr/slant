package me.jameesyy.slant.combat;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.Main;
import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.Targeter;
import me.jameesyy.slant.util.AntiBot;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.Optional;

public class AimAssist {
    private static float range;
    private static float rangeSqr;
    private static float maxYawTickRotation;
    private static float rotationSpeed;
    private static boolean doVerticalRotations;
    private static boolean enabled;
    private static TargetPriority targetPriority;
    private static boolean isHittingBlock;
    private static float closenessThreshold;
    private static boolean whenLeftMouseButtonHeldDown;
    private static boolean whenMovingForward;

    public static float getFov() {
        return fov;
    }

    public static void setFov(float f) {
        AimAssist.fov = f;
        ModConfig.aimAssistFov = f;
        Reporter.queueSetMsg("Aim Assist", "FOV", f);
    }

    private static float fov = 60f;

    public static boolean isHittingBlock() {
        return isHittingBlock;
    }

    public static void setIsHittingBlock(boolean b) {
        AimAssist.isHittingBlock = b;
    }

    public static void setClosenessThreshold(float f) {
        AimAssist.closenessThreshold = f;
        ModConfig.aimAssistClosenessThreshold = f;
        Reporter.queueSetMsg("Aim Assist", "Closeness FOV", f);
    }

    public static float getClosenessThreshold() {
        return closenessThreshold;
    }

    public static void setWhenLeftMouseButtonHeldDown(boolean b) {
        ModConfig.aimAssistWhenLeftMouseButtonHeldDown = b;
        AimAssist.whenLeftMouseButtonHeldDown = b;
        Reporter.queueSetMsg("Aim Assist", "When Left Mouse Button Held Down", b);

    }

    public static void setWhenMovingForward(boolean whenMovingForward) {
        AimAssist.whenMovingForward = whenMovingForward;
    }

    public enum TargetPriority {
        INITIAL_HITSCAN("Initial Hitscan"),
        CLOSEST_FOV("Closest FOV"),
        LOWEST_HEALTH("Lowest Health");

        private final String configName;

        TargetPriority(String configName) {
            this.configName = configName;
        }

        @Override
        public String toString() {
            return configName;
        }
    }

    public static void setTargetPriority(TargetPriority tp) {
        AimAssist.targetPriority = tp;
        ModConfig.aimAssistTargetPriority = tp.ordinal();
        Reporter.queueSetMsg("Aim Assist", "Target Priority", tp);
    }

    public static void setActivationRadius(float range) {
        rangeSqr = range * range;
        AimAssist.range = range;
        ModConfig.aimAssistActivationRadius = range;
        Reporter.queueSetMsg("Aim Assist", "Activation Radius", range);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.aimAssistEnabled = b;
        Reporter.queueEnableMsg("Aim Assist", b);
    }

    public static float getMaxYawTickRotation() {
        return maxYawTickRotation;
    }

    public static void setMaxYawTickRotation(float maxtickrot) {
        AimAssist.maxYawTickRotation = maxtickrot;
        ModConfig.aimAssistMaxYawTickRotation = maxtickrot;
        Reporter.queueSetMsg("Aim Assist", "Max Yaw Tick Rotation", maxtickrot);
    }

    public static float getRotationSpeed() {
        return rotationSpeed;
    }

    public static void setRotationSpeed(float rotationSpeed) {
        AimAssist.rotationSpeed = rotationSpeed;
        ModConfig.aimAssistRotationSpeed = rotationSpeed;
        Reporter.queueSetMsg("Aim Assist", "Rotation Speed", rotationSpeed);
    }

    public static boolean shouldDoVerticalRotations() {
        return doVerticalRotations;
    }

    public static void setVerticalRotations(boolean b) {
        AimAssist.doVerticalRotations = b;
        ModConfig.aimAssistVerticalRotations = b;
        Reporter.queueSetMsg("Aim Assist", "Vertical Rotations", b);
    }

    private static Optional<EntityLivingBase> findTarget() {
        Minecraft mc = Minecraft.getMinecraft();

        Optional<EntityLivingBase> bestTarget = Optional.empty();
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
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

    /**
     * @param entity the target
     * @return a 2-element array (yaw, pitch) needed to rotate to a target
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

    public static boolean isAimAssistingAllowed() {
        Minecraft mc = Minecraft.getMinecraft();
        return ActionConflictResolver.isRotatingAllowed()
                && enabled
                && !isHittingBlock()
                && Targeter.getTarget().isPresent()
                && !mc.thePlayer.isUsingItem()
                && mc.gameSettings.thirdPersonView != 2
                && (Mouse.isButtonDown(0) || !whenLeftMouseButtonHeldDown)
                && (mc.gameSettings.keyBindForward.isKeyDown() || !whenMovingForward);
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

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Main.getAimAssistKey().isPressed()) setEnabled(!enabled);
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
}