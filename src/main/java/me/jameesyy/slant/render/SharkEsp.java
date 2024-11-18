package me.jameesyy.slant.render;

import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.Renderer;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class SharkEsp {

    private static final float MAX_OPACITY = 0.9F;
    private static final float MIN_OPACITY = 0.1F;
    private static final float CRITICAL_HEALTH = 0.15f;
    private static float lowHealthThreshold = 0.7f;
    private static float activationRadiusSqr = 50 * 50;
    private static boolean enabled;
    private static boolean render3dHitboxes;

    public static void setActivationRadius(float radius) {
        activationRadiusSqr = radius * radius;
        ModConfig.sharkEspActivationRadius = radius;
        Reporter.queueSetMsg("Shark ESP", "Activation Radius", radius);
    }

    public static float getLowHealthThreshold() {
        return lowHealthThreshold;
    }

    public static void setLowHealthThreshold(float ratio) {
        lowHealthThreshold = ratio;
        ModConfig.sharkEspLowHealthThreshold = ratio;
        Reporter.queueSetMsg("Shark ESP", "Low Health Threshold", ratio);
    }

    public static float getActivationRadiusSqr() {
        return activationRadiusSqr;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.sharkEspEnabled = b;
        Reporter.queueReportMsg("Shark ESP", b);
    }

    public static boolean isRender3dHitboxes() {
        return render3dHitboxes;
    }

    public static void setRender3dHitboxes(boolean b) {
        render3dHitboxes = b;
        ModConfig.sharkEspRender3dHitboxes = b;
        Reporter.queueReportMsg("Shark ESP 3D Hitboxes", b);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderWorldLastEvent event) {
        if (!enabled) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer me = mc.thePlayer;
        float partialTicks = event.partialTicks;

        List<EntityPlayer> nearbyPlayers = new ArrayList<EntityPlayer>();

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == me) continue;

            double distanceSqr = me.getDistanceSqToEntity(player);
            if (distanceSqr > activationRadiusSqr) continue;
            nearbyPlayers.add(player);
        }

        if (nearbyPlayers.isEmpty()) return;

        for (EntityPlayer player : nearbyPlayers) {
            if (!player.isEntityAlive()) continue;

            float healthRatio = player.getHealth() / player.getMaxHealth();
            if (healthRatio > lowHealthThreshold) continue;

            float[] color = calculateColor(healthRatio);
            float opacity = calculateOpacity(healthRatio);

            if (render3dHitboxes) {
                Renderer.draw3dEntityESP(player, partialTicks, color[0], color[1], color[2], opacity);
            } else {
//                Renderer.draw2dEntityEsp(player, partialTicks, color[0], color[1], color[2], opacity);
            }
        }
    }

    private float[] calculateColor(float healthRatio) {
        float red = 1.0f;
        float green, blue;

        if (healthRatio <= CRITICAL_HEALTH) {
            green = 0.0f;
            blue = 0.0f;
        } else { // interpolate between yellow and red for health above critical threshold
            float normalizedHealth = (healthRatio - CRITICAL_HEALTH) / (lowHealthThreshold - CRITICAL_HEALTH);
            green = normalizedHealth * normalizedHealth; // quadratic fn - steeper color dropoff
            blue = 0.0f;
        }

        return new float[]{red, green, blue};
    }

    private float calculateOpacity(float healthRatio) {
        float normalizedHealth;
        if (healthRatio <= CRITICAL_HEALTH) {
            normalizedHealth = 0;
        } else {
            normalizedHealth = (healthRatio - CRITICAL_HEALTH) / (lowHealthThreshold - CRITICAL_HEALTH);
        }
        float opacityFactor = 1 - (float) Math.pow(normalizedHealth, 2); // quadratic fn - steeper color dropoff
        return MIN_OPACITY + (MAX_OPACITY - MIN_OPACITY) * opacityFactor;
    }
}