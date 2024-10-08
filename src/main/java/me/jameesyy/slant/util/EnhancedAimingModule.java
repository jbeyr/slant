package me.jameesyy.slant.util;

import me.jameesyy.slant.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class EnhancedAimingModule {
    private static float MIN_TARGET_ANGULAR_SIZE; // degrees
    private static float BLEND_STRENGTH; // [0.0, 5.0]
    private static float AIM_STRENGTH; // [0.0, 10.0]

    public static void setAimStrength(float strength) {
        AIM_STRENGTH = MathHelper.clamp_float(strength, 0.0f, 10.0f);
        ModConfig.aimStrength = strength;
        Reporter.reportSet("Aimlock", "Aim Strength", strength);
    }

    public static float[] mapRotation(float rawYawDelta, float rawPitchDelta, Entity player, Entity target) {
        double[] anglesToTarget = calculateAnglesToTarget(player, target);
        double yawToTarget = anglesToTarget[0];
        double pitchToTarget = anglesToTarget[1];

        double distanceToTarget = player.getDistanceToEntity(target);
        double angularSize = calculateAngularSize(target, distanceToTarget);

        float aimAssistFactorYaw = calculateAimAssistFactor(yawToTarget, angularSize);
        float aimAssistFactorPitch = calculateAimAssistFactor(pitchToTarget, angularSize);

        float mappedYawDelta = applyMappingFunction(rawYawDelta, yawToTarget, angularSize) * (1 + aimAssistFactorYaw * AIM_STRENGTH * 2);
        float mappedPitchDelta = applyMappingFunction(rawPitchDelta, pitchToTarget, angularSize) * (1 + aimAssistFactorPitch * AIM_STRENGTH * 2);

        float scalingFactor = (float) Math.pow(AIM_STRENGTH, 3);
        mappedYawDelta *= scalingFactor;
        mappedPitchDelta *= scalingFactor;

        return new float[]{mappedYawDelta, mappedPitchDelta};
    }

    private static float applyMappingFunction(float rawDelta, double angleToTarget, double angularSize) {
        double angleDifference = Math.abs(angleToTarget);
        double fullSensitivityThreshold = angularSize * 3;
        double minSensitivityThreshold = angularSize / 3;

        double scaleFactor;

        if (angleDifference > fullSensitivityThreshold) {
            scaleFactor = 1;
        } else if (angleDifference < minSensitivityThreshold) {
            scaleFactor = 0.05;
        } else {
            scaleFactor = 0.05 + (angleDifference - minSensitivityThreshold) / (fullSensitivityThreshold - minSensitivityThreshold) * 0.95;
        }

        float directionToTarget = (float) Math.signum(angleToTarget);
        float adjustedDelta = (float) (rawDelta * scaleFactor * directionToTarget * Math.pow(AIM_STRENGTH, 3));
        float blendFactor = (float) Math.pow(1 - scaleFactor, BLEND_STRENGTH * AIM_STRENGTH * 2);

        return (1 - blendFactor) * rawDelta + blendFactor * adjustedDelta * AIM_STRENGTH * 2;
    }

    private static double[] calculateAnglesToTarget(Entity player, Entity target) {
        double dx = target.posX - player.posX;
        double dy = target.posY + (target.height * 0.9) - (player.posY + player.getEyeHeight());
        double dz = target.posZ - player.posZ;

        double yaw = Math.atan2(dz, dx);
        yaw = Math.toDegrees(yaw) - 90;
        yaw = MathHelper.wrapAngleTo180_double(yaw - player.rotationYaw);

        double pitch = -Math.atan2(dy, Math.sqrt(dx * dx + dz * dz));
        pitch = Math.toDegrees(pitch);
        pitch = MathHelper.wrapAngleTo180_double(pitch - player.rotationPitch);

        return new double[]{yaw, pitch};
    }

    private static double calculateAngularSize(Entity target, double distance) {
        double targetSize = Math.max(target.width, target.height);
        double angularSize = (targetSize / distance) * (180 / Math.PI);

        angularSize *= (1 + Math.pow(AIM_STRENGTH - 1, 3));

        return MathHelper.clamp_double(angularSize, MIN_TARGET_ANGULAR_SIZE, 180 * AIM_STRENGTH);
    }

    private static float calculateAimAssistFactor(double angleToTarget, double angularSize) {
        double normalizedAngle = Math.abs(angleToTarget) / angularSize;
        return (float) (1 - Math.pow(normalizedAngle, AIM_STRENGTH / 2));
    }

    public static void setBlendFactor(float f) {
        BLEND_STRENGTH = f;
        ModConfig.blendFactor = f;
        Reporter.reportSet("Aimlock", "Blend Strength", f);
    }

    public static void setMinTargetAngularSize(float f) {
        MIN_TARGET_ANGULAR_SIZE = f;
        ModConfig.minTargetAngularSize = f;
        Reporter.reportSet("Aimlock", "Min Target Angular Size", f);
    }
}