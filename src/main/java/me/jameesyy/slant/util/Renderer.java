package me.jameesyy.slant.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
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

    public static Vec3 interpolatedPos(Entity en, double partialTicks) {
        double ix = en.lastTickPosX + (en.posX - en.lastTickPosX) * partialTicks;
        double iy = en.lastTickPosY + (en.posY - en.lastTickPosY) * partialTicks;
        double iz = en.lastTickPosZ + (en.posZ - en.lastTickPosZ) * partialTicks;
        return new Vec3(ix, iy, iz);
    }

    public static Vec3 interpolatedDifferenceFromEntities(Entity src, Entity dst, double partialTicks) {
        Vec3 sv = interpolatedPos(src, partialTicks);
        Vec3 ov = interpolatedPos(dst, partialTicks);
        return ov.subtract(sv);
    }

    public static Vec3 interpolatedDifferenceFromMe(Entity dst, double partialTicks) {
        EntityPlayer me = Minecraft.getMinecraft().thePlayer;
        return interpolatedDifferenceFromEntities(me, dst, partialTicks);
    }

    public static <T extends Entity> void drawEntityESP(T en, float partialTicks, float red, float green, float blue, float opacity) {

        Vec3 dv = interpolatedDifferenceFromMe(en, partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(dv.xCoord, dv.yCoord, dv.zCoord);

        double halfWidth = .4;
        double height = 1.9;

        GlStateManager.color(red, green, blue, opacity * 0.25f);
        drawFilledBox(-halfWidth, 0, -halfWidth, halfWidth, height, halfWidth);

        GlStateManager.color(red, green, blue, opacity);
        drawOutlineBox(-halfWidth, 0, -halfWidth, halfWidth, height, halfWidth);

        GlStateManager.popMatrix();
    }

    public static <T extends Entity> void drawEntityESP(T en, float partialTicks, float red, float green, float blue, float opacity, float fillBoxMultiplier) {

        Vec3 dv = interpolatedDifferenceFromMe(en, partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(dv.xCoord, dv.yCoord, dv.zCoord);

        double halfWidth = .4;
        double height = 1.9;

        GlStateManager.color(red, green, blue, opacity * fillBoxMultiplier);
        drawFilledBox(-halfWidth, 0, -halfWidth, halfWidth, height, halfWidth);

        GlStateManager.color(red, green, blue, opacity);
        drawOutlineBox(-halfWidth, 0, -halfWidth, halfWidth, height, halfWidth);

        GlStateManager.popMatrix();
    }

    private static void drawFilledBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        GL11.glBegin(GL11.GL_QUADS);
        // front
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(maxX, minY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(minX, maxY, minZ);

        // back
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(maxX, minY, maxZ);

        // bottom
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(maxX, minY, minZ);

        // top
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(minX, maxY, maxZ);

        // left
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glVertex3d(minX, minY, maxZ);

        // right
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
}