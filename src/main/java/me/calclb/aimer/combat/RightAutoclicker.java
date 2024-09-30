package me.calclb.aimer.combat;

import me.calclb.aimer.Main;
import me.calclb.aimer.Reporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.input.Mouse;

public class RightAutoclicker {
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean isRightMouseDown = false;
    private long lastClickTime = 0;
    private long clickDelay = 0;

    private static boolean enabled;
    private static int minCPS = 19;  // Minimum clicks per second
    private static int maxCPS = 22; // Maximum clicks per second

    public static void setEnabled(boolean b) {
        enabled = b;
        Reporter.reportToggled("RMB Autoclicker", b);
    }

    public static void setMinCPS(int cps) {
        RightAutoclicker.minCPS = cps;
        Reporter.reportSet("RMB Autoclicker", "Min CPS", cps);

    }

    public static void setMaxCPS(int cps) {
        RightAutoclicker.maxCPS = cps;
        Reporter.reportSet("RMB Autoclicker", "Max CPS", cps);

    }

    public static int getMinCPS() {
        return minCPS;
    }

    public static int getMaxCPS() {
        return maxCPS;
    }

    public static boolean isEnabled() {
        return enabled;
    }

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
        if (event.phase != TickEvent.Phase.START) return;
        if (Main.getRightAutoclickKey().isPressed()) setEnabled(!enabled);
        if (!enabled) return;
        if (!isInAValidStateToClick()) return;

        // Check the actual state of the right mouse button each tick
        if (!Mouse.isButtonDown(1)) return;  // 1 is the right mouse button

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime >= clickDelay) {
            simulateRightClick();
            resetClickDelay();
        }
    }

    private boolean isInAValidStateToClick() {
        if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) return false;
        if (mc.currentScreen != null) return false;
        if (mc.thePlayer.isUsingItem()) return false;
        if (!isHoldingPlaceableBlock()) return false;
        if (isHoldingInteractableBlock()) return false;
        return isRightMouseDown;
    }

    private boolean isHoldingPlaceableBlock() {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        return heldItem != null && heldItem.getItem() instanceof ItemBlock;
    }

    private boolean isHoldingInteractableBlock() {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem != null && heldItem.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) heldItem.getItem()).getBlock();
            return block instanceof BlockContainer || block instanceof BlockWorkbench;
        }
        return false;
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
        long minDelay = (1000 / maxCPS);
        long maxDelay = (1000 / minCPS);
        return minDelay + (long) (Math.random() * (maxDelay - minDelay + 1));
    }
}