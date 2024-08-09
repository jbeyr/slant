package me.calclb.aimer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Sensitivity {
    private double maxDistanceSq = Math.pow(5, 2);
    private double maxAngle = Math.toRadians(13);
    private double reductionFactor = 2/5d;

    private double originalSensitivity;
    private boolean isSensitivityModified = false;

    public Sensitivity() {
        Minecraft mc = Minecraft.getMinecraft();
        this.originalSensitivity = mc.gameSettings.mouseSensitivity;
        this.isSensitivityModified = false;
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer me = mc.thePlayer;
        if (me == null || mc.theWorld == null) return;

        boolean keyPressed = Main.getSenseKey().isKeyDown();
        EntityPlayer target = keyPressed ? Pointer.findClosestPlayerInRange(me, maxDistanceSq, maxAngle) : null;
        if (target != null) {
            setSensitivity(getTargetSensitivity(me, target));
            isSensitivityModified = true;
        } else {
            if (isSensitivityModified) {
                setSensitivity((float) originalSensitivity);
                isSensitivityModified = false;
            }
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
        Vec3 targetVec = Pointer.getNearestPointOnBox(eyePos, targetBox);

        double dx = targetVec.xCoord - eyePos.xCoord;
        double dy = targetVec.yCoord - eyePos.yCoord;
        double dz = targetVec.zCoord - eyePos.zCoord;

        double distanceSq = dx * dx + dy * dy + dz * dz;

        Vec3 toTargetVec = new Vec3(dx, dy, dz);
        double lookDotTarget = lookVec.dotProduct(toTargetVec);
        double lookLengthSq = lookVec.lengthVector();
        double angle = Math.acos(lookDotTarget / (Math.sqrt(lookLengthSq * distanceSq)));

        double angleFactor = angle / maxAngle;
        double distanceFactor = Math.sqrt(distanceSq / maxDistanceSq);
        double combinedFactor = Math.max(angleFactor, distanceFactor);

        return (float) (originalSensitivity * (reductionFactor + (1 - reductionFactor) * combinedFactor));
    }
}