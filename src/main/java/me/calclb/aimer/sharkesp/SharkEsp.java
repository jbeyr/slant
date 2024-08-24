package me.calclb.aimer.sharkesp;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

@Mod(modid = "sharkesp", version = "1.0")
public class SharkEsp {

    private static final float MAX_OPACITY = 0.9F;
    private static final float MIN_OPACITY = 0.1F;
    private static final float HEALTH_THRESHOLD = 0.7f;
    private static final float CRITICAL_HEALTH = 0.15f; // 15% health
    private static final double MAX_DISTANCE = 50.0;

    private final Map<EntityPlayer, PosTracker<EntityPlayer>> posTrackers = new HashMap<EntityPlayer, PosTracker<EntityPlayer>>();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer me = mc.thePlayer;
        float partialTicks = event.partialTicks;

        List<EntityPlayer> nearbyPlayers = new ArrayList<EntityPlayer>();

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == me) continue;

            double distance = me.getDistanceToEntity(player);
            if (distance > MAX_DISTANCE) continue;
            nearbyPlayers.add(player);

            PosTracker<EntityPlayer> pt = posTrackers.get(player);
            if(pt == null) {
                pt = new PosTracker<EntityPlayer>(player);
                posTrackers.put(player, pt);
            }
            pt.updatePosTo(player);
        }

        if (nearbyPlayers.isEmpty()) return;

        Renderer.setupRendering();

        for (EntityPlayer player : nearbyPlayers) {
            PosTracker<EntityPlayer> pTracker = posTrackers.get(player);
            if (pTracker == null) continue;

            float healthRatio = player.getHealth() / player.getMaxHealth();
            if (healthRatio > HEALTH_THRESHOLD) continue;

            float[] color = calculateColor(healthRatio);
            float opacity = calculateOpacity(healthRatio);
            System.out.println("calling render hbox method..");
            Renderer.drawEntityESP(mc.getRenderManager(), player, pTracker, partialTicks, color[0], color[1], color[2], opacity);
        }

        Renderer.resetRendering();
    }

    private float[] calculateColor(float healthRatio) {
        float red = 1.0f;
        float green, blue;

        if (healthRatio <= CRITICAL_HEALTH) {
            // Very red for critical health
            green = 0.0f;
            blue = 0.0f;
        } else {
            // Interpolate between yellow and red for health above critical
            float normalizedHealth = (healthRatio - CRITICAL_HEALTH) / (HEALTH_THRESHOLD - CRITICAL_HEALTH);
            green = normalizedHealth * normalizedHealth; // Quadratic falloff for smoother transition
            blue = 0.0f;
        }

        return new float[]{red, green, blue};
    }

    private float calculateOpacity(float healthRatio) {
        float normalizedHealth;
        if (healthRatio <= CRITICAL_HEALTH) {
            normalizedHealth = 0; // Full opacity for critical health
        } else {
            normalizedHealth = (healthRatio - CRITICAL_HEALTH) / (HEALTH_THRESHOLD - CRITICAL_HEALTH);
        }
        float opacityFactor = 1 - (float) Math.pow(normalizedHealth, 2); // Quadratic function for steeper curve
        return MIN_OPACITY + (MAX_OPACITY - MIN_OPACITY) * opacityFactor;
    }
}