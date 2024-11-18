package me.jameesyy.slant.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Renderer {
    public static int scaleZeroOneRangeTo255(float f) {
        return (int)(f * 255);
    }

    private static void setupRendering() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableCull();
    }

    private static void resetRendering() {
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

    public static Vec3 interpolatedPos(Vec3 now, Vec3 prev, double partialTicks) {
        double ix = prev.xCoord + (now.xCoord - prev.xCoord) * partialTicks;
        double iy = prev.yCoord + (now.yCoord - prev.yCoord) * partialTicks;
        double iz = prev.zCoord + (now.zCoord - prev.zCoord) * partialTicks;
        return new Vec3(ix, iy, iz);
    }

    public static Vec3 interpolatedDifferenceFromEntities(Entity src, Entity dst, double partialTicks) {
        Vec3 sv = interpolatedPos(src, partialTicks);
        Vec3 ov = interpolatedPos(dst, partialTicks);
        return ov.subtract(sv);
    }

    public static Vec3 interpolatedDifferenceFromMeAndVector(Vec3 dst, double partialTicks) {
        EntityPlayer me = Minecraft.getMinecraft().thePlayer;
        return interpolatedPos(me.getPositionVector(), dst, partialTicks);
    }

    public static Vec3 interpolatedDifferenceFromMeAndEntity(Entity dst, double partialTicks) {
        EntityPlayer me = Minecraft.getMinecraft().thePlayer;
        return interpolatedDifferenceFromEntities(me, dst, partialTicks);
    }

    public static <T extends Entity> void draw3dEntityESP(T en, float partialTicks, float red, float green, float blue, float opacity) {
        Vec3 dv = interpolatedDifferenceFromMeAndEntity(en, partialTicks);
        drawBbox3d(dv, red, green, blue, opacity, .25f, false);
    }

    /**
     * Pushes a matrix in GlStateManager with the drawn hitbox, renders it, then pops the matrix. Calls {@see Renderer.setupRendering} and {@see Renderer.resetRendering} internally.
     * @param v The position vector of the hitbox
     * @param red Red color component (0-1)
     * @param green Green color component (0-1)
     * @param blue Blue color component (0-1)
     * @param opacity Overall opacity of the hitbox (0-1)
     * @param filledBboxOpacityMultiplier Opacity multiplier for the filled part of the hitbox
     * @param respectDepth If true, the hitbox will be drawn behind objects closer to the camera
     */
    public static void drawBbox3d(Vec3 v, float red, float green, float blue, float opacity, float filledBboxOpacityMultiplier, boolean respectDepth) {
        setupRendering();

        if (respectDepth) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        } else {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(v.xCoord, v.yCoord, v.zCoord);

        double halfWidth = .4;
        double height = 1.9;

        // Draw filled box
        GlStateManager.color(red, green, blue, opacity * filledBboxOpacityMultiplier);
        draw3dFilledBox(-halfWidth, 0, -halfWidth, halfWidth, height, halfWidth);

        // Draw outline
        if (respectDepth) {
            // For the outline, we want it to always be visible, so we disable depth writing
            GlStateManager.depthMask(false);
        }
        GlStateManager.color(red, green, blue, opacity);
        draw3dOutlineBox(-halfWidth, 0, -halfWidth, halfWidth, height, halfWidth);

        GlStateManager.popMatrix();

        // Reset depth settings
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);

        resetRendering();
    }

    static void draw3dFilledBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
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

    static void draw3dOutlineBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
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

    public static <T extends Entity> void draw2dEntityEsp(T en, float partialTicks, float red, float green, float blue, float opacity) {
        Minecraft mc = Minecraft.getMinecraft();

        float x = (float) (en.lastTickPosX + (en.posX - en.lastTickPosX) * partialTicks);
        float y = (float) (en.lastTickPosY + (en.posY - en.lastTickPosY) * partialTicks);
        float z = (float) (en.lastTickPosZ + (en.posZ - en.lastTickPosZ) * partialTicks);

        AxisAlignedBB bbox = en.getEntityBoundingBox().offset(
                x - en.posX,
                y - en.posY,
                z - en.posZ
        );

        x -= (float) mc.getRenderManager().viewerPosX;
        y -= (float) mc.getRenderManager().viewerPosY;
        z -= (float) mc.getRenderManager().viewerPosZ;

        float[] points = {
                (float) bbox.minX, (float) bbox.minY, (float) bbox.minZ,
                (float) bbox.maxX, (float) bbox.minY, (float) bbox.minZ,
                (float) bbox.maxX, (float) bbox.maxY, (float) bbox.minZ,
                (float) bbox.minX, (float) bbox.maxY, (float) bbox.minZ,
                (float) bbox.minX, (float) bbox.minY, (float) bbox.maxZ,
                (float) bbox.maxX, (float) bbox.minY, (float) bbox.maxZ,
                (float) bbox.maxX, (float) bbox.maxY, (float) bbox.maxZ,
                (float) bbox.minX, (float) bbox.maxY, (float) bbox.maxZ
        };

        ScaledResolution scaledResolution = new ScaledResolution(mc);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, green, blue, opacity);
        GL11.glLineWidth(2.0F);

        FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
        FloatBuffer projection = BufferUtils.createFloatBuffer(16);
        IntBuffer viewport = BufferUtils.createIntBuffer(16);
        FloatBuffer winPos = BufferUtils.createFloatBuffer(3);

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < points.length; i += 3) {
            if (GLU.gluProject(x + points[i], y + points[i + 1], z + points[i + 2],
                    modelView, projection, viewport, winPos)) {
                float winX = winPos.get(0);
                float winY = scaledResolution.getScaledHeight() - winPos.get(1);
                GL11.glVertex2f(winX, winY);
            }
        }
        GL11.glEnd();

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}