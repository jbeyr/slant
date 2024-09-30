package me.jameesyy.slant;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
import me.jameesyy.slant.combat.*;
import me.jameesyy.slant.movement.AutoJumpReset;
import me.jameesyy.slant.movement.NoJumpDelay;
import me.jameesyy.slant.render.BedEsp;
import me.jameesyy.slant.render.InvisEsp;
import me.jameesyy.slant.render.Pointer;
import me.jameesyy.slant.render.SharkEsp;
import me.jameesyy.slant.util.AntiBot;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.io.File;

public class ModConfig extends Vigilant {

    private static ModConfig INSTANCE;

    // region MODULE STATES

    @Property(type = PropertyType.SWITCH, name = "Opp Tracker", category = "Utility", description = "Tells combat modules to prioritize targets that are aggressive to you.")
    public static boolean oppTrackerEnabled = false;

    @Property(type = PropertyType.SWITCH, name = "AntiBot", category = "Utility", description = "Tells combat modules to ignore bots.")
    public static boolean antiBotEnabled = true;

    // Combat
    @Property(type = PropertyType.SWITCH, name = "Auto Weapon", category = "Combat", description = "Sets your selected item to a weapon when attacking.")
    public static boolean autoWeaponEnabled = false;

        @Property(type = PropertyType.SWITCH, name = "Auto Weapon: Swap On Swing", category = "Combat", description = "If true, swaps when a target entity triggers LMB Autoclicker.")
        public static boolean autoWeaponSwapOnSwing = false;

    @Property(type = PropertyType.SWITCH, name = "Aimlock", category = "Combat", description = "Helps track the target hitbox when your crosshair enters it.")
    public static boolean aimlockEnabled = false;

        @Property(type = PropertyType.SWITCH, name = "Aimlock: Vertical Rotations", category = "Combat")
        public static boolean aimlockVerticalRotations = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Aimlock: Rotation Speed", category = "Combat", minF = .05f, maxF = .5f)
        public static float aimlockRotationSpeed = 0.15f;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Aimlock: Max Yaw Tick Rotation", category = "Combat", minF = 1f, maxF = 50f)
        public static float aimlockMaxYawTickRotation = 40f;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Aimlock: Activation Radius", category = "Combat", minF = 1f, maxF = 8f)
        public static float aimlockActivationRadius = 4.25f;

    @Property(type = PropertyType.SWITCH, name = "Right Autoclicker", category = "Combat", description = "Places held blocks when you're pressing the 'use item' key.")
    public static boolean rightAutoclickerEnabled = false;

        @Property(type = PropertyType.SLIDER, name = "RMB: Min CPS", category = "Combat", min = 8, max = 30)
        public static int rightAutoClickerMinCps = 18;

        @Property(type = PropertyType.SLIDER, name = "RMB: Max CPS", category = "Combat", min = 8, max = 30)
        public static int rightAutoClickerMaxCps = 22;

    @Property(type = PropertyType.SWITCH, name = "Left Autoclicker", category = "Combat", description = "Swings when a player is in front of you.")
    public static boolean leftAutoclickerEnabled = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "LMB: Activation Range", category = "Combat", minF = 1f, maxF = 8f)
        public static float leftAutoclickerActivationRadius = 4.5f;

        @Property(type = PropertyType.SLIDER, name = "LMB: Min CPS", category = "Combat", min = 8, max = 25)
        public static int leftAutoClickerMinCps = 12;

        @Property(type = PropertyType.SLIDER, name = "LMB: Max CPS", category = "Combat", min = 8, max = 25)
        public static int leftAutoClickerMaxCps = 14;

    @Property(type = PropertyType.SWITCH, name = "Auto Jump Reset", category = "Movement", description = "Jumps for you when you're attacked to reduce knockback.")
    public static boolean autoJumpResetEnabled = false;

        @Property(type = PropertyType.PERCENT_SLIDER, name = "Auto Jump Reset: Chance", category = "Movement")
        public static float autoJumpResetChance = 0.75f;

    @Property(type = PropertyType.SWITCH, name = "No Jump Delay", category = "Movement", description = "Removes the vanilla jumping cooldown.")
    public static boolean noJumpDelayEnabled = false;

    @Property(type = PropertyType.SWITCH, name = "No Hit Delay", category = "Combat", description = "Removes the swing cooldown when you miss attacks repeatedly.")
    public static boolean noHitDelayEnabled = false;

    @Property(type = PropertyType.SWITCH, name = "Pointer", category = "Render", description = "Points at the closest part of a target's hitbox.")
    public static boolean pointerEnabled = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Pointer Activation Radius", category = "Render", maxF = 12f)
        public static float pointerActivationRadius = 4.5f;

    // Render
    @Property(type = PropertyType.SWITCH, name = "Bed ESP", category = "Render", subcategory = "ESP")
    public static boolean bedEspEnabled = false;

        @Property(type = PropertyType.SLIDER, name = "Bed ESP Radius", category = "Render", min = 5, max = 100)
        public static int bedEspActivationRadiusBlocks = 20;

    @Property(type = PropertyType.SWITCH, name = "Invis ESP", category = "Render", subcategory = "ESP")
    public static boolean invisEspEnabled = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Invis ESP Radius", category = "Render", maxF = 100f)
        public static float invisEspActivationRadius = 50f;

    @Property(type = PropertyType.SWITCH, name = "Shark ESP", category = "Render", subcategory = "ESP", description = "ESP, but for low-health players")
    public static boolean sharkEspEnabled = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Shark ESP Radius", category = "Render", maxF = 100f)
        public static float sharkEspActivationRadius = 20f;

        @Property(type = PropertyType.PERCENT_SLIDER, name = "Shark ESP: Low Health Threshold", category = "Render")
        public static float sharkEspLowHealthThreshold = 0.5f;

    // endregion


    private ModConfig() {
        super(new File("./config/" + Main.MODID + ".toml"), "Slant Config");
        initialize();
    }

    public void setModulesToConfig() {
//        OppTracker.setEnabled(oppTrackerEnabled); // Assuming OppTracker has a static setEnabled() method
        AntiBot.setEnabled(antiBotEnabled);

        AutoWeapon.setEnabled(autoWeaponEnabled);
        AutoWeapon.setSwapOnSwing(autoWeaponSwapOnSwing);

        Aimlock.setEnabled(aimlockEnabled);
        Aimlock.setVerticalRotations(aimlockVerticalRotations);
        Aimlock.setRotationSpeed(aimlockRotationSpeed);
        Aimlock.setMaxYawTickRotation(aimlockMaxYawTickRotation);
        Aimlock.setActivationRadius(aimlockActivationRadius);

        RightAutoclicker.setEnabled(rightAutoclickerEnabled);
        RightAutoclicker.setMinCPS(rightAutoClickerMinCps);
        RightAutoclicker.setMaxCPS(rightAutoClickerMaxCps);

        LeftAutoclicker.setEnabled(leftAutoclickerEnabled);
        LeftAutoclicker.setActivationRadius(leftAutoclickerActivationRadius);
        LeftAutoclicker.setMinCPS(leftAutoClickerMinCps);
        LeftAutoclicker.setMaxCPS(leftAutoClickerMaxCps);

        NoHitDelay.setEnabled(noHitDelayEnabled);

        AutoJumpReset.setEnabled(autoJumpResetEnabled);
        AutoJumpReset.setChance(autoJumpResetChance);

        NoJumpDelay.setEnabled(noJumpDelayEnabled);

        // Render
        Pointer.setEnabled(pointerEnabled);
        Pointer.setActivationRadius(pointerActivationRadius);

        BedEsp.setEnabled(bedEspEnabled);
        BedEsp.setActivationRadiusBlocks(bedEspActivationRadiusBlocks);

        InvisEsp.setEnabled(invisEspEnabled);
        InvisEsp.setActivationRadius(invisEspActivationRadius);

        SharkEsp.setEnabled(sharkEspEnabled);
        SharkEsp.setActivationRadius(sharkEspActivationRadius);
        SharkEsp.setLowHealthThreshold(sharkEspLowHealthThreshold);
    }

    public void setupDependencies() {
        // Combat dependencies
        registerListener("oppTrackerEnabled", (Boolean b) -> {
            System.out.println("oppTrackerEnabled was set to " + b);
        });

        // when toggles change, enable the module
        registerListener("antiBotEnabled", AntiBot::setEnabled);
        registerListener("autoWeaponEnabled", AutoWeapon::setEnabled);
        registerListener("autoWeaponSwapOnSwing", AutoWeapon::setSwapOnSwing);
        registerListener("aimlockEnabled", Aimlock::setEnabled);
        registerListener("rightAutoclickerEnabled", RightAutoclicker::setEnabled);
        registerListener("leftAutoclickerEnabled", LeftAutoclicker::setEnabled);
        registerListener("autoJumpResetEnabled", AutoJumpReset::setEnabled);
        registerListener("noJumpDelayEnabled", NoJumpDelay::setEnabled);
        registerListener("noHitDelayEnabled", NoHitDelay::setEnabled);
        registerListener("pointerEnabled", Pointer::setEnabled);
        registerListener("bedEspEnabled", BedEsp::setEnabled);
        registerListener("invisEspEnabled", InvisEsp::setEnabled);
        registerListener("sharkEspEnabled", SharkEsp::setEnabled);

        // autoclickers
        registerListener("leftAutoClickerMinCps", LeftAutoclicker::setMinCPS);
        registerListener("leftAutoClickerMaxCps", LeftAutoclicker::setMaxCPS);
        registerListener("leftAutoclickerActivationRadius", LeftAutoclicker::setActivationRadius);
        registerListener("rightAutoClickerMinCps", RightAutoclicker::setMinCPS);
        registerListener("rightAutoClickerMaxCps", RightAutoclicker::setMaxCPS);

        // aimlock
        registerListener("aimlockVerticalRotations", Aimlock::setVerticalRotations);
        registerListener("aimlockRotationSpeed", Aimlock::setRotationSpeed);
        registerListener("aimlockMaxYawTickRotation", Aimlock::setMaxYawTickRotation);
        registerListener("aimlockActivationRadius", Aimlock::setActivationRadius);

        // others
        registerListener("autoJumpResetChance", AutoJumpReset::setChance);
        registerListener("pointerActivationRadius", Pointer::setActivationRadius);
        registerListener("sharkEspActivationRadius", SharkEsp::setActivationRadius);
        registerListener("invisEspActivationRadius", InvisEsp::setActivationRadius);
        registerListener("bedEspActivationRadiusBlocks", BedEsp::setActivationRadiusBlocks);
        registerListener("sharkEspLowHealthThreshold", SharkEsp::setLowHealthThreshold);
    }

    private static void openConfigGui() {
        Minecraft.getMinecraft().displayGuiScreen(getInstance().gui());
    }

    public static ModConfig getInstance() {
        if (INSTANCE == null) INSTANCE = new ModConfig();
        return INSTANCE;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Main.getOpenConfigKey().isPressed()) {
            openConfigGui();
        }
    }
}