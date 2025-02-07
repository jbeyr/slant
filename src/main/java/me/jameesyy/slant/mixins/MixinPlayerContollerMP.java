package me.jameesyy.slant.mixins;


import me.jameesyy.slant.combat.AimAssist;
import me.jameesyy.slant.util.AutoTool;
import me.jameesyy.slant.util.NoMiningDelay;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerContollerMP {

    @Shadow
    private boolean isHittingBlock;

    @Shadow
    private int blockHitDelay;

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"))
    private void onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir) {
        if(NoMiningDelay.isEnabled()) blockHitDelay = 0;

        Minecraft mc = Minecraft.getMinecraft();
        if (AutoTool.isEnabled() && (!AutoTool.isOnSneakOnly() || mc.thePlayer.isSneaking()) && (!AutoTool.nearBedOnly() || AutoTool.isNearBed())) {
            Block block = mc.theWorld.getBlockState(posBlock).getBlock();
            int bestSlot = AutoTool.getBestToolSlot(block);
            if (bestSlot != mc.thePlayer.inventory.currentItem) {
                mc.thePlayer.inventory.currentItem = bestSlot;
            }
        }
    }

    // TODO consider disabling aim assist when breaking blocks
//    @Inject(method = "onPlayerDamageBlock", at = @At("TAIL"))
//    private void afterPlayerDamageBlock(BlockPos p_onPlayerDamageBlock_1_, EnumFacing p_onPlayerDamageBlock_2_, CallbackInfoReturnable<Boolean> cir) {
//        AimAssist.setIsHittingBlock(isHittingBlock);
//    }
//
//    @Inject(method = "resetBlockRemoving", at = @At("TAIL"))
//    private void afterResetBlockRemoving(CallbackInfo ci) {
//        AimAssist.setIsHittingBlock(isHittingBlock);
//    }
//
//    @Inject(method = "clickBlock", at = @At("TAIL"))
//    private void afterClickBlock(BlockPos p_clickBlock_1_, EnumFacing p_clickBlock_2_, CallbackInfoReturnable<Boolean> cir) {
//        AimAssist.setIsHittingBlock(isHittingBlock);
//    }
}
