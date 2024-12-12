package me.jameesyy.slant.mixins;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.combat.AimAssist;
import me.jameesyy.slant.movement.Safewalk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void onUpdateWalkingPlayer(CallbackInfo ci) {
        EntityPlayerSP player = (EntityPlayerSP) (Object) this;
        Safewalk.setLastMovementInput(player.movementInput);

        if (ActionConflictResolver.isRotatingAllowed() && !player.isUsingItem() && !AimAssist.isHittingBlock()) slant$handleAimAssist(player);
    }

    @Unique
    private void slant$handleAimAssist(EntityPlayerSP player) {

        if(!AimAssist.isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (AimAssist.isInAValidStateToAim() && AimAssist.getTargetEntity().isPresent() && mc.gameSettings.thirdPersonView != 2) {
            EntityLivingBase target = AimAssist.getTargetEntity().get();
            double[] hitboxBounds = AimAssist.calculateHitboxBounds(player, target);
            float minYaw = (float) hitboxBounds[0];
            float maxYaw = (float) hitboxBounds[1];
            float minPitch = (float) hitboxBounds[2];
            float maxPitch = (float) hitboxBounds[3];

            float currentYaw = player.rotationYaw;
            float centerYaw = (minYaw + maxYaw) / 2;

            float yawDiff = (centerYaw - currentYaw) % 360;
            if (yawDiff > 180) yawDiff -= 360;
            if (yawDiff < -180) yawDiff += 360;

            float maxYawTickRotation = AimAssist.getMaxYawTickRotation();
            yawDiff = (float) AimAssist.clamp(yawDiff, -maxYawTickRotation, maxYawTickRotation);

            float targetPitch = (float) AimAssist.clamp((minPitch + maxPitch) / 2, -90, 90);
            float pitchDiff = targetPitch - player.rotationPitch;

            float rotationSpeed = AimAssist.getRotationSpeed();
            player.rotationYaw += yawDiff * rotationSpeed;
            if(AimAssist.shouldDoVerticalRotations()) player.rotationPitch += pitchDiff * rotationSpeed;
        }
    }
}