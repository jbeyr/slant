package me.jameesyy.slant.mixins;

import me.jameesyy.slant.Targeter;
import me.jameesyy.slant.combat.AimAssist;
import me.jameesyy.slant.movement.Safewalk;
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
    }

    @Unique
    private void slant$handleAimAssist(EntityPlayerSP player) {
        if (!AimAssist.isAimAssistingAllowed()) return;

        // invariant: target exists if the check above passes
        EntityLivingBase target = Targeter.getTarget().get();

        double[] hitboxBounds = AimAssist.calculateHitboxBounds(player, target);
        float minYaw = (float) hitboxBounds[0];
        float maxYaw = (float) hitboxBounds[1];
        float minPitch = (float) hitboxBounds[2];
        float maxPitch = (float) hitboxBounds[3];

        float currentYaw = player.rotationYaw;
        float centerYaw = (minYaw + maxYaw) / 2;

        // normalize yaw
        float yawDiff = (centerYaw - currentYaw) % 360;
        if (yawDiff > 180) yawDiff -= 360;
        if (yawDiff < -180) yawDiff += 360;

        float maxYawTickRotation = AimAssist.getMaxYawTickRotation();
        yawDiff = (float) AimAssist.clamp(yawDiff, -maxYawTickRotation, maxYawTickRotation);

        float targetPitch = (float) AimAssist.clamp((minPitch + maxPitch) / 2, -90, 90);
        float pitchDiff = targetPitch - player.rotationPitch;

        // don't aim if within "acceptable" fov
        double angleDifference = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
        if (angleDifference <= AimAssist.getClosenessThreshold()) return;


        float rotationSpeed = AimAssist.getRotationSpeed();
        player.rotationYaw += yawDiff * rotationSpeed;
        if(AimAssist.shouldDoVerticalRotations()) player.rotationPitch += pitchDiff * rotationSpeed;
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"), cancellable = true)
    private void onLivingUpdate(CallbackInfo ci) {
        EntityPlayerSP player = (EntityPlayerSP) (Object) this;

        // use adjusted/corrected aimassist yaw/pitch and then let physics engine handle the rest
        if (AimAssist.isAimAssistingAllowed()) slant$handleAimAssist(player);
    }
}