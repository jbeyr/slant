package me.jameesyy.slant.combat;

import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import java.util.Optional;

public class AutoWeapon {
    private static boolean enabled;
    private static boolean swapOnSwing;

    public static Optional<Integer> findWeaponInHotbar(EntityPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = player.inventory.getStackInSlot(i);
            if (itemStack == null) continue;

            Item item = itemStack.getItem();
            if (item instanceof ItemSword || item instanceof ItemAxe) return Optional.of(i);
        }
        return Optional.empty();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.autoWeaponEnabled = b;
        Reporter.queueReportMsg("Auto Weapon", b);
    }

    public static boolean shouldSwapOnSwing() {
        return swapOnSwing;
    }

    public static void swap() {
        EntityPlayer me = Minecraft.getMinecraft().thePlayer;
        Optional<Integer> weaponSlot = AutoWeapon.findWeaponInHotbar(me);
        if (!weaponSlot.isPresent()) return;

        int slot = weaponSlot.get();
        if (me.inventory.currentItem != slot) {
            me.inventory.currentItem = slot;
        }
    }

    public static void setSwapOnSwing(boolean b) {
        AutoWeapon.swapOnSwing = b;
        ModConfig.autoWeaponSwapOnSwing = b;
        Reporter.queueSetMsg("Auto Weapon", "Swap On Swing", b);
    }
}
