package me.calclb.aimer.combat;

import me.calclb.aimer.Main;
import me.calclb.aimer.Pointer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Sensitivity {
    private double maxDistanceSq = Math.pow(4.25, 2);
    private double maxAngle = Math.toRadians(13);
    private double reductionFactor = 3/5d;

    private double originalSensitivity;
    private boolean isSensitivityModified;
    private boolean isToggled = false;
    private final Minecraft mc = Minecraft.getMinecraft(); // Minecraft instance

    public Sensitivity() {
        this.originalSensitivity = mc.gameSettings.mouseSensitivity;
        this.isSensitivityModified = false;
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderWorldLastEvent event) {
        EntityPlayer me = mc.thePlayer;
        if (me == null || mc.theWorld == null) return;

        // Toggle the aim assist when the key is pressed
        if (Main.getSenseKey().isPressed()) {
            isToggled = !isToggled;
            sendToggleMessage();
        }


        if (isToggled) {
            EntityPlayer target = Pointer.findClosestAttackablePlayerInRange(me, maxDistanceSq, maxAngle, event.partialTicks);
            if (target != null) {
                setSensitivity(getTargetSensitivity(me, target, event.partialTicks));
                isSensitivityModified = true;
            } else { // restore sensitivity if no target is found
                if (isSensitivityModified) {
                    setSensitivity((float) originalSensitivity);
                    isSensitivityModified = false;
                }
            }
        } else { // restore sensitivity if toggled off
            if (isSensitivityModified) {
                setSensitivity((float) originalSensitivity);
                isSensitivityModified = false;
            }
        }
    }

    private void setSensitivity(float val) {
        // only update original sensitivity if it's not already modified
        if (!isSensitivityModified && Math.abs(mc.gameSettings.mouseSensitivity - originalSensitivity) > 0.001f) {
            originalSensitivity = mc.gameSettings.mouseSensitivity;
        }
        mc.gameSettings.mouseSensitivity = val;

    }

    private float getTargetSensitivity(EntityPlayer me, EntityPlayer target, double partialTicks) {
        Vec3 lookVec = me.getLook(1.0F);

        Vec3 eyePos = new Vec3(me.posX, me.posY + me.getEyeHeight(), me.posZ);
        Vec3 targetVec = Pointer.getNearestPointOnBoxFromMyEyes(target, partialTicks);

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

    private void sendToggleMessage() {
        String message = isToggled ? String.format("Sensitivity toggled ON (x%s, %s rad, %sm)",
                reductionFactor, maxAngle, Math.sqrt(maxDistanceSq)) : "Sensitivity toggled OFF";
        mc.thePlayer.addChatMessage(new ChatComponentText(message));
    }
}