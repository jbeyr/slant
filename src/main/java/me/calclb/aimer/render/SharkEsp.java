package me.calclb.aimer.render;

import me.calclb.aimer.Reporter;
import me.calclb.aimer.util.Renderer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class SharkEsp {

    private static final float MAX_OPACITY = 0.9F;
    private static final float MIN_OPACITY = 0.1F;
    private static final float CRITICAL_HEALTH = 0.15f; // 15% health
    private static float lowHealthThreshold = 0.7f;
    private static float activationRadiusSqr = 50 * 50;
    private static boolean enabled;

    public static void setActivationRadius(float radius) {
        activationRadiusSqr = radius * radius;
        Reporter.reportSet("Shark ESP", "Activation Radius", radius);
    }

    public static float getLowHealthThreshold() {
        return lowHealthThreshold;
    }

    public static void setLowHealthThreshold(float ratio) {
        lowHealthThreshold = ratio;
        Reporter.reportSet("Shark ESP", "Low Health Threshold", ratio);
    }

    public static float getActivationRadiusSqr() {
        return activationRadiusSqr;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        Reporter.reportToggled("Shark ESP", b);
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

        Renderer.setupRendering();

        for (EntityPlayer player : nearbyPlayers) {
            if (!player.isEntityAlive()) continue;

            float healthRatio = player.getHealth() / player.getMaxHealth();
            if (healthRatio > lowHealthThreshold) continue;

            float[] color = calculateColor(healthRatio);
            float opacity = calculateOpacity(healthRatio);
            Renderer.drawEntityESP(player, partialTicks, color[0], color[1], color[2], opacity);
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
            float normalizedHealth = (healthRatio - CRITICAL_HEALTH) / (lowHealthThreshold - CRITICAL_HEALTH);
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
            normalizedHealth = (healthRatio - CRITICAL_HEALTH) / (lowHealthThreshold - CRITICAL_HEALTH);
        }
        float opacityFactor = 1 - (float) Math.pow(normalizedHealth, 2); // Quadratic function for steeper curve
        return MIN_OPACITY + (MAX_OPACITY - MIN_OPACITY) * opacityFactor;
    }
}