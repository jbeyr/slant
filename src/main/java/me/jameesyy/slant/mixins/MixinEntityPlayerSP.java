package me.jameesyy.slant.mixins;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.combat.Aimlock;
import me.jameesyy.slant.movement.Safewalk;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
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
}