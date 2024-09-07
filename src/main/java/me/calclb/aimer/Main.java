package me.calclb.aimer;

import me.calclb.aimer.aimassist.AimAssist;
import me.calclb.aimer.aimassist.AimassistCommand;
import me.calclb.aimer.aimassist.Sensitivity;
import me.calclb.aimer.autoclicker.LeftAutoclicker;
import me.calclb.aimer.autoclicker.RightAutoclicker;
import me.calclb.aimer.esp.BedEsp;
import me.calclb.aimer.esp.InvisEsp;
import me.calclb.aimer.esp.SharkEsp;
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
    public static final String MODID = "aimer";
    public static final String VERSION = "1.0";
    private static KeyBinding aimAssistKey;
    private static KeyBinding senseKey;
    private static KeyBinding lmbAutoclickKey;
    private static KeyBinding rmbAutoclickKey;


    public static KeyBinding getAimAssistKey() {
        return aimAssistKey;
    }

    public static KeyBinding getSenseKey() {
        return senseKey;
    }

    public static KeyBinding getLeftAutoclickKey() {
        return lmbAutoclickKey;
    }

    public static KeyBinding getRightAutoclickKey() {
        return rmbAutoclickKey;
    }


    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new AntiBot());
        MinecraftForge.EVENT_BUS.register(new LeftAutoclicker());
        MinecraftForge.EVENT_BUS.register(new RightAutoclicker());
        MinecraftForge.EVENT_BUS.register(new SharkEsp());
        MinecraftForge.EVENT_BUS.register(new InvisEsp());
        MinecraftForge.EVENT_BUS.register(new AimAssist());
        MinecraftForge.EVENT_BUS.register(new Sensitivity());
        MinecraftForge.EVENT_BUS.register(new BedEsp());

        aimAssistKey = new KeyBinding("Aimassist", Keyboard.KEY_NONE, "key.categories.gameplay");
        senseKey = new KeyBinding("Sense", Keyboard.KEY_NONE, "key.categories.gameplay");
        lmbAutoclickKey = new KeyBinding("LMB Autoclicker", Keyboard.KEY_NONE, "key.categories.gameplay");
        rmbAutoclickKey = new KeyBinding("RMB Autoclicker", Keyboard.KEY_NONE, "key.categories.gameplay");

        ClientCommandHandler.instance.registerCommand(new AimassistCommand());

        ClientRegistry.registerKeyBinding(aimAssistKey);
        ClientRegistry.registerKeyBinding(senseKey);
        ClientRegistry.registerKeyBinding(lmbAutoclickKey);
        ClientRegistry.registerKeyBinding(rmbAutoclickKey);
    }
}
