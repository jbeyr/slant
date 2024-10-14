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

        // Calculate the center coordinates of both bounding boxes
        double lastCenterX = (lastPos.minX + lastPos.maxX) / 2;
        double lastCenterZ = (lastPos.minZ + lastPos.maxZ) / 2;
        Vec3 lastTargetPos = new Vec3(lastCenterX, lastPos.minY, lastCenterZ);

        Vec3 myLerpPos = Renderer.interpolatedPos(me, partialTicks);
        Vec3 dv = lastTargetPos.subtract(myLerpPos);

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
}
