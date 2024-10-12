package me.jameesyy.slant.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static me.jameesyy.slant.network.PacketManager.mc;

public class LagUtils {
    public static EntityPlayer getTarget(double range, EntityPlayer oldtarget) {
        List<EntityPlayer> targets = mc.theWorld.playerEntities;
        targets = targets.stream().filter(entity -> mc.thePlayer.getDistanceToEntity(entity) <= range && entity != mc.thePlayer && !entity.isDead && !entity.isSpectator() && !entity.isPlayerSleeping() && !entity.isInvisible()).collect(Collectors.toList());

        targets.sort(Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceToEntity(entity)));

        EntityPlayer newtarget = null;
        if (!targets.isEmpty()) newtarget = targets.get(0);

        if (oldtarget == null) return newtarget;
        else if (newtarget != null) {
            if (mc.thePlayer.getDistanceToEntity(newtarget) <= mc.thePlayer.getDistanceToEntity(oldtarget) - 2 || (oldtarget.isPlayerSleeping() || oldtarget.isDead || oldtarget.isSpectator() || oldtarget.isInvisible())) {
                return newtarget;
            }
        }

        return oldtarget;
    }

    public static double getDistanceToAxis(AxisAlignedBB aabb) {
        float f = (float) (Minecraft.getMinecraft().thePlayer.posX - (aabb.minX + aabb.maxX) / 2.0);
        float f1 = (float) (Minecraft.getMinecraft().thePlayer.posY - (aabb.minY + aabb.maxY) / 2.0);
        float f2 = (float) (Minecraft.getMinecraft().thePlayer.posZ - (aabb.minZ + aabb.maxZ) / 2.0);
        return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2);

    }

    public static boolean isIngame() {
        return mc.theWorld != null && mc.thePlayer != null;
    }

    public static void reAxis(EntityPlayer player, AxisAlignedBB bp, int color, boolean shade, float partialTicks) {
        if (bp != null) {
            float a = (float) ((color >> 24) & 255) / 255.0F;
            float r = (float) ((color >> 16) & 255) / 255.0F;
            float g = (float) ((color >> 8) & 255) / 255.0F;
            float b = (float) (color & 255) / 255.0F;
            if (shade) {
                double x = bp.minX - mc.getRenderManager().viewerPosX;
                double y = bp.minY - mc.getRenderManager().viewerPosY;
                double z = bp.minZ - mc.getRenderManager().viewerPosZ;
                double x2 = bp.maxX - mc.getRenderManager().viewerPosX;
                double y2 = bp.maxY - mc.getRenderManager().viewerPosY;
                double z2 = bp.maxZ - mc.getRenderManager().viewerPosZ;
                GL11.glBlendFunc(770, 771);
                GL11.glEnable(3042);
                GL11.glLineWidth(2.0F);
                GL11.glDisable(3553);
                GL11.glDisable(2929);
                GL11.glDepthMask(false);
                GL11.glColor4d(r, g, b, a);
                drawBoundingBox(new AxisAlignedBB(x, y, z, x2, y2, z2), r, g, b);
                GL11.glEnable(3553);
                GL11.glEnable(2929);
                GL11.glDepthMask(true);
                GL11.glDisable(3042);
            } else {
                AxisAlignedBB bbox = bp;
                double x = -mc.getRenderManager().viewerPosX;
                double y = -mc.getRenderManager().viewerPosY;
                double z = -mc.getRenderManager().viewerPosZ;

                AxisAlignedBB axis = new AxisAlignedBB(
                        bbox.minX + x,
                        bbox.minY + y,
                        bbox.minZ + z,
                        bbox.maxX + x,
                        bbox.maxY + y,
                        bbox.maxZ + z
                );

                GL11.glBlendFunc(770, 771);
                GL11.glEnable(3042);
                GL11.glDisable(3553);
                GL11.glDisable(2929);
                GL11.glDepthMask(false);
                GL11.glLineWidth(2.0F);
                GL11.glColor4f(r, g, b, a);
                RenderGlobal.drawSelectionBoundingBox(axis);
                GL11.glEnable(3553);
                GL11.glEnable(2929);
                GL11.glDepthMask(true);
                GL11.glDisable(3042);
            }
        }
    }

    public static void drawBoundingBox(AxisAlignedBB abb, float r, float g, float b) {
        float a = 0.25F;
        Tessellator ts = Tessellator.getInstance();
        WorldRenderer vb = ts.getWorldRenderer();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        ts.draw();
        vb.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vb.pos(abb.minX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.minX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.minZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.maxY, abb.maxZ).color(r, g, b, a).endVertex();
        vb.pos(abb.maxX, abb.minY, abb.maxZ).color(r, g, b, a).endVertex();
        ts.draw();
    }
}
