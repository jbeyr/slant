package me.jameesyy.slant.util;

import me.jameesyy.slant.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class AutoTool {

    private static boolean enabled;
    private static boolean onSneakOnly;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.autoToolEnabled = b;
        Reporter.reportToggled("Auto Tool", b);
    }

    public static boolean isOnSneakOnly() {
        return onSneakOnly;
    }

    public static void setOnSneakOnly(boolean b) {
        AutoTool.onSneakOnly = b;
        ModConfig.autoToolOnSneakOnly = b;
        Reporter.reportSet("Auto Tool", "Sneak Only", b);
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
}
