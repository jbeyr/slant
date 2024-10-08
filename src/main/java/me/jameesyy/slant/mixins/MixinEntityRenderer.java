package me.jameesyy.slant.mixins;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.combat.Aimlock;
import me.jameesyy.slant.util.EnhancedAimingModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow
    private Minecraft mc;

    @Inject(
            method = "updateCameraAndRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;setAngles(FF)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onBeforeSetAngles(float partialTicks, long nanoTime, CallbackInfo ci,
                                   boolean flag, float f, float f1, float f2, float f3, int l1) {
        if (Aimlock.isEnabled() && ActionConflictResolver.isRotatingAllowed() && mc.inGameHasFocus && Display.isActive()) {
            EntityPlayerSP player = mc.thePlayer;
            Optional<EntityLivingBase> target = Aimlock.getTargetEntity();

            if (target.isPresent()) {
                float[] mappedDeltas = EnhancedAimingModule.mapRotation(f2, f3, player, target.get());

                // Modify f2 and f3 which will be used in the upcoming setAngles call
                f2 = mappedDeltas[0];
                if(Aimlock.doesVerticalRotations()) f3 = mappedDeltas[1];
            }
        }
    }
}