package me.jameesyy.slant.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import static me.jameesyy.slant.network.PacketManager.mc;
import static me.jameesyy.slant.util.Renderer.drawBbox3d;

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
    public static void drawTrueBacktrackHitbox(Vec3 lastPos, Vec3 nowPos, float partialTicksForTargetPos, float partialTicksForPlayerPos, float red, float green, float blue, float opacity) {
        // TODO use a kb physics simulation, or better yet, just process inbound packets on a fake entity and throttle them for the real entity
        EntityPlayer me = Minecraft.getMinecraft().thePlayer;
        Vec3 targetLerpPos = Renderer.interpolatedPos(nowPos, lastPos, partialTicksForTargetPos);
        Vec3 myLerpPos = Renderer.interpolatedPos(me, partialTicksForPlayerPos);
        Vec3 dv = targetLerpPos.subtract(myLerpPos);
        drawBbox3d(dv, red, green, blue, opacity, .5f, true);
    }
}
