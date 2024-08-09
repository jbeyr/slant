package me.calclb.aimer;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main {
    private static KeyBinding aimAssistKey;
    private static KeyBinding senseKey;
    public static final String MODID = "aimer";
    public static final String VERSION = "1.0";

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new AimAssist());
        MinecraftForge.EVENT_BUS.register(new AntiBot());
        MinecraftForge.EVENT_BUS.register(new Sensitivity());
        MinecraftForge.EVENT_BUS.register(new Pointer());

        aimAssistKey = new KeyBinding("Aimassist", Keyboard.KEY_NONE, "key.categories.gameplay");
        senseKey = new KeyBinding("Sense", Keyboard.KEY_NONE, "key.categories.gameplay");

        ClientCommandHandler.instance.registerCommand(new AimassistCommand());
        ClientRegistry.registerKeyBinding(aimAssistKey);
        ClientRegistry.registerKeyBinding(senseKey);
    }

    public static KeyBinding getAimAssistKey() {
        return aimAssistKey;
    }

    public static KeyBinding getSenseKey() {
        return senseKey;
    }
}
