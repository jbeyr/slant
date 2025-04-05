package me.jameesyy.slant.combat;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.Main;
import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.AntiBot;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.input.Mouse;

import java.util.Optional;

public class LeftAutoclicker {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static long lastClickTime = 0;
    private static long clickDelay = 0;

    private static float rangeSqr;
    private static int minCPS;
    private static int maxCPS;

    private static boolean enabled;
    private static boolean respectHurtTicks;
    private static boolean triggerIfNearFov;
    private static boolean triggerIfMouseDown;

    public static boolean isRespectHurtTicks() {
        return respectHurtTicks;
    }

    public static void setRespectHurtTicks(boolean b) {
        respectHurtTicks = b;
        ModConfig.leftAutoclickerRespectHurtTicks = b;
        Reporter.queueSetMsg("Left Autoclicker", "Respect Hurt Ticks", b);
    }

    public static void setActivationRadius(float range) {
        LeftAutoclicker.rangeSqr = range * range;
        Reporter.queueSetMsg("LMB Autoclicker", "Activation Radius", range);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.leftAutoclickerEnabled = b;
        Reporter.queueEnableMsg("LMB Autoclicker", b);
    }

    public static float getActivationRangeSqr() {
        return rangeSqr;
    }

    public static int getMaxCPS() {
        return maxCPS;
    }

    public static void setMaxCPS(int cps) {
        LeftAutoclicker.maxCPS = cps;
        ModConfig.leftAutoClickerMaxCps = cps;
        Reporter.queueSetMsg("LMB Autoclicker", "Max CPS", cps);
    }

    public static int getMinCPS() {
        return minCPS;
    }

    public static void setMinCPS(int cps) {
        LeftAutoclicker.minCPS = cps;
        ModConfig.leftAutoClickerMinCps = cps;
        Reporter.queueSetMsg("LMB Autoclicker", "Min CPS", cps);
    }

    public static Optional<Entity> crosshair() {
        EntityPlayer me = mc.thePlayer;
        if (me == null) return Optional.empty();

        MovingObjectPosition omo = mc.objectMouseOver;
        if (omo != null && omo.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            Entity en = omo.entityHit;
            if (!(en instanceof EntityLivingBase)) return Optional.empty();
            if ((AntiBot.isRecommendedTarget((EntityLivingBase)en)) && (!respectHurtTicks || (en.hurtResistantTime == 0 || 5 < en.hurtResistantTime && en.hurtResistantTime < 20))
            ) {
                return Optional.of(en);
            }
        }
        return Optional.empty();
    }

    public static void legitLeftClick() {
        int key = mc.gameSettings.keyBindAttack.getKeyCode();

        KeyBinding.setKeyBindState(key, true);
        KeyBinding.onTick(key);
        KeyBinding.setKeyBindState(key, false);
    }

    public static boolean shouldClick() {
        return enabled
                && crosshair().isPresent()
                && ActionConflictResolver.isClickAllowed()
                && (Mouse.isButtonDown(0) || !triggerIfMouseDown)
                && hasCooldownExpired();
    }

    public static void resetClickDelay() {
        lastClickTime = System.currentTimeMillis();
        clickDelay = getRandomClickDelay();
    }

    private static boolean hasCooldownExpired() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastClickTime >= clickDelay;
    }

    private static long getRandomClickDelay() {
        long minDelay = 1000 / maxCPS;
        long maxDelay = 1000 / minCPS;
        return minDelay + (long) (Math.random() * (maxDelay - minDelay + 1));
    }

    public static void setTriggerIfNearFov(boolean b) {
        triggerIfNearFov = b;
        ModConfig.leftAutoclickerTriggerIfNearFov = b;
        Reporter.queueSetMsg("Autoclicker", "Trigger If Near FOV", b);
    }

    public static void setTriggerIfMouseDown(boolean b) {
        triggerIfMouseDown = b;
        ModConfig.leftAutoclickerTriggerIfMouseDown = b;
        Reporter.queueSetMsg("Autoclicker", "Trigger If Mouse Down", b);
    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        if (event.button == 0 && event.buttonstate) {  // 0 is the left mouse button; buttonstate is true if pressed
            resetClickDelay();
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (Main.getLeftAutoclickKey().isPressed()) setEnabled(!enabled);
        if (Main.getTriggerBotKey().isPressed()) setTriggerIfMouseDown(!triggerIfMouseDown);

        if (shouldClick()) {
            if (AutoWeapon.isEnabled() && AutoWeapon.shouldSwapOnSwing() && ActionConflictResolver.isHotbarSelectedSlotChangeAllowed())
                AutoWeapon.swap();
            legitLeftClick();
        }
    }
}