package me.jameesyy.slant.util;

import me.jameesyy.slant.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class EnhancedAimingModule {
    private static float MIN_TARGET_ANGULAR_SIZE; // degrees
    private static float BLEND_STRENGTH;
    private static float AIM_STRENGTH = 1.0f; // Adjustable between 0.0 and 2.0, for example

    public static void setAimStrength(float strength) {
        AIM_STRENGTH = MathHelper.clamp_float(strength, 0.0f, 2.0f);
        ModConfig.aimStrength = strength;
        Reporter.reportSet("Aimlock", "Aim Strength", strength);
    }


    public static float[] mapRotation(float rawYawDelta, float rawPitchDelta, Entity player, Entity target) {
        // Calculate angles to target
        double[] anglesToTarget = calculateAnglesToTarget(player, target);
        double yawToTarget = anglesToTarget[0];
        double pitchToTarget = anglesToTarget[1];

        // Calculate distance to target
        double distanceToTarget = player.getDistanceToEntity(target);

        // Calculate angular size of target
        double angularSize = calculateAngularSize(target, distanceToTarget);

        // Apply mapping function for both yaw and pitch
        float mappedYawDelta = applyMappingFunction(rawYawDelta, yawToTarget, angularSize);
        float mappedPitchDelta = applyMappingFunction(rawPitchDelta, pitchToTarget, angularSize);

        // Apply an additional scaling based on AIM_STRENGTH
        float scalingFactor = 1 + (AIM_STRENGTH - 1) * 0.25f;
        mappedYawDelta *= scalingFactor;
        mappedPitchDelta *= scalingFactor;

        return new float[]{mappedYawDelta, mappedPitchDelta};
    }

    private static float applyMappingFunction(float rawDelta, double angleToTarget, double angularSize) {
        // Calculate the absolute angle difference
        double angleDifference = Math.abs(angleToTarget);

        // Define the range where we want to start decreasing sensitivity
        double fullSensitivityThreshold = angularSize * 2; // Adjust this multiplier as needed
        double minSensitivityThreshold = angularSize / 2; // Adjust this divisor as needed

        double scaleFactor;

        if (angleDifference > fullSensitivityThreshold) {
            // Normal sensitivity when far from target
            scaleFactor = 1;
        } else if (angleDifference < minSensitivityThreshold) {
            // Minimum sensitivity when very close to target
            scaleFactor = 0.1; // Adjust this minimum value as needed
        } else {
            // Gradually decrease sensitivity as we get closer to the target
            scaleFactor = 0.1 + (angleDifference - minSensitivityThreshold) / (fullSensitivityThreshold - minSensitivityThreshold) * 0.9;
        }

        // Calculate the direction towards the target
        float directionToTarget = (float) Math.signum(angleToTarget);

        // Blend between raw input and adjusted input based on how close we are to the target
        float adjustedDelta = (float) (rawDelta * scaleFactor * directionToTarget * AIM_STRENGTH);
        float blendFactor = (float) Math.pow(1 - scaleFactor, BLEND_STRENGTH * AIM_STRENGTH);
        return (1 - blendFactor) * rawDelta + blendFactor * adjustedDelta;
    }

    private static double[] calculateAnglesToTarget(Entity player, Entity target) {
        double dx = target.posX - player.posX;
        double dy = target.posY + (target.height * 0.85) - (player.posY + player.getEyeHeight());
        double dz = target.posZ - player.posZ;

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Calculate yaw
        double yaw = Math.atan2(dz, dx);
        yaw = Math.toDegrees(yaw) - 90;
        yaw = MathHelper.wrapAngleTo180_double(yaw - player.rotationYaw);

        // Calculate pitch
        double pitch = -Math.atan2(dy, Math.sqrt(dx * dx + dz * dz));
        pitch = Math.toDegrees(pitch);
        pitch = MathHelper.wrapAngleTo180_double(pitch - player.rotationPitch);

        return new double[]{yaw, pitch};
    }

    private static double calculateAngularSize(Entity target, double distance) {
        double targetSize = Math.max(target.width, target.height);
        double angularSize = (targetSize / distance) * (180 / Math.PI);
        angularSize *= (1 + (AIM_STRENGTH - 1) * 0.5);

        return MathHelper.clamp_double(angularSize, MIN_TARGET_ANGULAR_SIZE, 45 * AIM_STRENGTH);
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