package me.calclb.aimer.combat;

import me.calclb.aimer.AntiBot;
import me.calclb.aimer.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.input.Mouse;

public class LeftAutoclicker {
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean isLeftMouseDown = false;
    private boolean isToggled = false;
    private boolean wasKeybindJustPressed = false;
    private long lastClickTime = 0;
    private long clickDelay = 0;
    private float rangeSqr = (float) Math.pow(4.5, 2);
    private int minCPS = 12;
    private int maxCPS = 14;

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

        double fov = Math.cos(Math.toRadians(30));

        boolean foundValidTarget = false;
        double closestDistanceSqr = rangeSqr;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase && entity != me) {
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

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        boolean isPressed = Main.getLeftAutoclickKey().isPressed();

        if (isPressed && !wasKeybindJustPressed) {
            isToggled = !isToggled;
            sendToggleMessage();
        }
        wasKeybindJustPressed = isPressed;

        if(!isInAValidStateToClick()) return;
        if (!Mouse.isButtonDown(0)) return;  // 0 is the left mouse button

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime >= clickDelay && isValidEntityInCrosshair()) {
            simulateMouseClick();
            resetClickDelay();
        }
    }

    private boolean isInAValidStateToClick() {
        if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) return false;
        if (mc.currentScreen != null) return false;
        if (mc.thePlayer.isUsingItem()) return false;
        return isToggled && isLeftMouseDown;
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