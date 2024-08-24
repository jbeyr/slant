package me.calclb.aimer.autoclicker;

import me.calclb.aimer.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class RightAutoclicker {
    private boolean isRightMouseDown = false;
    private boolean isToggled = false;
    private boolean wasKeybindJustPressed = false;

    private long lastClickTime = 0;
    private long clickDelay = 0;

    private int minCPS = 18;  // Minimum clicks per second
    private int maxCPS = 21; // Maximum clicks per second

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        if (event.button == 1) {  // 1 is the right mouse button
            isRightMouseDown = event.buttonstate;
            if (isRightMouseDown) {
                resetClickDelay();
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.START) return;
        boolean isPressed = Main.getRightAutoclickKey().isPressed();

        if (isPressed && !wasKeybindJustPressed) {
            isToggled = !isToggled;
            sendToggleMessage();
        }
        wasKeybindJustPressed = isPressed;

        if (!isToggled) return;
        if (!isRightMouseDown) return;
        if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) return;
        if (mc.currentScreen != null) return;
        if (mc.thePlayer.isUsingItem()) return;
        if (!isHoldingPlaceableBlock()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime >= clickDelay) {
            simulateRightClick();
            resetClickDelay();
        }
    }

    private boolean isHoldingPlaceableBlock() {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        return heldItem != null && heldItem.getItem() instanceof ItemBlock;
    }

    private void simulateRightClick() {
        MovingObjectPosition objectMouseOver = mc.objectMouseOver;

        if (objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos pos = objectMouseOver.getBlockPos();
            EnumFacing side = objectMouseOver.sideHit;

            if (mc.theWorld.getBlockState(pos).getBlock().canCollideCheck(mc.theWorld.getBlockState(pos), false)) {
                BlockPos placementPos = pos.offset(side);
                if (mc.thePlayer.canPlayerEdit(placementPos, side, mc.thePlayer.getHeldItem())) {
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                }
            }
        }
    }

    private void resetClickDelay() {
        lastClickTime = System.currentTimeMillis();
        clickDelay = getRandomClickDelay();
    }

    private long getRandomClickDelay() {
        long minDelay = (long)(1000 / (maxCPS*1.15f));
        long maxDelay = (long)(1000 / (minCPS*1.15f));
        return minDelay + (long) (Math.random() * (maxDelay - minDelay + 1));
    }

    private void sendToggleMessage() {
        String message = isToggled ? String.format("Right autoclicker toggled ON (%s..%s cps)", minCPS, maxCPS) : "Right autoclicker toggled OFF";
        mc.thePlayer.addChatMessage(new ChatComponentText(message));
    }
}