package me.jameesyy.slant.render;

import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ChestEsp {

    private static boolean enabled;
    private static int radius;
    private static Color color;

    public static void setEnabled(boolean b) {
        ChestEsp.enabled = b;
        ModConfig.chestEspEnabled = b;
        Reporter.queueReportMsg("Chest ESP", b);
    }

    public static void setBboxColor(Color color) {
        ChestEsp.color = color;
        ModConfig.chestEspBboxColor = color;
        Reporter.queueSetMsg("Chest ESP", "Bounding Box Color", color);
    }

    public static void setRadius(int r) {
        ChestEsp.radius = r;
        ModConfig.chestEspRadius = r;
        Reporter.queueSetMsg("Chest ESP", "Radius", r);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if(!enabled) return;
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null || mc.thePlayer == null) return;

        for (TileEntityChest chest : mc.theWorld.loadedTileEntityList.stream().filter(te -> te instanceof TileEntityChest)
                .map(te -> (TileEntityChest) te)
                .filter(chest -> mc.thePlayer.getDistanceSq(chest.getPos()) <= radius * radius)
                .collect(java.util.stream.Collectors.toList())) {
            drawESPBox(chest, event.partialTicks);
        }
    }

    private void drawESPBox(TileEntityChest chest, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Minecraft mc = Minecraft.getMinecraft();
        double x = chest.getPos().getX() - mc.getRenderManager().viewerPosX;
        double y = chest.getPos().getY() - mc.getRenderManager().viewerPosY;
        double z = chest.getPos().getZ() - mc.getRenderManager().viewerPosZ;

        AxisAlignedBB box = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
        RenderGlobal.drawOutlinedBoundingBox(box, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}