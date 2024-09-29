package me.calclb.aimer.render;

import me.calclb.aimer.util.Renderer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class InvisEsp {

    private static final float OPACITY = 0.3F;
    private static final double MAX_DISTANCE = 50.0;

    @SubscribeEvent
    public void onRenderOverlay(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer me = mc.thePlayer;
        float partialTicks = event.partialTicks;

        List<EntityPlayer> invisiblePlayers = new ArrayList<EntityPlayer>();

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if(!player.isEntityAlive()) continue;
            if (player == me) continue;
            if (!player.isInvisible()) continue;

            double distance = me.getDistanceToEntity(player);
            if (distance > MAX_DISTANCE) continue;
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