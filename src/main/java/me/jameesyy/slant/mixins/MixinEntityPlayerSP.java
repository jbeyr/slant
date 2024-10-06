package me.jameesyy.slant.mixins;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.combat.Aimlock;
import me.jameesyy.slant.movement.Safewalk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.*;
import java.util.Optional;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void onUpdateWalkingPlayer(CallbackInfo ci) {
        EntityPlayerSP player = (EntityPlayerSP) (Object) this;
        Safewalk.setLastMovementInput(player.movementInput);
        slant$handleAimlock(player);
    }

    @Unique
    private void slant$handleAimlock(EntityPlayerSP player) {

        if(!Aimlock.isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (Aimlock.isInAValidStateToAim() && Aimlock.getTargetEntity().isPresent() && mc.gameSettings.thirdPersonView != 2) {
            EntityLivingBase target = Aimlock.getTargetEntity().get();
            double[] hitboxBounds = Aimlock.calculateHitboxBounds(player, target);
            float minYaw = (float) hitboxBounds[0];
            float maxYaw = (float) hitboxBounds[1];
            float minPitch = (float) hitboxBounds[2];
            float maxPitch = (float) hitboxBounds[3];

            float currentYaw = player.rotationYaw;
            float centerYaw = (minYaw + maxYaw) / 2;

            float yawDiff = (centerYaw - currentYaw) % 360;
            if (yawDiff > 180) yawDiff -= 360;
            if (yawDiff < -180) yawDiff += 360;

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