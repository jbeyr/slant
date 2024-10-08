package me.jameesyy.slant.combat;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.Main;
import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.input.Mouse;

import javax.swing.*;

public class RightAutoclicker {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static long lastClickTime = 0;
    private static long clickDelay = 0;

    private static boolean enabled;
    private static int minCPS;
    private static int maxCPS;

    public static int getMinCPS() {
        return minCPS;
    }

    public static void setMinCPS(int cps) {
        RightAutoclicker.minCPS = cps;
        ModConfig.rightAutoClickerMinCps = cps;
        Reporter.reportSet("RMB Autoclicker", "Min CPS", cps);
    }

    public static int getMaxCPS() {
        return maxCPS;
    }

    public static void setMaxCPS(int cps) {
        RightAutoclicker.maxCPS = cps;
        ModConfig.rightAutoClickerMaxCps = cps;
        Reporter.reportSet("RMB Autoclicker", "Max CPS", cps);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.rightAutoclickerEnabled = b;
        Reporter.reportToggled("RMB Autoclicker", b);
    }

    public static void legitRightClick() {
        int key = mc.gameSettings.keyBindUseItem.getKeyCode();

        // for releasing the key
        Timer timer = new Timer((int) (clickDelay / 2), (actionevent) -> {
            KeyBinding.setKeyBindState(key, false);
        });

        KeyBinding.setKeyBindState(key, true);
        KeyBinding.onTick(key);

        timer.setRepeats(false);
        timer.start();
    }

    public static boolean shouldClick() {
        return enabled
                && ActionConflictResolver.isClickAllowed()
                && Mouse.isButtonDown(1)
                && hasCooldownExpired()
                && isHoldingPlaceableBlock()
                && !isHoldingInteractableBlock();
    }

    public static void resetClickDelay() {
        lastClickTime = System.currentTimeMillis();
        clickDelay = getRandomClickDelay();
    }

    private static boolean hasCooldownExpired() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastClickTime >= clickDelay;
    }

    private static boolean isHoldingPlaceableBlock() {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        return heldItem != null && heldItem.getItem() instanceof ItemBlock;
    }

    private static boolean isHoldingInteractableBlock() {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem != null && heldItem.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) heldItem.getItem()).getBlock();
            return block instanceof BlockContainer || block instanceof BlockWorkbench;
        }
        return false;
    }

    private static long getRandomClickDelay() {
        long minDelay = (1000 / maxCPS);
        long maxDelay = (1000 / minCPS);
        return minDelay + (long) (Math.random() * (maxDelay - minDelay + 1));
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (Main.getRightAutoclickKey().isPressed()) setEnabled(!enabled);

        if (shouldClick()) legitRightClick();
    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        if (event.button == 1 && event.buttonstate) {  // 1 is right mouse button; buttonstate is true if pressed
            resetClickDelay();
        }
    }
}