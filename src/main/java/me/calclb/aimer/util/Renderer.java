package me.calclb.aimer.util;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public class Renderer {
    public static int scaleZeroOneRangeTo255(float f) {
        return (int)(f * 255);
    }

    public static void setupRendering() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableCull();
    }

    public static void resetRendering() {
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
    }

    public static <T extends Entity> void drawEntityESP(RenderManager rm, T en, PosTracker<T> tracker, float partialTicks, float red, float green, float blue, float opacity) {
        GlStateManager.pushMatrix();

        // Simplify the translation to just move to the render position
        double x = tracker.getInterpolatedX(en, partialTicks) - rm.viewerPosX;
        double y = tracker.getInterpolatedY(en, partialTicks) - rm.viewerPosY;
        double z = tracker.getInterpolatedZ(en, partialTicks) - rm.viewerPosZ;

        GlStateManager.translate(x, y, z);

        double halfWidth = .4;
        double height = 1.9;

        GlStateManager.color(red, green, blue, opacity * 0.25f);
        drawFilledBox(-halfWidth, 0, -halfWidth, halfWidth, height, halfWidth);

        GlStateManager.color(red, green, blue, opacity);
        drawOutlineBox(-halfWidth, 0, -halfWidth, halfWidth, height, halfWidth);

        GlStateManager.popMatrix();
    }

    private static void drawFilledBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        GL11.glBegin(GL11.GL_QUADS);
        // Front
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(maxX, minY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(minX, maxY, minZ);

        // Back
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(maxX, minY, maxZ);

        // Bottom
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(maxX, minY, minZ);

        // Top
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(minX, maxY, maxZ);

        // Left
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glVertex3d(minX, minY, maxZ);

        // Right
        GL11.glVertex3d(maxX, minY, minZ);
        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glEnd();
    }

    private static void drawOutlineBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(maxX, minY, minZ);
        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(maxX, minY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glEnd();
    }

    public static void drawBoundingBox(AxisAlignedBB bbox, int red, int green, int blue, int alpha) {
        GlStateManager.color(red, green, blue, alpha);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bbox.minX, bbox.minY, bbox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.maxX, bbox.minY, bbox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.maxX, bbox.minY, bbox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.minX, bbox.minY, bbox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.minX, bbox.minY, bbox.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bbox.minX, bbox.maxY, bbox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.maxX, bbox.maxY, bbox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.maxX, bbox.maxY, bbox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.minX, bbox.maxY, bbox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.minX, bbox.maxY, bbox.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bbox.minX, bbox.minY, bbox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.minX, bbox.maxY, bbox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.maxX, bbox.minY, bbox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.maxX, bbox.maxY, bbox.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.maxX, bbox.minY, bbox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.maxX, bbox.maxY, bbox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.minX, bbox.minY, bbox.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bbox.minX, bbox.maxY, bbox.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }
}