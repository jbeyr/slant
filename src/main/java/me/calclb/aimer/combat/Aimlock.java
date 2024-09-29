package me.calclb.aimer.combat;

import me.calclb.aimer.AntiBot;
import me.calclb.aimer.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Aimlock {
    private static boolean toggled;
    private static EntityLivingBase targetEntity;
    private static double rangeSqr = Math.pow(4.25f, 2);

    // Getter methods
    public static boolean isToggled() {
        return toggled;
    }

    public static EntityLivingBase getTargetEntity() {
        return targetEntity;
    }

    // Toggle method
    public static void toggle() {
        toggled = !toggled;
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Aimlock toggled: " + toggled));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        // Toggle aimlock and reset target when key is pressed
        if (Main.getAimlockKey().isPressed()) {
            targetEntity = null;
            toggle();
        }

        if (!toggled) return;
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
        return toggled;
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