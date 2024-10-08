package me.jameesyy.slant;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
import me.jameesyy.slant.combat.*;
import me.jameesyy.slant.movement.AutoJumpReset;
import me.jameesyy.slant.movement.NoJumpDelay;
import me.jameesyy.slant.movement.Safewalk;
import me.jameesyy.slant.render.BedEsp;
import me.jameesyy.slant.render.InvisEsp;
import me.jameesyy.slant.render.Pointer;
import me.jameesyy.slant.render.SharkEsp;
import me.jameesyy.slant.util.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.awt.*;
import java.io.File;

public class ModConfig extends Vigilant {

    private static ModConfig INSTANCE;

    // region MODULE STATES

    @Property(type = PropertyType.SWITCH, name = "Safewalk", category = "Modules", description = "Sneak near the edge of blocks.")
    public static boolean safewalkEnabled = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Edge Distance", category = "Safewalk", description = "The distance from the edge at which sneaking occurs.", maxF = 0.5f, decimalPlaces = 2)
        public static float safewalkEdgeDistance = 0.15f;

    @Property(type = PropertyType.SWITCH, name = "Anti Bot", category = "Modules", description = "Tells combat modules to ignore bots.")
    public static boolean antiBotEnabled = true;

    @Property(type = PropertyType.SWITCH, name = "Auto Ghead", category = "Modules", description = "Consumes golden heads when you're low on health.")
    public static boolean autoGheadEnabled = false;

        @Property(type = PropertyType.PERCENT_SLIDER, name = "Health Threshold", category = "Auto Ghead", description = "Won't consume a golden head above this threshold.", maxF = 1f)
        public static float autoGheadHealthThreshold = 0.75f;

    @Property(type = PropertyType.SWITCH, name = "Auto Tool", category = "Modules", description = "Swaps to the ideal tool to break a block.")
    public static boolean autoToolEnabled = false;

        @Property(type = PropertyType.SWITCH, name = "On Sneak Only", category = "Auto Tool", description = "Only swap if crouching and breaking a block.")
        public static boolean autoToolOnSneakOnly = false;

    // Combat
    @Property(type = PropertyType.SWITCH, name = "Auto Weapon", category = "Modules", description = "Sets your selected item to a weapon when attacking.")
    public static boolean autoWeaponEnabled = false;

        @Property(type = PropertyType.SWITCH, name = "Swap On Swing", category = "Auto Weapon", description = "If true, swaps when a target entity triggers LMB Autoclicker.")
        public static boolean autoWeaponSwapOnSwing = false;

    // region aimlock
    @Property(type = PropertyType.SWITCH, name = "Aimlock", category = "Modules", description = "Helps track the target hitbox when your crosshair enters it.")
    public static boolean aimlockEnabled = false;

        @Property(type = PropertyType.SELECTOR, name = "Target Priority", category = "Aimlock", description = "Focus aimlock on the initially hitscanned player, the closest player within the FOV range, or the lowest health player within the FOV range.", options = {"Initial Hitscan", "Closest FOV", "Lowest Health"})
        public static int aimlockTargetPriority = Aimlock.TargetPriority.INITIAL_HITSCAN.ordinal();

        @Property(type = PropertyType.SWITCH, name = "Vertical Rotations", category = "Aimlock")
        public static boolean aimlockVerticalRotations = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Activation FOV", category = "Aimlock", minF = 0, maxF = 360, decimalPlaces = 0)
        public static float aimlockFov = 30f;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Activation Radius", category = "Aimlock", minF = 1f, maxF = 8f, decimalPlaces = 2)
        public static float aimlockActivationRadius = 4.15f;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "Minimum Target Angular Size (Degrees)", category="Aimlock", minF = 10f, maxF = 45f, decimalPlaces = 0)
    public static float minTargetAngularSize = 10f;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "Blend Strength", category="Aimlock", minF=0f, maxF=1f, decimalPlaces = 2)
    public static float blendFactor = 0.5f;

    @Property(type = PropertyType.COLOR, name = "Target Hitbox Color", category="Aimlock")
    public static Color aimlockTargetHitboxColor = new Color(255, 255, 255, 255);

    // endregion

    @Property(type = PropertyType.SWITCH, name = "Right Autoclicker", category = "Modules", description = "Places held blocks when you're pressing the 'use item' key.")
    public static boolean rightAutoclickerEnabled = false;

        @Property(type = PropertyType.SLIDER, name = "RMB: Min CPS", category = "Autoclicker", min = 8, max = 30)
        public static int rightAutoClickerMinCps = 18;

        @Property(type = PropertyType.SLIDER, name = "RMB: Max CPS", category = "Autoclicker", min = 8, max = 30)
        public static int rightAutoClickerMaxCps = 22;

    @Property(type = PropertyType.SWITCH, name = "Left Autoclicker", category = "Modules", description = "Swings when a player is in front of you.")
    public static boolean leftAutoclickerEnabled = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "LMB: Activation Range", category = "Autoclicker", minF = 1f, maxF = 8f, decimalPlaces = 2)
        public static float leftAutoclickerActivationRadius = 4.5f;

        @Property(type = PropertyType.SLIDER, name = "LMB: Min CPS", category = "Autoclicker", min = 8, max = 25)
        public static int leftAutoClickerMinCps = 12;

        @Property(type = PropertyType.SLIDER, name = "LMB: Max CPS", category = "Autoclicker", min = 8, max = 25)
        public static int leftAutoClickerMaxCps = 14;

    @Property(type = PropertyType.SWITCH, name = "Auto Jump Reset", category = "Modules", description = "Jumps for you when you're attacked to reduce knockback.")
    public static boolean autoJumpResetEnabled = false;

        @Property(type = PropertyType.PERCENT_SLIDER, name = "Chance", category = "Auto Jump Reset")
        public static float autoJumpResetChance = 0.75f;

    @Property(type = PropertyType.SWITCH, name = "No Jump Delay", category = "Modules", description = "Removes the vanilla jumping cooldown.")
    public static boolean noJumpDelayEnabled = false;

    @Property(type = PropertyType.SWITCH, name = "No Hit Delay", category = "Modules", description = "Removes the swing cooldown when you miss attacks repeatedly.")
    public static boolean noHitDelayEnabled = false;

    @Property(type = PropertyType.SWITCH, name = "No Mining Delay", category = "Modules", description = "Removes the swing cooldown when you try breaking a block.")
    public static boolean noMiningDelayEnabled = false;

    @Property(type = PropertyType.SWITCH, name = "Pointer", category = "Modules", description = "Points at the closest part of a target's hitbox.")
    public static boolean pointerEnabled = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Activation Radius", category = "Pointer", maxF = 12f, decimalPlaces = 2)
        public static float pointerActivationRadius = 4.5f;

    // Render
    @Property(type = PropertyType.SWITCH, name = "Bed ESP", category = "Modules", subcategory = "ESP")
    public static boolean bedEspEnabled = false;

    @Property(type = PropertyType.SLIDER, name = "Bed ESP Radius", category = "ESP", min = 5, max = 40)
    public static int bedEspActivationRadiusBlocks = 20;

    @Property(type = PropertyType.SWITCH, name = "Invis ESP", category = "Modules", subcategory = "ESP")
    public static boolean invisEspEnabled = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Invis ESP Radius", category = "ESP", maxF = 100f)
        public static float invisEspActivationRadius = 50f;

    @Property(type = PropertyType.SWITCH, name = "Shark ESP", category = "Modules", subcategory = "ESP", description = "ESP, but for low-health players")
    public static boolean sharkEspEnabled = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Shark ESP Radius", category = "ESP", maxF = 100f)
        public static float sharkEspActivationRadius = 20f;

        @Property(type = PropertyType.PERCENT_SLIDER, name = "Shark ESP: Low Health Threshold", category = "ESP")
        public static float sharkEspLowHealthThreshold = 0.5f;

    // endregion


    private ModConfig() {
        super(new File("./config/" + Main.MODID + ".toml"), "Slant Config");
        initialize();
    }

    public void setModulesToConfig() {
        AntiBot.setEnabled(antiBotEnabled);

        AutoGhead.setEnabled(autoGheadEnabled);
        AutoGhead.setHealthThreshold(autoGheadHealthThreshold);

        AutoWeapon.setEnabled(autoWeaponEnabled);
        AutoWeapon.setSwapOnSwing(autoWeaponSwapOnSwing);

        AutoTool.setEnabled(autoToolEnabled);
        AutoTool.setOnSneakOnly(autoToolOnSneakOnly);

        Aimlock.setEnabled(aimlockEnabled);
        Aimlock.setFov(aimlockFov);
        Aimlock.setTargetPriority(Aimlock.TargetPriority.values()[aimlockTargetPriority]);
        Aimlock.setTargetHitboxColor(aimlockTargetHitboxColor);
        Aimlock.setVerticalRotations(aimlockVerticalRotations);
        Aimlock.setActivationRadius(aimlockActivationRadius);
        EnhancedAimingModule.setMinTargetAngularSize(minTargetAngularSize);
        EnhancedAimingModule.setBlendFactor(blendFactor);

        RightAutoclicker.setEnabled(rightAutoclickerEnabled);
        RightAutoclicker.setMinCPS(rightAutoClickerMinCps);
        RightAutoclicker.setMaxCPS(rightAutoClickerMaxCps);

        LeftAutoclicker.setEnabled(leftAutoclickerEnabled);
        LeftAutoclicker.setActivationRadius(leftAutoclickerActivationRadius);
        LeftAutoclicker.setMinCPS(leftAutoClickerMinCps);
        LeftAutoclicker.setMaxCPS(leftAutoClickerMaxCps);

        NoHitDelay.setEnabled(noHitDelayEnabled);
        NoMiningDelay.setEnabled(noMiningDelayEnabled);

        AutoJumpReset.setEnabled(autoJumpResetEnabled);
        AutoJumpReset.setChance(autoJumpResetChance);
        NoJumpDelay.setEnabled(noJumpDelayEnabled);
        Safewalk.setEnabled(safewalkEnabled);
        Safewalk.setEdgeDistance(safewalkEdgeDistance);

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

        registerListener("antiBotEnabled", AntiBot::setEnabled);
        registerListener("autoGheadEnabled", AutoGhead::setEnabled);
        registerListener("autoWeaponEnabled", AutoWeapon::setEnabled);
        registerListener("autoWeaponSwapOnSwing", AutoWeapon::setSwapOnSwing);
        registerListener("autoToolEnabled", AutoTool::setEnabled);
        registerListener("autoToolOnSneakOnly", AutoTool::setOnSneakOnly);
        registerListener("aimlockEnabled", Aimlock::setEnabled);
        registerListener("rightAutoclickerEnabled", RightAutoclicker::setEnabled);
        registerListener("leftAutoclickerEnabled", LeftAutoclicker::setEnabled);
        registerListener("autoJumpResetEnabled", AutoJumpReset::setEnabled);
        registerListener("noJumpDelayEnabled", NoJumpDelay::setEnabled);
        registerListener("noHitDelayEnabled", NoHitDelay::setEnabled);
        registerListener("noMiningDelayEnabled", NoMiningDelay::setEnabled);
        registerListener("pointerEnabled", Pointer::setEnabled);
        registerListener("bedEspEnabled", BedEsp::setEnabled);
        registerListener("invisEspEnabled", InvisEsp::setEnabled);
        registerListener("sharkEspEnabled", SharkEsp::setEnabled);
        registerListener("safewalkEnabled", Safewalk::setEnabled);

        // autoclickers
        registerListener("leftAutoClickerMinCps", LeftAutoclicker::setMinCPS);
        registerListener("leftAutoClickerMaxCps", LeftAutoclicker::setMaxCPS);
        registerListener("leftAutoclickerActivationRadius", LeftAutoclicker::setActivationRadius);
        registerListener("rightAutoClickerMinCps", RightAutoclicker::setMinCPS);
        registerListener("rightAutoClickerMaxCps", RightAutoclicker::setMaxCPS);


        // aimlock
        registerListener("aimlockTargetHitboxColor", Aimlock::setTargetHitboxColor);
        registerListener("aimlockTargetPriority", (Integer i) -> Aimlock.setTargetPriority(Aimlock.TargetPriority.values()[i]));
        registerListener("aimlockVerticalRotations", Aimlock::setVerticalRotations);
        registerListener("aimlockFov", Aimlock::setFov);
        registerListener("aimlockActivationRadius", Aimlock::setActivationRadius);

        registerListener("minTargetAngularSize", EnhancedAimingModule::setMinTargetAngularSize);
        registerListener("blendFactor", EnhancedAimingModule::setBlendFactor);

        // others
        registerListener("autoGheadHealthThreshold", AutoGhead::setHealthThreshold);
        registerListener("autoJumpResetChance", AutoJumpReset::setChance);
        registerListener("pointerActivationRadius", Pointer::setActivationRadius);
        registerListener("sharkEspActivationRadius", SharkEsp::setActivationRadius);
        registerListener("invisEspActivationRadius", InvisEsp::setActivationRadius);
        registerListener("bedEspActivationRadiusBlocks", BedEsp::setActivationRadiusBlocks);
        registerListener("sharkEspLowHealthThreshold", SharkEsp::setLowHealthThreshold);
        registerListener("safewalkEdgeDistance", Safewalk::setEdgeDistance);
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