package me.jameesyy.slant;

import me.jameesyy.slant.combat.*;
import me.jameesyy.slant.movement.BridgeAssist;
import me.jameesyy.slant.movement.Sprint;
import me.jameesyy.slant.network.Backtrack;
import me.jameesyy.slant.network.PacketManager;
import me.jameesyy.slant.render.*;
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
    public static final String VERSION = "1.3.0";

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static KeyBinding lmbAutoclickKey;
    private static KeyBinding rmbAutoclickKey;
    private static KeyBinding betterAimKey;
    private static KeyBinding backtrackKey;
    private static KeyBinding aimAssistKey;

    private static KeyBinding autoJumpResetKey;
    private static KeyBinding bridgeAssistKey;
    private static KeyBinding openConfigKey;
    private static KeyBinding printNbtKey;
    private static KeyBinding triggerbotKey;

    public static Minecraft getMc() {
        return mc;
    }

    public static KeyBinding getLeftAutoclickKey() {
        return lmbAutoclickKey;
    }

    public static KeyBinding getRightAutoclickKey() {
        return rmbAutoclickKey;
    }

    public static KeyBinding getBetterAimKey() {
        return betterAimKey;
    }

    public static KeyBinding getAimAssistKey() {
        return aimAssistKey;
    }

    public static KeyBinding getOpenConfigKey() {
        return openConfigKey;
    }

    public static KeyBinding getAutoJumpResetKey() {
        return autoJumpResetKey;
    }

    public static KeyBinding getBridgeAssistKey() {
        return bridgeAssistKey;
    }

    public static KeyBinding getPrintNbtKey() {
        return printNbtKey;
    }

    public static KeyBinding getTriggerBotKey() {
        return triggerbotKey;
    }

    public static KeyBinding getBacktrackKey() {
        return backtrackKey;
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

        betterAimKey = new KeyBinding("Better Aim", Keyboard.KEY_NONE, "key.categories.gameplay");
        backtrackKey = new KeyBinding("Backtrack", Keyboard.KEY_NONE, "key.categories.gameplay");
        aimAssistKey = new KeyBinding("Aim Assist", Keyboard.KEY_NONE, "key.categories.gameplay");
        lmbAutoclickKey = new KeyBinding("LMB Autoclicker", Keyboard.KEY_NONE, "key.categories.gameplay");
        rmbAutoclickKey = new KeyBinding("RMB Autoclicker", Keyboard.KEY_NONE, "key.categories.gameplay");
        triggerbotKey = new KeyBinding("Trigger Bot", Keyboard.KEY_NONE, "key.categories.gameplay");
        autoJumpResetKey = new KeyBinding("Auto Jump Reset", Keyboard.KEY_NONE, "key.categories.gameplay");
        bridgeAssistKey = new KeyBinding("Bridge Assist", Keyboard.KEY_NONE, "key.categories.gameplay");
        openConfigKey = new KeyBinding("Open Slant Config", Keyboard.KEY_NONE, "key.categories.gameplay");
        printNbtKey = new KeyBinding("Print NBT of Held Item", Keyboard.KEY_NONE, "key.categories.gameplay");

        ClientRegistry.registerKeyBinding(betterAimKey);
        ClientRegistry.registerKeyBinding(backtrackKey);
        ClientRegistry.registerKeyBinding(aimAssistKey);
        ClientRegistry.registerKeyBinding(lmbAutoclickKey);
        ClientRegistry.registerKeyBinding(rmbAutoclickKey);
        ClientRegistry.registerKeyBinding(triggerbotKey);
        ClientRegistry.registerKeyBinding(autoJumpResetKey);
        ClientRegistry.registerKeyBinding(bridgeAssistKey);
        ClientRegistry.registerKeyBinding(openConfigKey);
        ClientRegistry.registerKeyBinding(printNbtKey);

        ModConfig.getInstance().preload();
        ModConfig.getInstance().setupConfigCallbacks();
        MinecraftForge.EVENT_BUS.register(ModConfig.getInstance());

        PacketManager packetManager = new PacketManager();
        Targeter.addListener(packetManager);
        MinecraftForge.EVENT_BUS.register(packetManager);

        MinecraftForge.EVENT_BUS.register(new Targeter());
        MinecraftForge.EVENT_BUS.register(new Reporter());
        MinecraftForge.EVENT_BUS.register(new AntiBot());
        MinecraftForge.EVENT_BUS.register(new LeftAutoclicker());
        MinecraftForge.EVENT_BUS.register(new RightAutoclicker());
        MinecraftForge.EVENT_BUS.register(new SharkEsp());
        MinecraftForge.EVENT_BUS.register(new InvisEsp());
        MinecraftForge.EVENT_BUS.register(new BedEsp());
        MinecraftForge.EVENT_BUS.register(new BetterAim());
        MinecraftForge.EVENT_BUS.register(new AimAssist());
        MinecraftForge.EVENT_BUS.register(new NbtPrinter());
        MinecraftForge.EVENT_BUS.register(new AutoGhead());
        MinecraftForge.EVENT_BUS.register(new BridgeAssist());
        MinecraftForge.EVENT_BUS.register(new RodRecast());
        MinecraftForge.EVENT_BUS.register(new FireballPointer());
        MinecraftForge.EVENT_BUS.register(new QuickMathsSolver());
        MinecraftForge.EVENT_BUS.register(new ChestEsp());
        MinecraftForge.EVENT_BUS.register(new Tracers());
        MinecraftForge.EVENT_BUS.register(new Sprint());
        MinecraftForge.EVENT_BUS.register(new BlockHit());
        MinecraftForge.EVENT_BUS.register(new Backtrack());
        ModConfig.getInstance().setModulesToConfig();
    }
}
