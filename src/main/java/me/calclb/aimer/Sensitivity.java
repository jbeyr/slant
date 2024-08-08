package me.calclb.aimer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Sensitivity {
    private static final double MAX_DISTANCE_SQ = Math.pow(5, 2);
    private static final double MAX_ANGLE = Math.toRadians(0);
    private static final double REDUCTION_FACTOR = 1 / 3d;

    private double originalSensitivity;
    private boolean defaultSenseHasBeenOverridden;
    private boolean isSensitivityModified = false;
    private boolean isFadingOut = false;
    private boolean hasTarget = false;


    public Sensitivity() {
        Minecraft mc = Minecraft.getMinecraft();
        this.originalSensitivity = mc.gameSettings.mouseSensitivity;
        this.isSensitivityModified = false;
    }

    public boolean hasSenseBeenOverridden() {
        boolean ret = defaultSenseHasBeenOverridden;
        if (defaultSenseHasBeenOverridden) defaultSenseHasBeenOverridden = false;
        return ret;
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer me = mc.thePlayer;
        if (me == null || mc.theWorld == null) return;

        long currentTime = System.currentTimeMillis();
        boolean keyPressed = Main.getSenseKey().isKeyDown();

        EntityPlayer targetPlayer = keyPressed ? findClosestPlayerInRange(me) : null;

        if (targetPlayer != null) {
            float targetSensitivity = getTargetSensitivity(me, targetPlayer);
            setSensitivity(targetSensitivity);
            isSensitivityModified = true;
            isFadingOut = false;
            hasTarget = true;
        } else if (hasTarget || !keyPressed) {
            // Start fading out if we lost the target or the key was released
            if (isSensitivityModified) {
                setSensitivity((float) originalSensitivity);
                isSensitivityModified = false;
            }
            if (!isFadingOut) {
                isFadingOut = true;
            }
            hasTarget = false;
        }
    }

    private void setSensitivity(float val) {
        if (!isSensitivityModified && Math.abs(Minecraft.getMinecraft().gameSettings.mouseSensitivity - originalSensitivity) > 0.001f) {
            originalSensitivity = Minecraft.getMinecraft().gameSettings.mouseSensitivity;
        }
        Minecraft.getMinecraft().gameSettings.mouseSensitivity = val;
    }

    private float getTargetSensitivity(EntityPlayer me, EntityPlayer target) {
        Vec3 lookVec = me.getLook(1.0F);
        AxisAlignedBB targetBox = target.getEntityBoundingBox();

        Vec3 eyePos = new Vec3(me.posX, me.posY + me.getEyeHeight(), me.posZ);
        Vec3 targetVec = getNearestPointOnBox(eyePos, lookVec, targetBox);

        double dx = targetVec.xCoord - eyePos.xCoord;
        double dy = targetVec.yCoord - eyePos.yCoord;
        double dz = targetVec.zCoord - eyePos.zCoord;

        double distanceSq = dx * dx + dy * dy + dz * dz;

        Vec3 toTargetVec = new Vec3(dx, dy, dz);
        double lookDotTarget = lookVec.dotProduct(toTargetVec);
        double lookLengthSq = lookVec.lengthVector();
        double angle = Math.acos(lookDotTarget / (Math.sqrt(lookLengthSq * distanceSq)));

        double angleFactor = angle / MAX_ANGLE;
        double distanceFactor = Math.sqrt(distanceSq / MAX_DISTANCE_SQ);
        double combinedFactor = Math.max(angleFactor, distanceFactor);

        return (float) (originalSensitivity * (REDUCTION_FACTOR + (1 - REDUCTION_FACTOR) * combinedFactor));
    }

    private Vec3 getNearestPointOnBox(Vec3 origin, Vec3 direction, AxisAlignedBB box) {
        double[] min = {box.minX, box.minY, box.minZ};
        double[] max = {box.maxX, box.maxY, box.maxZ};
        double[] orig = {origin.xCoord, origin.yCoord, origin.zCoord};
        double[] dir = {direction.xCoord, direction.yCoord, direction.zCoord};

        double tmin = Double.NEGATIVE_INFINITY;
        double tmax = Double.POSITIVE_INFINITY;

        for (int i = 0; i < 3; i++) {
            if (Math.abs(dir[i]) < 1e-8) {
                if (orig[i] < min[i] || orig[i] > max[i]) {
                    return null;
                }
            } else {
                double ood = 1.0 / dir[i];
                double t1 = (min[i] - orig[i]) * ood;
                double t2 = (max[i] - orig[i]) * ood;

                if (t1 > t2) {
                    double temp = t1;
                    t1 = t2;
                    t2 = temp;
                }

                tmin = Math.max(tmin, t1);
                tmax = Math.min(tmax, t2);

                if (tmin > tmax) {
                    return null;
                }
            }
        }

        return new Vec3(
                origin.xCoord + direction.xCoord * tmin,
                origin.yCoord + direction.yCoord * tmin,
                origin.zCoord + direction.zCoord * tmin
        );
    }

    private EntityPlayer findClosestPlayerInRange(EntityPlayer me) {
        EntityPlayer closestPlayer = null;
        double closestAngle = Double.MAX_VALUE;

        for (EntityPlayer otherPlayer : me.worldObj.playerEntities) {
            if (otherPlayer == me) continue;

            Vec3 lookVec = me.getLook(1.0F);
            AxisAlignedBB targetBox = otherPlayer.getEntityBoundingBox();
            Vec3 eyePos = new Vec3(me.posX, me.posY + me.getEyeHeight(), me.posZ);
            Vec3 targetVec = getNearestPointOnBox(eyePos, lookVec, targetBox);

            if (targetVec == null) continue;

            double dx = targetVec.xCoord - eyePos.xCoord;
            double dy = targetVec.yCoord - eyePos.yCoord;
            double dz = targetVec.zCoord - eyePos.zCoord;

            double distanceSq = dx * dx + dy * dy + dz * dz;
            if (distanceSq > MAX_DISTANCE_SQ) continue;

            Vec3 toTargetVec = new Vec3(dx, dy, dz);
            double lookDotTarget = lookVec.dotProduct(toTargetVec);
            double lookLengthSq = lookVec.lengthVector();

            double angle = Math.acos(lookDotTarget / (Math.sqrt(lookLengthSq * distanceSq)));

            if (angle < closestAngle) {
                closestAngle = angle;
                closestPlayer = otherPlayer;
            }
        }

        return (closestAngle <= MAX_ANGLE) ? closestPlayer : null;
    }
}