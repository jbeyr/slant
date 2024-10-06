package me.jameesyy.slant.combat;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.Main;
import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.render.Pointer;
import me.jameesyy.slant.util.AntiBot;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.input.Mouse;

public class LeftAutoclicker {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static long lastClickTime = 0;
    private static long clickDelay = 0;

    private static float rangeSqr;
    private static int minCPS;
    private static int maxCPS;

    private static boolean enabled;

    public static void setActivationRadius(float range) {
        LeftAutoclicker.rangeSqr = range * range;
        Reporter.reportSet("LMB Autoclicker", "Activation Radius", range);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.leftAutoclickerEnabled = b;
        Reporter.reportToggled("LMB Autoclicker", b);
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
        Reporter.reportSet("LMB Autoclicker", "Max CPS", cps);
    }

    public static int getMinCPS() {
        return minCPS;
    }

    public static void setMinCPS(int cps) {
        LeftAutoclicker.minCPS = cps;
        ModConfig.leftAutoClickerMinCps = cps;
        Reporter.reportSet("LMB Autoclicker", "Min CPS", cps);
    }

    public static boolean isValidEntityInCrosshair() {
        EntityPlayer me = mc.thePlayer;
        if (me == null) return false;

        Vec3 lookVec = me.getLookVec();
        Vec3 eyePos = me.getPositionEyes(1.0F);

        double fov = Math.cos(Math.toRadians(45));

        boolean foundValidTarget = false;
        double closestDistanceSqr = rangeSqr;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) || entity == me) continue;
            EntityLivingBase leb = (EntityLivingBase) entity;

            Vec3 toEntity = leb.getPositionVector().addVector(0, leb.getEyeHeight() / 2, 0).subtract(eyePos);
            double distance = toEntity.lengthVector();
            double distSqr = distance * distance;

            if (distSqr > rangeSqr) continue;

            Vec3 toEntityNormalized = toEntity.normalize();
            double dotProduct = lookVec.dotProduct(toEntityNormalized);
            if (dotProduct <= fov) continue;

            if (Pointer.getVisiblePart(me, leb) == null) continue;

            foundValidTarget = true;
            if (distSqr < closestDistanceSqr) {
                closestDistanceSqr = distSqr;
            }
        }

        if (!foundValidTarget) {
            MovingObjectPosition objectMouseOver = mc.objectMouseOver;
            if (objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                Entity entity = objectMouseOver.entityHit;
                if (entity instanceof EntityPlayer && AntiBot.isRecommendedTarget((EntityPlayer) entity, rangeSqr) || entity instanceof EntityFireball || (entity instanceof EntityLiving && entity.isEntityAlive())) {
                    foundValidTarget = true;
                    closestDistanceSqr = me.getDistanceSqToEntity(entity);
                }
            }
        }

        return foundValidTarget;
    }

    public static void legitLeftClick() {
        int key = mc.gameSettings.keyBindAttack.getKeyCode();

        KeyBinding.setKeyBindState(key, true);
        KeyBinding.onTick(key);
        KeyBinding.setKeyBindState(key, false);
    }

    public static boolean shouldClick() {
        return enabled
                && ActionConflictResolver.isClickAllowed()
                && Mouse.isButtonDown(0)
                && hasCooldownExpired()
                && isValidEntityInCrosshair();
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

        if (shouldClick()) {
            if (AutoWeapon.isEnabled() && AutoWeapon.shouldSwapOnSwing()) AutoWeapon.swap();
            legitLeftClick();
        }
    }
}