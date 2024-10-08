package me.jameesyy.slant;

import me.jameesyy.slant.combat.Aimlock;
import me.jameesyy.slant.combat.LeftAutoclicker;
import me.jameesyy.slant.combat.RightAutoclicker;
import me.jameesyy.slant.movement.Safewalk;
import me.jameesyy.slant.render.BedEsp;
import me.jameesyy.slant.render.InvisEsp;
import me.jameesyy.slant.render.Pointer;
import me.jameesyy.slant.render.SharkEsp;
import me.jameesyy.slant.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main {
    public static final String MODID = "slant";
    public static final String VERSION = "1.2.0";

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static KeyBinding lmbAutoclickKey;
    private static KeyBinding rmbAutoclickKey;
    private static KeyBinding aimlockKey;
    private static KeyBinding autoJumpResetKey;
    private static KeyBinding safewalkKey;
    private static KeyBinding openConfigKey;
    private static KeyBinding printNbtKey;

    public static Minecraft getMc() {
        return mc;
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

    public static KeyBinding getOpenConfigKey() {
        return openConfigKey;
    }

    public static KeyBinding getAutoJumpResetKey() {
        return autoJumpResetKey;
    }

    public static KeyBinding getSafewalkKey() {
        return safewalkKey;
    }

    public static KeyBinding getPrintNbtKey() {
        return printNbtKey;
    }


    @EventHandler
    public void init(FMLInitializationEvent event) {

        aimlockKey = new KeyBinding("Aimlock", Keyboard.KEY_NONE, "key.categories.gameplay");
        lmbAutoclickKey = new KeyBinding("LMB Autoclicker", Keyboard.KEY_NONE, "key.categories.gameplay");
        rmbAutoclickKey = new KeyBinding("RMB Autoclicker", Keyboard.KEY_NONE, "key.categories.gameplay");
        autoJumpResetKey = new KeyBinding("Auto Jump Reset", Keyboard.KEY_NONE, "key.categories.gameplay");
        safewalkKey = new KeyBinding("Safewalk", Keyboard.KEY_NONE, "key.categories.gameplay");
        openConfigKey = new KeyBinding("Open Slant Config", Keyboard.KEY_NONE, "key.categories.gameplay");
        printNbtKey = new KeyBinding("Print NBT of Held Item", Keyboard.KEY_NONE, "key.categories.gameplay");

        ClientRegistry.registerKeyBinding(lmbAutoclickKey);
        ClientRegistry.registerKeyBinding(rmbAutoclickKey);
        ClientRegistry.registerKeyBinding(aimlockKey);
        ClientRegistry.registerKeyBinding(autoJumpResetKey);
        ClientRegistry.registerKeyBinding(safewalkKey);
        ClientRegistry.registerKeyBinding(openConfigKey);
        ClientRegistry.registerKeyBinding(printNbtKey);

        ModConfig.getInstance().preload();
        ModConfig.getInstance().setupDependencies();
        MinecraftForge.EVENT_BUS.register(ModConfig.getInstance());
        MinecraftForge.EVENT_BUS.register(new Reporter());
        MinecraftForge.EVENT_BUS.register(new AntiBot());
        MinecraftForge.EVENT_BUS.register(new LeftAutoclicker());
        MinecraftForge.EVENT_BUS.register(new RightAutoclicker());
        MinecraftForge.EVENT_BUS.register(new SharkEsp());
        MinecraftForge.EVENT_BUS.register(new InvisEsp());
        MinecraftForge.EVENT_BUS.register(new BedEsp());
        MinecraftForge.EVENT_BUS.register(new Pointer());
        MinecraftForge.EVENT_BUS.register(new Aimlock());
        MinecraftForge.EVENT_BUS.register(new NbtPrinter());
        MinecraftForge.EVENT_BUS.register(new AutoGhead());
        MinecraftForge.EVENT_BUS.register(new Safewalk());
        MinecraftForge.EVENT_BUS.register(new AutoTool());
        ModConfig.getInstance().setModulesToConfig();
    }
}
