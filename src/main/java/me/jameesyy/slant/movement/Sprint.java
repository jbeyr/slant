package me.jameesyy.slant.movement;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Sprint {
    private static boolean enabled;

    public static void setEnabled(boolean b) {
        Sprint.enabled = b;
        ModConfig.sprintEnabled = b;
        Reporter.queueEnableMsg("Sprint", b);
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if(!enabled) return;
        if(!ActionConflictResolver.isSprintingAllowed()) return;
        KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindSprint.getKeyCode(), true);
    }
}