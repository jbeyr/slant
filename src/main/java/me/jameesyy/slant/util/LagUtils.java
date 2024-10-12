package me.jameesyy.slant.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static me.jameesyy.slant.network.PacketManager.mc;
import static me.jameesyy.slant.util.Renderer.*;

public class LagUtils {

    public static double getDistanceToAxis(AxisAlignedBB aabb) {
        float f = (float) (Minecraft.getMinecraft().thePlayer.posX - (aabb.minX + aabb.maxX) / 2.0);
        float f1 = (float) (Minecraft.getMinecraft().thePlayer.posY - (aabb.minY + aabb.maxY) / 2.0);
        float f2 = (float) (Minecraft.getMinecraft().thePlayer.posZ - (aabb.minZ + aabb.maxZ) / 2.0);
        return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2);
    }

    public static boolean isIngame() {
        return mc.theWorld != null && mc.thePlayer != null;
    }

    /**
     * Draws the real position of the hitbox where the other entity is.
     */
    public static void drawTrueBacktrackHitbox(AxisAlignedBB lastPos, AxisAlignedBB nowPos, float partialTicks, float red, float green, float blue, float opacity) {
        EntityPlayer me = Minecraft.getMinecraft().thePlayer;
        double playerX = me.lastTickPosX + (me.posX - me.lastTickPosX) * partialTicks;
        double playerY = me.lastTickPosY + (me.posY - me.lastTickPosY) * partialTicks;
        double playerZ = me.lastTickPosZ + (me.posZ - me.lastTickPosZ) * partialTicks;

        // Calculate the center coordinates of both bounding boxes
        double lastCenterX = (lastPos.minX + lastPos.maxX) / 2;
        double lastCenterZ = (lastPos.minZ + lastPos.maxZ) / 2;

        double nowCenterX = (nowPos.minX + nowPos.maxX) / 2;
        double nowCenterZ = (nowPos.minZ + nowPos.maxZ) / 2;

        // Interpolate between the centers
        double ix = lastCenterX + (nowCenterX - lastCenterX);
        double iy = lastPos.minY + (nowPos.minY - lastPos.minY);
        double iz = lastCenterZ + (nowCenterZ - lastCenterZ);

        Vec3 dv = new Vec3(ix - playerX, iy - playerY, iz - playerZ);

        GlStateManager.pushMatrix();
        GlStateManager.translate(dv.xCoord, dv.yCoord, dv.zCoord);

        double halfWidth = .4;
        double height = 1.9;

        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        GlStateManager.color(red, green, blue, opacity * 0.5f);
        drawFilledBox(-halfWidth, 0, -halfWidth, halfWidth, height, halfWidth);

        GlStateManager.color(red, green, blue, opacity);
        drawOutlineBox(-halfWidth, 0, -halfWidth, halfWidth, height, halfWidth);

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }
}
