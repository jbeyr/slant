package me.jameesyy.slant.combat;

import me.jameesyy.slant.Main;
import me.jameesyy.slant.util.AntiBot;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class AimAssist {

    public static float getLockAimSpeedMultiplier() {
        return lockAimSpeedMultiplier;
    }

    public static void setLockAimSpeedMultiplier(float lockAimSpeedMultiplier) {
        AimAssist.lockAimSpeedMultiplier = lockAimSpeedMultiplier;
    }

    public static float getLockFov() {
        return lockFov;
    }

    public static void setLockFov(float lockFov) {
        AimAssist.lockFov = lockFov;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Main.getAimAssistKey().isPressed()) {
            setEnabled(!enabled);
        }
    }

    public static boolean isOnlyPlayers() {
        return onlyPlayers;
    }

    public static void setOnlyPlayers(boolean onlyPlayers) {
        AimAssist.onlyPlayers = onlyPlayers;
    }

    public static boolean enabled;
    private static boolean onlyPlayers;


    public static boolean isIncreasedFOVWhileLocked() {
        return increasedFOVWhileLocked;
    }

    public static void setIncreasedFOVWhileLocked(boolean increasedFOVWhileLocked) {
        AimAssist.increasedFOVWhileLocked = increasedFOVWhileLocked;
    }

    public static boolean isConsiderBetterManualAim() {
        return considerBetterManualAim;
    }

    public static void setConsiderBetterManualAim(boolean considerBetterManualAim) {
        AimAssist.considerBetterManualAim = considerBetterManualAim;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        AimAssist.enabled = b;
        Reporter.queueEnableMsg("Aim Assist", b);
    }


    public static float getHorizontalSpeed() {
        return horizontalSpeed;
    }

    public static void setHorizontalSpeed(float horizontalSpeed) {
        AimAssist.horizontalSpeed = horizontalSpeed;
    }

    public static float getVerticalSpeed() {
        return verticalSpeed;
    }

    public static void setVerticalSpeed(float verticalSpeed) {
        AimAssist.verticalSpeed = verticalSpeed;
    }

    public static float getHorizontalMultipoint() {
        return horizontalMultipoint;
    }

    public static void setHorizontalMultipoint(float horizontalMultipoint) {
        AimAssist.horizontalMultipoint = horizontalMultipoint;
    }

    public static float getVerticalMultipoint() {
        return verticalMultipoint;
    }

    public static void setVerticalMultipoint(float verticalMultipoint) {
        AimAssist.verticalMultipoint = verticalMultipoint;
    }

    public static float getPredict() {
        return predict;
    }

    public static void setPredict(float predict) {
        AimAssist.predict = predict;
    }

    public static float getRandomization() {
        return randomization;
    }

    public static void setRandomization(float randomization) {
        AimAssist.randomization = randomization;
    }

    public static float getMinRange() {
        return minRange;
    }

    public static void setMinRange(float minRange) {
        AimAssist.minRange = minRange;
    }

    public static float getMaxRange() {
        return maxRange;
    }

    public static void setMaxRange(float maxRange) {
        AimAssist.maxRange = maxRange;
    }

    public static float getMinFOV() {
        return minFOV;
    }

    public static void setMinFOV(float minFOV) {
        AimAssist.minFOV = minFOV;
    }

    public static float getMaxFOV() {
        return maxFOV;
    }

    public static void setMaxFOV(float maxFOV) {
        AimAssist.maxFOV = maxFOV;
    }

    private static float horizontalSpeed = 2.0f;
    private static float verticalSpeed = 2.0f;
    private static float horizontalMultipoint = 0.5f;
    private static float verticalMultipoint = 0.5f;
    private static float predict = 0.3f;
    private static float randomization = 0.5f;
    private static float lockFov;
    private static float lockAimSpeedMultiplier;

    private static float minRange = 0f;
    private static float maxRange = 4f;
    private static float minFOV = 0f;
    private static float maxFOV = 90f;

    private static boolean increasedFOVWhileLocked;
    private static boolean considerBetterManualAim;

    public static EntityLivingBase getCurrentTarget() {
        return currentTarget;
    }

    public static void setCurrentTarget(EntityLivingBase currentTarget) {
        AimAssist.currentTarget = currentTarget;
    }

    private static EntityLivingBase currentTarget = null;

    public static float getLastPlayerYaw() {
        return lastPlayerYaw;
    }

    public static void setLastPlayerYaw(float lastPlayerYaw) {
        AimAssist.lastPlayerYaw = lastPlayerYaw;
    }

    public static float getLastPlayerPitch() {
        return lastPlayerPitch;
    }

    public static void setLastPlayerPitch(float lastPlayerPitch) {
        AimAssist.lastPlayerPitch = lastPlayerPitch;
    }

    private static float lastPlayerYaw;
    private static float lastPlayerPitch;

    public static EntityLivingBase findBestTarget(float effectiveFOV) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        double bestDistanceSq = Double.MAX_VALUE;
        EntityLivingBase bestTarget = null;

        for (Entity en : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if(!(en instanceof EntityLivingBase)) continue;
            if(!(en instanceof EntityPlayer) && onlyPlayers) continue;
            EntityLivingBase elb = (EntityLivingBase) en;
            if(!AntiBot.isRecommendedTarget(elb)) continue;

            double distanceSq = player.getDistanceSqToEntity(elb);
            if (distanceSq < minRange*minRange || distanceSq > maxRange*maxRange) continue;

            float[] rotations = calculateRotations(elb);
            float yawDifference = Math.abs(MathHelper.wrapAngleTo180_float(rotations[0] - player.rotationYaw));
            float pitchDifference = Math.abs(MathHelper.wrapAngleTo180_float(rotations[1] - player.rotationPitch));

            if (yawDifference > effectiveFOV || pitchDifference > effectiveFOV) continue;
            if (yawDifference < minFOV && pitchDifference < minFOV) continue;

            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                bestTarget = elb;
            }
        }

        return bestTarget;
    }

    public static Vec3 getTargetPosition(EntityLivingBase target, float partialTicks) {
        Vec3 pos = new Vec3(
                target.lastTickPosX + (target.posX - target.lastTickPosX) * partialTicks,
                target.lastTickPosY + (target.posY - target.lastTickPosY) * partialTicks + target.getEyeHeight() / 2,
                target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * partialTicks
        );

        if (predict > 0) {
            // Scale prediction based on target velocity
            double velocity = Math.sqrt(target.motionX * target.motionX + target.motionZ * target.motionZ);
            float scaledPredict = (float) (predict * Math.min(1.0, velocity * 2)); // reduce prediction at lower speeds

            pos = pos.addVector(
                    target.motionX * scaledPredict,
                    target.motionY * scaledPredict,
                    target.motionZ * scaledPredict
            );
        }

        return pos;
    }

    public static Vec3 applyMultipoint(Vec3 targetPos, EntityLivingBase target) {
        AxisAlignedBB bb = target.getEntityBoundingBox();
        Vec3 center = targetPos;
        Vec3 edge = new Vec3(
                horizontalMultipoint > 0.5f ? bb.maxX : bb.minX,
                verticalMultipoint > 0.5f ? bb.maxY : bb.minY,
                horizontalMultipoint > 0.5f ? bb.maxZ : bb.minZ
        );

        return new Vec3(
                center.xCoord + (edge.xCoord - center.xCoord) * horizontalMultipoint,
                center.yCoord + (edge.yCoord - center.yCoord) * verticalMultipoint,
                center.zCoord + (edge.zCoord - center.zCoord) * horizontalMultipoint
        );
    }

    public static Vec3 addRandomization(Vec3 pos) {
        if (randomization <= 0) return pos;

        double spread = randomization * 0.1;
        return pos.addVector(
                (Math.random() - 0.5) * spread,
                (Math.random() - 0.5) * spread,
                (Math.random() - 0.5) * spread
        );
    }

    public static float[] calculateRotations(Vec3 pos) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        double x = pos.xCoord - player.posX;
        double y = pos.yCoord - (player.posY + player.getEyeHeight());
        double z = pos.zCoord - player.posZ;

        double distance = MathHelper.sqrt_double(x * x + z * z);

        float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(y, distance));

        return new float[] {
                MathHelper.wrapAngleTo180_float(yaw),
                MathHelper.wrapAngleTo180_float(pitch)
        };
    }

    public static float[] calculateRotations(Entity entity) {
        return calculateRotations(new Vec3(
                entity.posX,
                entity.posY + entity.getEyeHeight() / 2,
                entity.posZ
        ));
    }

    public static float[] smoothRotationsAndLockOnTarget(float[] desired) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        float yaw = player.rotationYaw;
        float pitch = player.rotationPitch;

        // Get the normalized target yaw
        float targetYaw = desired[0];
        float yawDiff = targetYaw - (yaw % 360.0f);

        // Adjust the difference to take the shortest rotation path
        if (yawDiff > 180.0f) {
            yawDiff -= 360.0f;
        } else if (yawDiff < -180.0f) {
            yawDiff += 360.0f;
        }

        float pitchDiff = MathHelper.wrapAngleTo180_float(desired[1] - pitch);

        // Calculate speeds based on distance
        float yawSpeed = Math.min(Math.abs(yawDiff) * 0.3f, horizontalSpeed);
        float pitchSpeed = Math.min(Math.abs(pitchDiff) * 0.3f, verticalSpeed);

        // FIXME seems to smooth unnaturally when near the center of the target (likely overshooting?)
        // consider leaving it as the desired yaw/pitch directly if it goes past it
        if (isTargetLocked()) {
            yawSpeed *= lockAimSpeedMultiplier;
            pitchSpeed *= lockAimSpeedMultiplier;
        }


        // check if rotations overshoot the target; keep it at the desired rotations if so
        float yawStep = Math.signum(yawDiff) * yawSpeed;
        float pitchStep = Math.signum(pitchDiff) * pitchSpeed;

        // Apply the actual rotations without normalizing the result
//        float newYaw = yaw + (Math.signum(yawDiff) * yawSpeed);
//        float newPitch = MathHelper.clamp_float(pitch + (Math.signum(pitchDiff) * pitchSpeed), -90F, 90F);
        float newYaw, newPitch;


        // if we overshot, just set to desired angle
        if (Math.abs(yawStep) > Math.abs(yawDiff)) {
            newYaw = targetYaw;
        } else {
            newYaw = yaw + yawStep;
        }

        if (Math.abs(pitchStep) > Math.abs(pitchDiff)) {
            newPitch = desired[1];
        } else {
            newPitch = MathHelper.clamp_float(pitch + pitchStep, -90F, 90F);
        }

        return new float[] {newYaw, newPitch};
    }

    public static boolean isTargetLocked() {
        if (currentTarget == null) return false;
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        float[] targetRotations = calculateRotations(currentTarget);

        // check if current aim is within a certain threshold/degrees from target
        float yawDiff = Math.abs(MathHelper.wrapAngleTo180_float(targetRotations[0] - player.rotationYaw));
        float pitchDiff = Math.abs(MathHelper.wrapAngleTo180_float(targetRotations[1] - player.rotationPitch));

        return yawDiff <= lockFov && pitchDiff <= lockFov;
    }
}