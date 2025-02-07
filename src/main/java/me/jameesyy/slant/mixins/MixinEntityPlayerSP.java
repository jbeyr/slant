package me.jameesyy.slant.mixins;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.combat.AimAssist;
import me.jameesyy.slant.movement.Safewalk;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.jameesyy.slant.combat.AimAssist.*;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void onUpdateWalkingPlayer(CallbackInfo ci) {
        EntityPlayerSP player = (EntityPlayerSP) (Object) this;
        Safewalk.setLastMovementInput(player.movementInput);
    }

    @Unique
    private void slant$handleAimAssist(EntityPlayerSP player) {
        float originalYaw = player.rotationYaw;
        float originalPitch = player.rotationPitch;

        float effectiveFOV = AimAssist.getCurrentTarget() != null && AimAssist.isIncreasedFOVWhileLocked()
                ? AimAssist.getMaxFOV() * 1.5f
                : AimAssist.getMaxFOV();

        EntityLivingBase target = AimAssist.findBestTarget(effectiveFOV);

        AimAssist.setCurrentTarget(target);
        if (target == null) return;

        // Calculate predicted position with speed-based prediction scaling
        Vec3 targetPos = AimAssist.getTargetPosition(target, 1.0f);

        // Get current rotations to target before prediction
        float[] baseRotations = AimAssist.calculateRotations(targetPos);

        // Apply prediction
        targetPos = AimAssist.applyMultipoint(targetPos, target);
        targetPos = AimAssist.addRandomization(targetPos);

        float[] rotations = AimAssist.calculateRotations(targetPos);

        // prevent excessive rotation changes from prediction
        float maxPredictionDelta = 45.0f; // Maximum allowed change from prediction
        float yawDiff = MathHelper.wrapAngleTo180_float(rotations[0] - baseRotations[0]);
        float pitchDiff = MathHelper.wrapAngleTo180_float(rotations[1] - baseRotations[1]);

        if (Math.abs(yawDiff) > maxPredictionDelta) {
            rotations[0] = baseRotations[0] + (maxPredictionDelta * Math.signum(yawDiff));
        }
        if (Math.abs(pitchDiff) > maxPredictionDelta) {
            rotations[1] = baseRotations[1] + (maxPredictionDelta * Math.signum(pitchDiff));
        }


        if (AimAssist.isConsiderBetterManualAim()) {
            // calculate yaw differences considering wraparound
            float manualYawDiff = player.rotationYaw - AimAssist.getLastPlayerYaw();
            float targetYawDiff = rotations[0] - (AimAssist.getLastPlayerYaw() % 360.0f);

            // normalize differences for comparison
            if (targetYawDiff > 180.0f) targetYawDiff -= 360.0f;
            else if (targetYawDiff < -180.0f) targetYawDiff += 360.0f;

            if (manualYawDiff > 180.0f) manualYawDiff -= 360.0f;
            else if (manualYawDiff < -180.0f) manualYawDiff += 360.0f;

            float manualPitchDiff = player.rotationPitch - AimAssist.getLastPlayerPitch();
            float targetPitchDiff = MathHelper.wrapAngleTo180_float(rotations[1] - AimAssist.getLastPlayerPitch());

            // when player's manual aim is moving faster towards target than our calculated aim
            if (Math.abs(manualYawDiff) > Math.abs(targetYawDiff) && Math.abs(manualPitchDiff) > Math.abs(targetPitchDiff)) {
                return;
            }
        }

        float[] smoothedRotations = AimAssist.smoothRotationsAndLockOnTarget(rotations);

        float maxRotationSpeed = 90f; // maximum degree rotation per tick

        float finalYawDiff = MathHelper.wrapAngleTo180_float(smoothedRotations[0] - originalYaw);
        float finalPitchDiff = MathHelper.wrapAngleTo180_float(smoothedRotations[1] - originalPitch);

        if (Math.abs(finalYawDiff) > maxRotationSpeed) {
            finalYawDiff = maxRotationSpeed * Math.signum(finalYawDiff);
        }
        if (Math.abs(finalPitchDiff) > maxRotationSpeed) {
            finalPitchDiff = maxRotationSpeed * Math.signum(finalPitchDiff);
        }

        // apply rotations
        player.rotationYaw = originalYaw + finalYawDiff;
        player.rotationPitch = MathHelper.clamp_float(originalPitch + finalPitchDiff, -90.0f, 90.0f);

        // set arm angles to match the rotation -- this is purely on client side and is miscellaneous
        player.renderArmYaw = player.rotationYaw;
        player.renderArmPitch = player.rotationPitch;

        // log these rotations
        AimAssist.setLastPlayerYaw(player.rotationYaw);
        AimAssist.setLastPlayerPitch(player.rotationPitch);
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void onLivingUpdate(CallbackInfo ci) {
        EntityPlayerSP me = (EntityPlayerSP) (Object) this;

        // use adjusted/corrected aimassist yaw/pitch and then let physics engine handle the rest
        if (ActionConflictResolver.isRotatingAllowed() && enabled) {
            slant$handleAimAssist(me);

            // Directly set arm angles to match rotation without interpolation

            // Previous values will be handled by updateEntityActionState
        }
    }
}