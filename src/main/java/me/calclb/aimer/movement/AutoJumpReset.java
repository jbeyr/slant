package me.calclb.aimer.movement;

import me.calclb.aimer.Main;
import me.calclb.aimer.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

public class AutoJumpReset {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random rng = new Random();
    private static boolean enabled;
    private static float chance = 0.75f;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        Reporter.reportToggled("Auto Jump Reset", b);
    }

    public static float getChance() {
        return chance;
    }

    public static void setChance(float f) {
        chance = Math.max(0, Math.min(1, f));
        Reporter.reportSet("Auto Jump Reset", "Chance", f);

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

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Main.getAutoJumpResetKey().isPressed()) {
            setEnabled(!enabled);
        }
    }

}
