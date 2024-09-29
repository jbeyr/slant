package me.calclb.aimer.mixins;

import me.calclb.aimer.Main;
import me.calclb.aimer.combat.Aimlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin {

    @Unique
    private static final float ROTATION_SPEED = 0.15f;

    @Unique
    private static final float MAX_YAW_TICK_ROTATION = 40f;

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void onUpdateWalkingPlayer(CallbackInfo ci) {
        EntityPlayerSP player = (EntityPlayerSP) (Object) this;
        Minecraft mc = Minecraft.getMinecraft();

        if (Aimlock.isToggled() && Aimlock.isInAValidStateToAim() && Aimlock.getTargetEntity() != null && mc.gameSettings.thirdPersonView != 2) {
            Entity target = Aimlock.getTargetEntity();

            double[] hitboxBounds = Aimlock.calculateHitboxBounds(player, target);
            float minYaw = (float) hitboxBounds[0];
            float maxYaw = (float) hitboxBounds[1];
            float minPitch = (float) hitboxBounds[2];
            float maxPitch = (float) hitboxBounds[3];

            float currentYaw = player.rotationYaw;
            float centerYaw = (minYaw + maxYaw) / 2;
            System.out.println("-------------------------------------------------------");
            System.out.println("currentYaw: " + currentYaw);

            // Calculate yaw difference considering multiple rotations
            float yawDiff = (centerYaw - currentYaw) % 360;
            if (yawDiff > 180) yawDiff -= 360;
            if (yawDiff < -180) yawDiff += 360;

            // Limit the yaw rotation to prevent sudden turns
            yawDiff = (float) Aimlock.clamp(yawDiff, -MAX_YAW_TICK_ROTATION, MAX_YAW_TICK_ROTATION);

            float targetPitch = (float) Aimlock.clamp((minPitch + maxPitch) / 2, -90, 90);
            float pitchDiff = targetPitch - player.rotationPitch;

            player.rotationYaw += yawDiff * ROTATION_SPEED;
            player.rotationPitch += pitchDiff * ROTATION_SPEED;
        }
    }
}