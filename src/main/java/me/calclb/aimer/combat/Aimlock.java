package me.calclb.aimer.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Aimlock {
    // TODO implement

    private Entity targetEntity;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null && mc.objectMouseOver != null) {
                if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                    targetEntity = mc.objectMouseOver.entityHit;
                } else {
                    targetEntity = null;
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        if (targetEntity != null && targetEntity.isEntityAlive()) {
            Minecraft mc = Minecraft.getMinecraft();
            double deltaYaw = event.dx * 0.15;  // Adjust sensitivity as needed
            double deltaPitch = -event.dy * 0.15;  // Negative because Minecraft inverts pitch

            // Calculate the angle to the target
            double[] angles = calculateAngles(targetEntity);
            double targetYaw = angles[0];
            double targetPitch = angles[1];

            // Calculate new rotations
            double newYaw = mc.thePlayer.rotationYaw + deltaYaw;
            double newPitch = mc.thePlayer.rotationPitch + deltaPitch;

            // Check if the new rotation would move outside the target's hitbox
            if (Math.abs(newYaw - targetYaw) > 5 || Math.abs(newPitch - targetPitch) > 5) {
                // Limit the rotation to stay within bounds
                newYaw = limitAngle(newYaw, targetYaw, 5);
                newPitch = limitAngle(newPitch, targetPitch, 5);

                // Apply the limited rotation
                mc.thePlayer.rotationYaw = (float) newYaw;
                mc.thePlayer.rotationPitch = (float) newPitch;

                // Cancel the original mouse event
                event.setCanceled(true);
            }
        }
    }

    private double limitAngle(double current, double target, double limit) {
        double delta = Math.abs(current - target);
        if (delta > limit) {
            if (current > target) {
                return target + limit;
            } else {
                return target - limit;
            }
        }
        return current;
    }

    private double[] calculateAngles(Entity entity) {
        Minecraft mc = Minecraft.getMinecraft();
        double x = entity.posX - mc.thePlayer.posX;
        double y = entity.posY + entity.getEyeHeight() - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double z = entity.posZ - mc.thePlayer.posZ;

        double dist = Math.sqrt(x * x + z * z);

        double yaw = Math.toDegrees(Math.atan2(z, x)) - 90;
        double pitch = -Math.toDegrees(Math.atan2(y, dist));

        return new double[]{yaw, pitch};
    }
}