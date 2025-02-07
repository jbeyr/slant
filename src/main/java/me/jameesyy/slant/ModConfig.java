package me.jameesyy.slant;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
import me.jameesyy.slant.combat.*;
import me.jameesyy.slant.movement.AutoJumpReset;
import me.jameesyy.slant.movement.NoJumpDelay;
import me.jameesyy.slant.movement.Safewalk;
import me.jameesyy.slant.movement.Sprint;
import me.jameesyy.slant.network.Backtrack;
import me.jameesyy.slant.network.PingSpoofer;
import me.jameesyy.slant.render.*;
import me.jameesyy.slant.util.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.awt.*;
import java.io.File;

public class ModConfig extends Vigilant {

    private static ModConfig INSTANCE;

    // region block hit
    @Property(type = PropertyType.SWITCH, name = "Block Hit", category = "Modules", description = "Blocks your sword between attacks to reduce damage.")
    public static boolean blockHitEnabled = false;

    // endregion


    @Property(type = PropertyType.SWITCH, name = "Aim Assist", category = "Modules", description = "Aim towards players for you.")
    public static boolean enabled = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Lock-on FOV", category = "Aim Assist", minF = 0, maxF = 360f, decimalPlaces = 1)
        public static float lockFov = 15f;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Lock-on Aim Speed Multiplier", category = "Aim Assist", minF = 0f, maxF = 5f, decimalPlaces = 1)
        public static float lockAimSpeedMultiplier = 1f;

        @Property(type = PropertyType.SWITCH, name = "Only Players", category = "Aim Assist", description = "Only aims on players")
        public static boolean onlyPlayers = true;
    
        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Horizontal speed", category = "Aim Assist", minF = 0, maxF = 1000f, decimalPlaces = 1)
        public static float horizontalSpeed = 5f;
    
        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Vertical speed", category = "Aim Assist", minF = 0, maxF = 10f, decimalPlaces = 1)
        public static float verticalSpeed = 2f;
    
        @Property(type = PropertyType.PERCENT_SLIDER, name = "Horizontal Multipoint", category = "Aim Assist", decimalPlaces = 2)
        public static float horizontalMultipoint = 0.5f;
    
        @Property(type = PropertyType.PERCENT_SLIDER, name = "Vertical Multipoint", category = "Aim Assist", decimalPlaces = 2)
        public static float verticalMultipoint = 0.5f;
    
        @Property(type = PropertyType.PERCENT_SLIDER, name = "Predict", category = "Aim Assist", decimalPlaces = 2)
        public static float predict = 0.5f;
    
        @Property(type = PropertyType.PERCENT_SLIDER, name = "Randomization", category = "Aim Assist", decimalPlaces = 2)
        public static float randomization = 0.5f;
    
        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Min Range", category = "Aim Assist", minF = 0, maxF = 10f, decimalPlaces = 2)
        public static float minRange = 1f;
    
        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Max Range", category = "Aim Assist", minF = 0, maxF = 10f, decimalPlaces = 2)
        public static float maxRange = 4f;
    
        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Min FOV", category = "Aim Assist", minF = 0, maxF = 360, decimalPlaces = 0)
        public static float minFOV = 0f;
    
        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Max FOV", category = "Aim Assist", minF = 0, maxF = 360, decimalPlaces = 0)
        public static float maxFOV = 100f;
    
        @Property(type = PropertyType.SWITCH, name = "Increase fov while locked", category = "Aim Assist")
        public static boolean increasedFOVWhileLocked = true;
    
        @Property(type = PropertyType.SWITCH, name = "Consider better manual aim", category = "Aim Assist")
        private boolean considerBetterManualAim = true;

    // endregion





    // TODO use the reflections + lombok libs to recursively map to these fields in their package
    // less explicit but I'd rather not deal with this much boilerplate

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

    @Property(type = PropertyType.SLIDER, name = "Sensitivity", category = "Backtrack", description = "Adjusts how aggressively the backtrack feature activates. Higher values increase the range and frequency of backtrack hits, potentially making them more noticeable. Lower values result in more subtle, conservative backtracking.", min = 0, max = 100, increment = 5)
        public static int backtrackSensitivity = 100;

    @Property(type = PropertyType.SWITCH, name = "Ping Spoofer", category = "Modules", description = "Increases your ping by a configurable offset.")
    public static boolean pingSpooferEnabled = false;

    @Property(type = PropertyType.SLIDER, name = "Delay", category = "Ping Spoofer", description = "The offset (milleseconds) to increase your own ping by.", min = 5, max = 300, increment = 5)
        public static int pingSpooferDelayMs = 50;

    @Property(type = PropertyType.SWITCH, name = "Anti Bot", category = "Modules", description = "Tells combat modules to ignore bots.")
    public static boolean antiBotEnabled = true;

        @Property(type = PropertyType.SWITCH, name = "Only Players", category = "Anti Bot", description = "Tells combat modules to only target players.")
        public static boolean antiBotOnlyPlayers = true;

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


    @Property(type = PropertyType.SWITCH, name = "Better Aim", category = "Modules", description = "Helps track the target hitbox when your crosshair enters it.")
    public static boolean betterAimEnabled = false;

        @Property(type = PropertyType.SELECTOR, name = "Target Priority", category = "Better Aim", description = "Focus betterAim on the initially hitscanned player, the closest player within the FOV range, or the lowest health player within the FOV range.", options = {"Initial Hitscan", "Closest FOV", "Lowest Health"})
            public static int betterAimTargetPriority = BetterAim.TargetPriority.INITIAL_HITSCAN.ordinal();

        @Property(type = PropertyType.SWITCH, name = "Vertical Rotations", category = "Better Aim")
            public static boolean betterAimVerticalRotations = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Activation FOV", category = "Better Aim", minF = 0, maxF = 360, decimalPlaces = 0)
            public static float betterAimFov = 360f;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Activation Radius", category = "Better Aim", minF = 1f, maxF = 8f, decimalPlaces = 2)
            public static float betterAimActivationRadius = 4.15f;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Minimum Target Angular Size (Degrees)", category="Better Aim", minF = 0.1f, maxF = 45f, decimalPlaces = 1)
        public static float minTargetAngularSize = 1f;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Blend Strength", category="Better Aim", minF=0f, maxF=5f, decimalPlaces = 1)
        public static float blendFactor = 0.5f;

        @Property(type = PropertyType.COLOR, name = "Target Hitbox Color", category="Better Aim")
        public static Color betterAimTargetHitboxColor = new Color(255, 255, 255, 255);

    @Property(type = PropertyType.SWITCH, name = "Right Autoclicker", category = "Modules", subcategory = "Right", description = "Places held blocks when you're pressing the 'use item' key.")
    public static boolean rightAutoclickerEnabled = false;

        @Property(type = PropertyType.SLIDER, name = "RMB: Min CPS", category = "Autoclicker", subcategory = "Right", min = 8, max = 30)
        public static int rightAutoClickerMinCps = 18;

        @Property(type = PropertyType.SLIDER, name = "RMB: Max CPS", category = "Autoclicker", subcategory = "Right", min = 8, max = 30)
        public static int rightAutoClickerMaxCps = 22;

        @Property(type = PropertyType.SWITCH, name = "RMB: Placing Blocks Only", category = "Autoclicker", subcategory = "Right", description = "Only clicks for placing blocks.")
        public static boolean rightAutoClickerPlacingBlocksOnly = true;

    @Property(type = PropertyType.SWITCH, name = "Left Autoclicker", category = "Modules", subcategory = "Left", description = "Swings when a player is in front of you.")
    public static boolean leftAutoclickerEnabled = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "LMB: Activation Range", category = "Autoclicker", subcategory = "Left", minF = 1f, maxF = 8f, decimalPlaces = 2)
        public static float leftAutoclickerActivationRadius = 4.5f;

        @Property(type = PropertyType.SWITCH, name = "LMB: Respect Hurt Ticks", category = "Autoclicker", subcategory = "Left", description = "Click only when the target player isn't in a hurt-tick invulnerability period.")
        public static boolean leftAutoclickerRespectHurtTicks = false;

        @Property(type = PropertyType.SWITCH, name = "LMB: Click If Mouse Down", category = "Autoclicker", subcategory = "Left", description = "Click only when your mouse is down and there's a target nearby.")
        public static boolean leftAutoclickerTriggerIfMouseDown = false;

        @Property(type = PropertyType.SWITCH, name = "LMB: Click If Target Near FOV", category = "Autoclicker", subcategory = "Left", description = "Click if the target is in your general direction, not just when your crosshair is directly over a target.")
        public static boolean leftAutoclickerTriggerIfNearFov = false;

        @Property(type = PropertyType.SLIDER, name = "LMB: Min CPS", category = "Autoclicker", subcategory = "Left", min = 8, max = 25)
        public static int leftAutoClickerMinCps = 12;

        @Property(type = PropertyType.SLIDER, name = "LMB: Max CPS", category = "Autoclicker", subcategory = "Left", min = 8, max = 25)
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

    @Property(type = PropertyType.SWITCH, name = "Shark ESP: Render 3d Hitboxes", category = "Modules", subcategory = "ESP")
    public static boolean sharkEspRender3dHitboxes = false;

    @Property(type = PropertyType.PERCENT_SLIDER, name = "Shark ESP: Low Health Threshold", category = "ESP")
        public static float sharkEspLowHealthThreshold = 0.5f;

    @Property(type = PropertyType.SWITCH, name = "Rod Recast", category = "Modules", description = "Immediately recasts the fishing rod once reeled in")
    public static boolean rodRecastEnabled = false;

    @Property(type = PropertyType.SWITCH, name = "Quick Maths Solver", category = "Modules", description = "Answers QUICK MATHS! events.")
    public static boolean quickMathsSolverEnabled = false;

        @Property(type = PropertyType.SLIDER, name = "Reply After X Solvers", category = "Quick Maths Solver", description = "Solve with the provided delay once X players answered correctly.", min=0, max=4)
        public static int quickMathsSolverReplyAfterXSolvers = 2;

        @Property(type = PropertyType.SLIDER, name = "Min. Response Ticks", category = "Quick Maths Solver", min=0, max=120)
        public static int quickMathsSolverMinResponseTicks = 5;

        @Property(type = PropertyType.SLIDER, name = "Max. Response Ticks", category = "Quick Maths Solver", min=0, max=120)
        public static int quickMathsSolverMaxResponseTicks = 15;

    @Property(type = PropertyType.SWITCH, name = "Chest ESP", category = "Modules", description = "Shows chest block entities.")
    public static boolean chestEspEnabled = false;

        @Property(type = PropertyType.SLIDER, name = "Radius", category = "Chest ESP", min=1, max=100)
        public static int chestEspRadius = 20;

        @Property(type = PropertyType.COLOR, name = "Bounding Box Color", category="Chest ESP")
        public static Color chestEspBboxColor = new Color(255, 255, 255, 255);


    @Property(type = PropertyType.SWITCH, name = "Tracers", category = "Modules", description = "Renders lines pointing to nearby entities.")
    public static boolean tracersEnabled = false;

        @Property(type = PropertyType.SWITCH, name = "Respect Line of Sight", category = "Tracers")
        public static boolean tracersRespectLineOfSight = false;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Camera Distance", category = "Tracers", minF=-10f, maxF=100f)
        public static float tracersCameraDistance = 1f;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Max Distance", category = "Tracers", minF=1f, maxF=100f)
        public static float tracersMaxDistance = 64f;

        @Property(type = PropertyType.DECIMAL_SLIDER, name = "Max Vertical Distance", category = "Tracers", minF=1f, maxF=100f)
        public static float tracersMaxVerticalDistance = 10f;

        @Property(type = PropertyType.SLIDER, name = "Max Lines", category = "Tracers", min=1, max=100)
        public static int tracersMaxLines = 10;

    @Property(type = PropertyType.SWITCH, name = "Sprint", category = "Modules", description = "Automatically holds sprint for you.")
    public static boolean sprintEnabled = false;

    private ModConfig() {
        super(new File("./config/" + Main.MODID + ".toml"), "Slant Config");
        initialize();
    }

    public void setModulesToConfig() {
        AntiBot.setEnabled(antiBotEnabled);
        AntiBot.setOnlyPlayers(antiBotOnlyPlayers);
        AntiBot.setRespectTeams(antiBotRespectTeams);

        AutoGhead.setEnabled(autoGheadEnabled);
        AutoWeapon.setEnabled(autoWeaponEnabled);
        AutoWeapon.setSwapOnSwing(autoWeaponSwapOnSwing);

        AutoTool.setEnabled(autoToolEnabled);
        AutoTool.setOnSneakOnly(autoToolOnSneakOnly);
        AutoTool.setNearBedOnly(autoToolNearBedOnly);

        BlockHit.setEnabled(blockHitEnabled);

        BetterAim.setEnabled(betterAimEnabled);
        BetterAim.setFov(betterAimFov);
        BetterAim.setTargetPriority(BetterAim.TargetPriority.values()[betterAimTargetPriority]);
        BetterAim.setTargetHitboxColor(betterAimTargetHitboxColor);
        BetterAim.setVerticalRotations(betterAimVerticalRotations);
        BetterAim.setActivationRadius(betterAimActivationRadius);
        EnhancedAimingModule.setMinTargetAngularSize(minTargetAngularSize);
        EnhancedAimingModule.setBlendFactor(blendFactor);
        EnhancedAimingModule.setAimStrength(10f); // hardcoding this since it has no real effect past 1f

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
        SharkEsp.setRender3dHitboxes(sharkEspRender3dHitboxes);
        SharkEsp.setLowHealthThreshold(sharkEspLowHealthThreshold);

        RodRecast.setEnabled(rodRecastEnabled);

        LeftAutoclicker.setTriggerIfMouseDown(leftAutoclickerTriggerIfMouseDown);
        LeftAutoclicker.setTriggerIfNearFov(leftAutoclickerTriggerIfNearFov);
        LeftAutoclicker.setRespectHurtTicks(leftAutoclickerRespectHurtTicks);
        QuickMathsSolver.setEnabled(quickMathsSolverEnabled);
        QuickMathsSolver.setReplyAfterXSolvers(quickMathsSolverReplyAfterXSolvers);
        QuickMathsSolver.setMinResponseTicks(quickMathsSolverMinResponseTicks);
        QuickMathsSolver.setMaxResponseTicks(quickMathsSolverMaxResponseTicks);
        QuickMathsSolver.setReplyAfterXSolvers(quickMathsSolverReplyAfterXSolvers);

        ChestEsp.setEnabled(chestEspEnabled);
        ChestEsp.setBboxColor(chestEspBboxColor);
        ChestEsp.setRadius(chestEspRadius);

        Tracers.setEnabled(tracersEnabled);
        Tracers.setMaxLines(tracersMaxLines);
        Tracers.setMaxDistance(tracersMaxDistance);
        Tracers.setCameraDistance(tracersCameraDistance);
        Tracers.setRespectLineOfSight(tracersRespectLineOfSight);
        Tracers.setMaxVerticalDistance(tracersMaxVerticalDistance);
        Sprint.setEnabled(sprintEnabled);

        AimAssist.setEnabled(enabled);
        AimAssist.setLockAimSpeedMultiplier(lockAimSpeedMultiplier);
        AimAssist.setLockFov(lockFov);
        AimAssist.setOnlyPlayers(onlyPlayers);
        AimAssist.setHorizontalSpeed(horizontalSpeed);
        AimAssist.setVerticalSpeed(verticalSpeed);
        AimAssist.setHorizontalMultipoint(horizontalMultipoint);
        AimAssist.setVerticalMultipoint(verticalMultipoint);
        AimAssist.setPredict(predict);
        AimAssist.setRandomization(randomization);
        AimAssist.setMinRange(minRange);
        AimAssist.setMaxRange(maxRange);
        AimAssist.setMinFOV(minFOV);
        AimAssist.setMaxFOV(maxFOV);
        AimAssist.setIncreasedFOVWhileLocked(increasedFOVWhileLocked);
        AimAssist.setConsiderBetterManualAim(considerBetterManualAim);
    }

    public void setupConfigCallbacks() {
        registerListener("enabled", AimAssist::setEnabled);
        registerListener("lockFov", AimAssist::setLockFov);
        registerListener("lockAimSpeedMultiplier", AimAssist::setLockAimSpeedMultiplier);
        registerListener("onlyPlayers", AimAssist::setOnlyPlayers);
        registerListener("horizontalSpeed", AimAssist::setHorizontalSpeed);
        registerListener("verticalSpeed", AimAssist::setVerticalSpeed);
        registerListener("horizontalMultipoint", AimAssist::setHorizontalMultipoint);
        registerListener("verticalMultipoint", AimAssist::setVerticalMultipoint);
        registerListener("predict", AimAssist::setPredict);
        registerListener("randomization", AimAssist::setRandomization);
        registerListener("minRange", AimAssist::setMinRange);
        registerListener("maxRange", AimAssist::setMaxRange);
        registerListener("minFOV", AimAssist::setMinFOV);
        registerListener("maxFOV", AimAssist::setMaxFOV);
        registerListener("increasedFOVWhileLocked", AimAssist::setIncreasedFOVWhileLocked);
        registerListener("considerBetterManualAim", AimAssist::setConsiderBetterManualAim);

        registerListener("antiBotEnabled", AntiBot::setEnabled);
        registerListener("antiBotOnlyPlayers", AntiBot::setOnlyPlayers);
        registerListener("antiBotRespectTeams", AntiBot::setRespectTeams);

        registerListener("autoGheadEnabled", AutoGhead::setEnabled);
        registerListener("autoWeaponEnabled", AutoWeapon::setEnabled);
        registerListener("autoToolEnabled", AutoTool::setEnabled);
        registerListener("betterAimEnabled", BetterAim::setEnabled);
        registerListener("rightAutoclickerEnabled", RightAutoclicker::setEnabled);
        registerListener("rightAutoClickerPlacingBlocksOnly", RightAutoclicker::setPlacingBlocksOnly);

        registerListener("blockHitEnabled", BlockHit::setEnabled);
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

        registerListener("leftAutoclickerRespectHurtTicks", LeftAutoclicker::setRespectHurtTicks);
        registerListener("leftAutoclickerTriggerIfMouseDown", LeftAutoclicker::setTriggerIfMouseDown);
        registerListener("leftAutoclickerTriggerIfNearFov", LeftAutoclicker::setTriggerIfNearFov);
        registerListener("leftAutoClickerMinCps", LeftAutoclicker::setMinCPS);
        registerListener("leftAutoClickerMaxCps", LeftAutoclicker::setMaxCPS);
        registerListener("leftAutoclickerActivationRadius", LeftAutoclicker::setActivationRadius);
        registerListener("rightAutoClickerMinCps", RightAutoclicker::setMinCPS);
        registerListener("rightAutoClickerMaxCps", RightAutoclicker::setMaxCPS);

        registerListener("betterAimTargetHitboxColor", BetterAim::setTargetHitboxColor);
        registerListener("betterAimTargetPriority", (Integer i) -> BetterAim.setTargetPriority(BetterAim.TargetPriority.values()[i]));
        registerListener("betterAimVerticalRotations", BetterAim::setVerticalRotations);
        registerListener("betterAimFov", BetterAim::setFov);
        registerListener("betterAimActivationRadius", BetterAim::setActivationRadius);

        registerListener("pingSpooferDelayMs", PingSpoofer::setDelay);
        registerListener("backtrackDelayMs", Backtrack::setDelay);
        registerListener("backtrackSensitivity", Backtrack::setSensitivity);

        registerListener("minTargetAngularSize", EnhancedAimingModule::setMinTargetAngularSize);
        registerListener("blendFactor", EnhancedAimingModule::setBlendFactor);

        registerListener("autoJumpResetChance", AutoJumpReset::setChance);
        registerListener("pointerActivationRadius", Pointer::setActivationRadius);
        registerListener("sharkEspActivationRadius", SharkEsp::setActivationRadius);
        registerListener("sharkEspRender3dHitboxes", SharkEsp::setRender3dHitboxes);
        registerListener("invisEspActivationRadius", InvisEsp::setActivationRadius);
        registerListener("bedEspActivationRadiusBlocks", BedEsp::setActivationRadiusBlocks);
        registerListener("sharkEspLowHealthThreshold", SharkEsp::setLowHealthThreshold);
        registerListener("safewalkEdgeDistance", Safewalk::setEdgeDistance);
        registerListener("rodRecastEnabled", RodRecast::setEnabled);
        registerListener("quickMathsSolverEnabled", QuickMathsSolver::setEnabled);
        registerListener("quickMathsSolverReplyAfterXSolvers", QuickMathsSolver::setReplyAfterXSolvers);
        registerListener("quickMathsSolverMinResponseTicks", QuickMathsSolver::setMinResponseTicks);
        registerListener("quickMathsSolverMaxResponseTicks", QuickMathsSolver::setMaxResponseTicks);

        registerListener("chestEspEnabled", ChestEsp::setEnabled);
        registerListener("chestEspBboxColor", ChestEsp::setBboxColor);
        registerListener("chestEspRadius", ChestEsp::setRadius);

        registerListener("tracersEnabled", Tracers::setEnabled);
        registerListener("tracersMaxLines", Tracers::setMaxLines);
        registerListener("tracersMaxDistance", Tracers::setMaxDistance);
        registerListener("tracersCameraDistance", Tracers::setCameraDistance);
        registerListener("tracersRespectLineOfSight", Tracers::setRespectLineOfSight);
        registerListener("tracersMaxVerticalDistance", Tracers::setMaxVerticalDistance);

        registerListener("sprintEnabled", Sprint::setEnabled);
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