package me.jameesyy.slant.util;

import me.jameesyy.slant.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFishingRod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Method;

public class RodRecast {

    private static boolean enabled;
    private boolean shouldRecast = false;
    private Method rightClickMouseMethod;
    private int ticksToWait = 0;
    private boolean wasRightClickDown = false;
    private boolean rodWasCast = false;

    public RodRecast() {
        try {
            rightClickMouseMethod = Minecraft.class.getDeclaredMethod("func_147121_ag"); // Minecraft#rightClickMouse()
            rightClickMouseMethod.setAccessible(true);
            System.out.println("RodRecast: Set rclick() to accessible");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("RodRecast: Initialized and registered");
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.rodRecastEnabled = b;
        Reporter.queueReportMsg("Rod Recast", b);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!enabled || event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        if (player == null) return;

        boolean isRightClickDown = Mouse.isButtonDown(1);
        boolean isHoldingRod = player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemFishingRod;

        if (isRightClickDown && !wasRightClickDown && isHoldingRod) {
            if (!rodWasCast) {
                // wasn't cast before, so this is the initial cast
                rodWasCast = true;
                System.out.println("RodRecast: Rod cast");
            } else {
                // already cast, so reel in
                shouldRecast = true;
                ticksToWait = 5; // Wait for 2 ticks before recasting
                System.out.println("RodRecast: Rod reeled in, will recast in 2 ticks");
            }
        }

        if (shouldRecast) {
            if (ticksToWait > 0) {
                ticksToWait--;
                return;
            }

            if (isHoldingRod) {
                try {
                    rightClickMouseMethod.invoke(mc);
                    System.out.println("RodRecast: Recasted rod");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("RodRecast: Player is not holding a fishing rod");
            }
            shouldRecast = false;
        }

        wasRightClickDown = isRightClickDown;
    }
}