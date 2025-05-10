package me.jameesyy.slant.mixins;

import me.jameesyy.slant.combat.NoHitDelay;
import me.jameesyy.slant.util.NoMiningDelay;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Minecraft.class})
public abstract class MixinMinecraft {
    @Shadow
    private int leftClickCounter;

    @Inject(method = "clickMouse", at = @At("HEAD"))
    private void onClickMouse(CallbackInfo ci) {
        if (NoHitDelay.isEnabled()) leftClickCounter = 0;
    }

    /* 2. Safety-net for the ticks afterward while the mouse is held */
    @Inject(method = "sendClickBlockToController", at = @At("HEAD"))
    private void zeroCounterEveryTick(boolean holding, CallbackInfo ci) {
        if (NoHitDelay.isEnabled()) leftClickCounter = 0;
    }
}
