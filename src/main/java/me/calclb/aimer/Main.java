package me.calclb.aimer;

import me.calclb.aimer.combat.*;
import me.calclb.aimer.commands.AutoJumpResetCommand;
import me.calclb.aimer.commands.NoJumpDelayCommand;
import me.calclb.aimer.render.BedEsp;
import me.calclb.aimer.render.InvisEsp;
import me.calclb.aimer.render.SharkEsp;
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
    private static KeyBinding senseKey;
    private static KeyBinding lmbAutoclickKey;
    private static KeyBinding rmbAutoclickKey;
    private static KeyBinding aimlockKey;


    public static KeyBinding getSenseKey() {
        return senseKey;
    }

    public static KeyBinding getLeftAutoclickKey() {
        return lmbAutoclickKey;
    }

    public static KeyBinding getRightAutoclickKey() {
        return rmbAutoclickKey;
    }

    public static KeyBinding getAimlockKey() {
        return aimlockKey;
    }


    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new AntiBot());
        MinecraftForge.EVENT_BUS.register(new LeftAutoclicker());
        MinecraftForge.EVENT_BUS.register(new RightAutoclicker());
        MinecraftForge.EVENT_BUS.register(new SharkEsp());
        MinecraftForge.EVENT_BUS.register(new InvisEsp());
        MinecraftForge.EVENT_BUS.register(new BedEsp());
        MinecraftForge.EVENT_BUS.register(new Sensitivity());
        MinecraftForge.EVENT_BUS.register(new Pointer());
        MinecraftForge.EVENT_BUS.register(new Aimlock());

        ClientCommandHandler.instance.registerCommand(new NoJumpDelayCommand());
        ClientCommandHandler.instance.registerCommand(new AutoJumpResetCommand());

        aimlockKey = new KeyBinding("Aimlock", Keyboard.KEY_NONE, "key.categories.gameplay");
        senseKey = new KeyBinding("Sense", Keyboard.KEY_NONE, "key.categories.gameplay");
        lmbAutoclickKey = new KeyBinding("LMB Autoclicker", Keyboard.KEY_NONE, "key.categories.gameplay");
        rmbAutoclickKey = new KeyBinding("RMB Autoclicker", Keyboard.KEY_NONE, "key.categories.gameplay");

        ClientRegistry.registerKeyBinding(senseKey);
        ClientRegistry.registerKeyBinding(lmbAutoclickKey);
        ClientRegistry.registerKeyBinding(rmbAutoclickKey);
        ClientRegistry.registerKeyBinding(aimlockKey);
    }
}
