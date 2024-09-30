package me.jameesyy.slant.mixins;

import me.jameesyy.slant.combat.Aimlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void onUpdateWalkingPlayer(CallbackInfo ci) {
        EntityPlayerSP player = (EntityPlayerSP) (Object) this;
        Minecraft mc = Minecraft.getMinecraft();

        if(!Aimlock.isEnabled()) return;

        if (Aimlock.isInAValidStateToAim() && Aimlock.getTargetEntity() != null && mc.gameSettings.thirdPersonView != 2) {
            Entity target = Aimlock.getTargetEntity();

            double[] hitboxBounds = Aimlock.calculateHitboxBounds(player, target);
            float minYaw = (float) hitboxBounds[0];
            float maxYaw = (float) hitboxBounds[1];
            float minPitch = (float) hitboxBounds[2];
            float maxPitch = (float) hitboxBounds[3];

            float currentYaw = player.rotationYaw;
            float centerYaw = (minYaw + maxYaw) / 2;

            // Calculate yaw difference considering multiple rotations
            float yawDiff = (centerYaw - currentYaw) % 360;
            if (yawDiff > 180) yawDiff -= 360;
            if (yawDiff < -180) yawDiff += 360;

            // Limit the yaw rotation to prevent sudden turns
            float maxYawTickRotation = Aimlock.getMaxYawTickRotation();
            yawDiff = (float) Aimlock.clamp(yawDiff, -maxYawTickRotation, maxYawTickRotation);

            float targetPitch = (float) Aimlock.clamp((minPitch + maxPitch) / 2, -90, 90);
            float pitchDiff = targetPitch - player.rotationPitch;

            float rotationSpeed = Aimlock.getRotationSpeed();
            player.rotationYaw += yawDiff * rotationSpeed;
            if(Aimlock.shouldDoVerticalRotations()) player.rotationPitch += pitchDiff * rotationSpeed;
        }
    }


}