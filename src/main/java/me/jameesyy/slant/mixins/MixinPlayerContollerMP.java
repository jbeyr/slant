package me.jameesyy.slant.mixins;


import me.jameesyy.slant.util.AutoTool;
import me.jameesyy.slant.util.NoMiningDelay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerContollerMP {

    @Shadow
    private int blockHitDelay;

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"))
    private void onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir) {
        if(NoMiningDelay.isEnabled()) blockHitDelay = 0;

        Minecraft mc = Minecraft.getMinecraft();
        if (AutoTool.isEnabled() && (!AutoTool.isOnSneakOnly() || mc.thePlayer.isSneaking()) && (!AutoTool.nearBedOnly() || AutoTool.isNearBed())) {
            net.minecraft.block.Block block = mc.theWorld.getBlockState(posBlock).getBlock();
            int bestSlot = AutoTool.getBestToolSlot(block);
            if (bestSlot != mc.thePlayer.inventory.currentItem) {
                mc.thePlayer.inventory.currentItem = bestSlot;
            }
        }
    }
}
