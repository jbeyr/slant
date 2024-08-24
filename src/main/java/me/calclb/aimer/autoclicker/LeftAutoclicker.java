package me.calclb.aimer.autoclicker;

import me.calclb.aimer.AntiBot;
import me.calclb.aimer.Main;
import me.calclb.aimer.Pointer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;


public class LeftAutoclicker {
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean isLeftMouseDown = false;
    private boolean isToggled = false; // Track the toggle state
    private boolean wasKeybindJustPressed = false; // Track the previous state of the toggle key
    private long lastClickTime = 0;
    private long clickDelay = 0;
    private float rangeSqr = 25f;
    private int minCPS = 12;  // Minimum clicks per second
    private int maxCPS = 15; // Maximum clicks per second

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        if (event.button == 0) {  // 0 is the left mouse button
            isLeftMouseDown = event.buttonstate;  // True if pressed, false if released
            if (isLeftMouseDown) {
                resetClickDelay(); // Reset the delay when the mouse button is pressed
            }
        }
    }

    private boolean isValidEntityInCrosshair() {
        EntityPlayer me = mc.thePlayer;
        if (me == null) return false;

        Vec3 lookVec = me.getLookVec();
        Vec3 eyePos = me.getPositionEyes(1.0F);

        double fov = Math.cos(Math.toRadians(30)); // 30 degree FOV

        boolean foundValidTarget = false;
        double closestDistanceSqr = rangeSqr;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer && entity != me && AntiBot.isValidTarget((EntityPlayer) entity, rangeSqr)) {
                Vec3 toEntity = entity.getPositionVector().addVector(0, entity.getEyeHeight() / 2, 0).subtract(eyePos);
                double distanceSqr = toEntity.lengthVector();

                if (distanceSqr <= rangeSqr) {
                    Vec3 toEntityNormalized = toEntity.normalize();
                    double dotProduct = lookVec.dotProduct(toEntityNormalized);
                    if (dotProduct > fov) {
                        foundValidTarget = true;
                        if (distanceSqr < closestDistanceSqr) {
                            closestDistanceSqr = distanceSqr;
                        }
                    }
                }
            }
        }

        // If no valid target found in FOV, check the crosshair
        if (!foundValidTarget) {
            MovingObjectPosition objectMouseOver = mc.objectMouseOver;
            if (objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                Entity entity = objectMouseOver.entityHit;
                if (entity instanceof EntityPlayer && AntiBot.isValidTarget((EntityPlayer) entity, rangeSqr)) {
                    foundValidTarget = true;
                    closestDistanceSqr = me.getDistanceSqToEntity(entity);
                }
            }
        }

        return foundValidTarget;
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        boolean isPressed = Main.getLeftAutoclickKey().isPressed();

        if (isPressed && !wasKeybindJustPressed) {
            isToggled = !isToggled;
            sendToggleMessage();
        }
        wasKeybindJustPressed = isPressed;

        if (!isToggled) return;
        if (!isLeftMouseDown) return;
        if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) return;
        if (mc.currentScreen != null) return;
        if (mc.thePlayer.isUsingItem()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime >= clickDelay && isValidEntityInCrosshair()) {
            simulateMouseClick();
            resetClickDelay();
        }
    }

    private void simulateMouseClick() {
        MovingObjectPosition objectMouseOver = mc.objectMouseOver;

        if (objectMouseOver != null) {
            switch (objectMouseOver.typeOfHit) {
                case ENTITY:
                    Entity entity = objectMouseOver.entityHit;
                    if (entity != null) {
                        mc.thePlayer.swingItem();
                        mc.playerController.attackEntity(mc.thePlayer, entity);
                    }
                    break;
                case BLOCK:
                    KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                    break;
                case MISS:
                    mc.thePlayer.swingItem();
            }
        }
    }

    private void resetClickDelay() {
        lastClickTime = System.currentTimeMillis();
        clickDelay = getRandomClickDelay();
    }

    private long getRandomClickDelay() {
        long minDelay = 1000 / maxCPS;
        long maxDelay = 1000 / minCPS;
        return minDelay + (long) (Math.random() * (maxDelay - minDelay + 1));
    }

    private void sendToggleMessage() {
        String message = isToggled ? String.format("Left autoclicker toggled ON (%s..%s cps)", minCPS, maxCPS) : "Left autoclicker toggled OFF";
        mc.thePlayer.addChatMessage(new ChatComponentText(message));
    }
}
