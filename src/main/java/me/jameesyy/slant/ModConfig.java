package me.jameesyy.slant;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
import me.jameesyy.slant.combat.*;
import me.jameesyy.slant.movement.AutoJumpReset;
import me.jameesyy.slant.movement.NoJumpDelay;
import me.jameesyy.slant.movement.Safewalk;
import me.jameesyy.slant.network.Backtrack;
import me.jameesyy.slant.network.PingSpoofer;
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

    @Property(type = PropertyType.SWITCH, name = "Safewalk", category = "Modules", description = "Sneak near the edge of blocks.")
    public static boolean safewalkEnabled = false;

        @Property(type = PropertyType.SWITCH, name = "Disable If Not Bridging Pitch", category = "Safewalk", description = "Toggles off the module after lifting your head back up.")
        public static boolean safewalkDisableIfNotBridgingPitch = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Edge Distance", category = "Safewalk", description = "The distance from the edge at which sneaking occurs.", maxF = 0.5f, decimalPlaces = 2)
        public static float safewalkEdgeDistance = 0.15f;

    @Property(type = PropertyType.SWITCH, name = "Backtrack", category = "Modules", description = "When fighting, uses a delayed hitbox of a target player to provide extra reach.")
    public static boolean backtrackEnabled = false;

        @Property(type = PropertyType.SLIDER, name = "Delay", category = "Backtrack", description = "The millesecond offset to delay the target hitbox by. Rule of thumb: max +5ms for every +50ms ping. If you have 100ms ping, set to no more than 10ms.", min = 1, max = 50)
        public static int backtrackDelayMs = 1;

        @Property(type = PropertyType.SLIDER, name = "Delay", category = "Backtrack", description = "Adjusts how aggressively the backtrack feature activates. Higher values increase the range and frequency of backtrack hits, potentially making them more noticeable. Lower values result in more subtle, conservative backtracking.", min = 0, max = 100, increment = 5)
        public static int backtrackSensitivity = 100;

    @Property(type = PropertyType.SWITCH, name = "Ping Spoofer", category = "Modules", description = "Increases your ping by a configurable offset.")
    public static boolean pingSpooferEnabled = false;

        @Property(type = PropertyType.SLIDER, name = "Delay", category = "Ping Spoofer", description = "The offset (milleseconds) to increase your own ping by.", min = 5, max = 500, increment = 5)
        public static int pingSpooferDelayMs = 50;

    @Property(type = PropertyType.SWITCH, name = "Anti Bot", category = "Modules", description = "Tells combat modules to ignore bots.")
    public static boolean antiBotEnabled = true;

        @Property(type = PropertyType.SWITCH, name = "Respect Teams", category = "Anti Bot", description = "Ignore teammates.")
        public static boolean antiBotRespectTeams = true;

    @Property(type = PropertyType.SWITCH, name = "Auto Ghead", category = "Modules", description = "Consumes golden heads when you're low on health.")
    public static boolean autoGheadEnabled = false;

    @Property(type = PropertyType.SWITCH, name = "Auto Tool", category = "Modules", description = "Swaps to the ideal tool to break a block.")
    public static boolean autoToolEnabled = false;

        @Property(type = PropertyType.SWITCH, name = "On Sneak Only", category = "Auto Tool", description = "Only swap if crouching and breaking a block.")
        public static boolean autoToolOnSneakOnly = false;

        @Property(type = PropertyType.SWITCH, name = "Near Bed Only", category = "Auto Tool", description = "Only swap if a bed is within a 10-block cuboid of you.")
        public static boolean autoToolNearBedOnly = false;

    @Property(type = PropertyType.SWITCH, name = "Auto Weapon", category = "Modules", description = "Sets your selected item to a weapon when attacking.")
    public static boolean autoWeaponEnabled = false;

        @Property(type = PropertyType.SWITCH, name = "Swap On Swing", category = "Auto Weapon", description = "If true, swaps when a target entity triggers LMB Autoclicker.")
        public static boolean autoWeaponSwapOnSwing = false;

    @Property(type = PropertyType.SWITCH, name = "Aimlock", category = "Modules", description = "Helps track the target hitbox when your crosshair enters it.")
    public static boolean aimlockEnabled = false;

        @Property(type = PropertyType.SELECTOR, name = "Target Priority", category = "Aimlock", description = "Focus aimlock on the initially hitscanned player, the closest player within the FOV range, or the lowest health player within the FOV range.", options = {"Initial Hitscan", "Closest FOV", "Lowest Health"})
        public static int aimlockTargetPriority = Aimlock.TargetPriority.INITIAL_HITSCAN.ordinal();

        @Property(type = PropertyType.SWITCH, name = "Vertical Rotations", category = "Aimlock")
        public static boolean aimlockVerticalRotations = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Activation FOV", category = "Aimlock", minF = 0, maxF = 360, decimalPlaces = 0)
        public static float aimlockFov = 360f;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Activation Radius", category = "Aimlock", minF = 1f, maxF = 8f, decimalPlaces = 2)
        public static float aimlockActivationRadius = 4.15f;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "Aim Strength", category = "Aimlock", minF = 0f, maxF = 10f, decimalPlaces = 1)
    public static float aimStrength = 1f;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "Minimum Target Angular Size (Degrees)", category="Aimlock", minF = 0.1f, maxF = 45f, decimalPlaces = 1)
    public static float minTargetAngularSize = 1f;

    @Property(type = PropertyType.DECIMAL_SLIDER, name = "Blend Strength", category="Aimlock", minF=0f, maxF=5f, decimalPlaces = 1)
    public static float blendFactor = 0.5f;

    @Property(type = PropertyType.COLOR, name = "Target Hitbox Color", category="Aimlock")
    public static Color aimlockTargetHitboxColor = new Color(255, 255, 255, 255);

    @Property(type = PropertyType.SWITCH, name = "Right Autoclicker", category = "Modules", description = "Places held blocks when you're pressing the 'use item' key.")
    public static boolean rightAutoclickerEnabled = false;

        @Property(type = PropertyType.SLIDER, name = "RMB: Min CPS", category = "Autoclicker", min = 8, max = 30)
        public static int rightAutoClickerMinCps = 18;

        @Property(type = PropertyType.SLIDER, name = "RMB: Max CPS", category = "Autoclicker", min = 8, max = 30)
        public static int rightAutoClickerMaxCps = 22;

        @Property(type = PropertyType.SWITCH, name = "RMB: Placing Blocks Only", category = "Autoclicker", description = "Only clicks for placing blocks.")
        public static boolean rightAutoClickerPlacingBlocksOnly = true;

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

    @Property(type = PropertyType.SWITCH, name = "Rod Recast", category = "Modules", description = "Immediately recasts the fishing rod once reeled in")
    public static boolean rodRecastEnabled = false;

    private ModConfig() {
        super(new File("./config/" + Main.MODID + ".toml"), "Slant Config");
        initialize();
    }

    public void setModulesToConfig() {
        AntiBot.setEnabled(antiBotEnabled);
        AntiBot.setRespectTeams(antiBotRespectTeams);

        AutoGhead.setEnabled(autoGheadEnabled);
        AutoWeapon.setEnabled(autoWeaponEnabled);
        AutoWeapon.setSwapOnSwing(autoWeaponSwapOnSwing);

        AutoTool.setEnabled(autoToolEnabled);
        AutoTool.setOnSneakOnly(autoToolOnSneakOnly);
        AutoTool.setNearBedOnly(autoToolNearBedOnly);

        Aimlock.setEnabled(aimlockEnabled);
        Aimlock.setFov(aimlockFov);
        Aimlock.setTargetPriority(Aimlock.TargetPriority.values()[aimlockTargetPriority]);
        Aimlock.setTargetHitboxColor(aimlockTargetHitboxColor);
        Aimlock.setVerticalRotations(aimlockVerticalRotations);
        Aimlock.setActivationRadius(aimlockActivationRadius);
        EnhancedAimingModule.setMinTargetAngularSize(minTargetAngularSize);
        EnhancedAimingModule.setBlendFactor(blendFactor);
        EnhancedAimingModule.setAimStrength(aimStrength);

        Backtrack.setEnabled(backtrackEnabled);
        Backtrack.setDelay(backtrackDelayMs);
        Backtrack.setSensitivity(backtrackSensitivity);

        PingSpoofer.setEnabled(pingSpooferEnabled);
        PingSpoofer.setDelay(pingSpooferDelayMs);

        RightAutoclicker.setEnabled(rightAutoclickerEnabled);
        RightAutoclicker.setMinCPS(rightAutoClickerMinCps);
        RightAutoclicker.setMaxCPS(rightAutoClickerMaxCps);
        RightAutoclicker.setPlacingBlocksOnly(rightAutoClickerPlacingBlocksOnly);

        LeftAutoclicker.setEnabled(leftAutoclickerEnabled);
        LeftAutoclicker.setActivationRadius(leftAutoclickerActivationRadius);
        LeftAutoclicker.setMinCPS(leftAutoClickerMinCps);
        LeftAutoclicker.setMaxCPS(leftAutoClickerMaxCps);

        NoHitDelay.setEnabled(noHitDelayEnabled);
        NoMiningDelay.setEnabled(noMiningDelayEnabled);

        AutoJumpReset.setEnabled(autoJumpResetEnabled);
        AutoJumpReset.setChance(autoJumpResetChance);
        NoJumpDelay.setEnabled(noJumpDelayEnabled);
        Safewalk.setDisableIfNotBridgingPitch(safewalkDisableIfNotBridgingPitch);
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

        RodRecast.setEnabled(rodRecastEnabled);
    }

    public void setupConfigCallbacks() {

        registerListener("antiBotEnabled", AntiBot::setEnabled);
        registerListener("autoGheadEnabled", AutoGhead::setEnabled);
        registerListener("autoWeaponEnabled", AutoWeapon::setEnabled);
        registerListener("autoToolEnabled", AutoTool::setEnabled);
        registerListener("aimlockEnabled", Aimlock::setEnabled);
        registerListener("rightAutoclickerEnabled", RightAutoclicker::setEnabled);
        registerListener("rightAutoClickerPlacingBlocksOnly", RightAutoclicker::setPlacingBlocksOnly);

        registerListener("leftAutoclickerEnabled", LeftAutoclicker::setEnabled);
        registerListener("autoJumpResetEnabled", AutoJumpReset::setEnabled);
        registerListener("noJumpDelayEnabled", NoJumpDelay::setEnabled);
        registerListener("noHitDelayEnabled", NoHitDelay::setEnabled);
        registerListener("noMiningDelayEnabled", NoMiningDelay::setEnabled);
        registerListener("bedEspEnabled", BedEsp::setEnabled);
        registerListener("invisEspEnabled", InvisEsp::setEnabled);
        registerListener("sharkEspEnabled", SharkEsp::setEnabled);
        registerListener("pointerEnabled", Pointer::setEnabled);
        registerListener("safewalkEnabled", Safewalk::setEnabled);
        registerListener("backtrackEnabled", Backtrack::setEnabled);
        registerListener("pingSpooferEnabled", PingSpoofer::setEnabled);

        registerListener("autoWeaponSwapOnSwing", AutoWeapon::setSwapOnSwing);
        registerListener("autoToolOnSneakOnly", AutoTool::setOnSneakOnly);
        registerListener("autoToolNearBedOnly", AutoTool::setNearBedOnly);
        registerListener("safewalkDisableIfNotBridgingPitch", Safewalk::setDisableIfNotBridgingPitch);
        registerListener("antiBotRespectTeams", AntiBot::setRespectTeams);

        registerListener("leftAutoClickerMinCps", LeftAutoclicker::setMinCPS);
        registerListener("leftAutoClickerMaxCps", LeftAutoclicker::setMaxCPS);
        registerListener("leftAutoclickerActivationRadius", LeftAutoclicker::setActivationRadius);
        registerListener("rightAutoClickerMinCps", RightAutoclicker::setMinCPS);
        registerListener("rightAutoClickerMaxCps", RightAutoclicker::setMaxCPS);


        registerListener("aimlockTargetHitboxColor", Aimlock::setTargetHitboxColor);
        registerListener("aimlockTargetPriority", (Integer i) -> Aimlock.setTargetPriority(Aimlock.TargetPriority.values()[i]));
        registerListener("aimlockVerticalRotations", Aimlock::setVerticalRotations);
        registerListener("aimlockFov", Aimlock::setFov);
        registerListener("aimlockActivationRadius", Aimlock::setActivationRadius);

        registerListener("pingSpooferDelayMs", PingSpoofer::setDelay);
        registerListener("backtrackDelayMs", Backtrack::setDelay);
        registerListener("backtrackSensitivity", Backtrack::setSensitivity);

        registerListener("aimStrength", EnhancedAimingModule::setAimStrength);
        registerListener("minTargetAngularSize", EnhancedAimingModule::setMinTargetAngularSize);
        registerListener("blendFactor", EnhancedAimingModule::setBlendFactor);

        registerListener("autoJumpResetChance", AutoJumpReset::setChance);
        registerListener("pointerActivationRadius", Pointer::setActivationRadius);
        registerListener("sharkEspActivationRadius", SharkEsp::setActivationRadius);
        registerListener("invisEspActivationRadius", InvisEsp::setActivationRadius);
        registerListener("bedEspActivationRadiusBlocks", BedEsp::setActivationRadiusBlocks);
        registerListener("sharkEspLowHealthThreshold", SharkEsp::setLowHealthThreshold);
        registerListener("safewalkEdgeDistance", Safewalk::setEdgeDistance);
        registerListener("rodRecastEnabled", RodRecast::setEnabled);
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