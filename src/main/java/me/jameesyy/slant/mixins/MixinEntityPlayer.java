package me.jameesyy.slant.mixins;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.combat.AutoWeapon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {
    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"))
    private void swapToWeaponIfTargeting(Entity targetEntity, CallbackInfo ci) {
        if (!AutoWeapon.isEnabled() || !ActionConflictResolver.isHotbarSelectedSlotChangeAllowed() || !(targetEntity instanceof EntityLivingBase)) return;
        AutoWeapon.swap();
    }
}
