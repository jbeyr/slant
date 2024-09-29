package me.calclb.aimer.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import java.util.Random;

public class AutoJumpReset {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random rng = new Random();
    private static boolean enabled;
    private static double chance = 0.75d;

    public static double getChance() {
        return chance;
    }

    public static void setChance(double d) {
        chance = Math.max(0, Math.min(1, d));
    }

    public static boolean shouldActivate() {
        return rng.nextDouble() < chance;
    }

    public static Minecraft getMc() {
        return mc;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static void legitJump() {
        int key = mc.gameSettings.keyBindJump.getKeyCode();
        KeyBinding.setKeyBindState(key, true);
        KeyBinding.onTick(key);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        AutoJumpReset.enabled = enabled;
    }


}
