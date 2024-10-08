package me.jameesyy.slant.util;

import me.jameesyy.slant.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class AutoTool {

    private static boolean enabled;
    private static boolean onSneakOnly;
    private static final int blocksAway = 10;
    private static boolean nearBedOnly;


    public static boolean nearBedOnly() {
        return nearBedOnly;
    }

    public static boolean isNearBed() {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        BlockPos playerPos = mc.thePlayer.getPosition();

        for (int x = -blocksAway; x <= blocksAway; x++) {
            for (int y = -blocksAway; y <= blocksAway; y++) {
                for (int z = -blocksAway; z <= blocksAway; z++) {
                    BlockPos checkPos = playerPos.add(x, y, z);
                    Block block = world.getBlockState(checkPos).getBlock();
                    if (block instanceof BlockBed) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.autoToolEnabled = b;
        Reporter.reportToggled("Auto Tool", b);
    }

    public static int getBestToolSlot(net.minecraft.block.Block block) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        float bestSpeed = 1f;
        int bestSlot = player.inventory.currentItem;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() != null) {
                float speed = stack.getStrVsBlock(block);
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }

    public static boolean isOnSneakOnly() {
        return onSneakOnly;
    }

    public static void setOnSneakOnly(boolean b) {
        AutoTool.onSneakOnly = b;
        ModConfig.autoToolOnSneakOnly = b;
        Reporter.reportSet("Auto Tool", "Sneak Only", b);
    }

    public static void setNearBedOnly(boolean b) {
        AutoTool.nearBedOnly = b;
        ModConfig.autoToolNearBedOnly = b;
        Reporter.reportSet("Auto Tool", "Near Bed Only", b);
    }
}
