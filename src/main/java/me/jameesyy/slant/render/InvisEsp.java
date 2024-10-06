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

public class InvisEsp {

    private static final float OPACITY = 0.3F;
    private static float activationRadiusSqr = 50 * 50;
    private static boolean enabled;

    public static void setActivationRadius(float radius) {
        activationRadiusSqr = radius * radius;
        ModConfig.invisEspActivationRadius = radius;
        Reporter.reportSet("Invis ESP", "Activation Radius", radius);
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.invisEspEnabled = b;
        Reporter.reportToggled("Invis ESP", b);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static float getActivationRadiusSqr() {
        return activationRadiusSqr;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderWorldLastEvent event) {
        if(!enabled) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer me = mc.thePlayer;
        float partialTicks = event.partialTicks;

        List<EntityPlayer> invisiblePlayers = new ArrayList<EntityPlayer>();

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if(!player.isEntityAlive()) continue;
            if (player == me) continue;
            if (!player.isInvisible()) continue;

            double distanceSqr = me.getDistanceSqToEntity(player);
            if (distanceSqr > activationRadiusSqr) continue;
            invisiblePlayers.add(player);
        }

        if (invisiblePlayers.isEmpty()) return;

        Renderer.setupRendering();

        for (EntityPlayer player : invisiblePlayers) {
            Renderer.drawEntityESP(player, partialTicks, 1.0f, 1.0f, 1.0f, OPACITY);
        }

        Renderer.resetRendering();
    }
}