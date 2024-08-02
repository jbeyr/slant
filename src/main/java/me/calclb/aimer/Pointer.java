package me.calclb.aimer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class Pointer {
    private static Vec3 pointToRender = null;

    public static Vec3 getNearestPointOnEntityHitbox(Entity entity, Vec3 cameraPos) {
        AxisAlignedBB hitbox = entity.getEntityBoundingBox();

        // Clamp the camera position to the nearest point on or within the hitbox
        double nearestX = clamp(cameraPos.xCoord, hitbox.minX, hitbox.maxX);
        double nearestY = clamp(cameraPos.yCoord, hitbox.minY, hitbox.maxY);
        double nearestZ = clamp(cameraPos.zCoord, hitbox.minZ, hitbox.maxZ);

        return new Vec3(nearestX, nearestY, nearestZ);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }


    private static Vec3 intersectRayPlane(Vec3 rayOrigin, Vec3 rayDirection, Vec3 planePoint, Vec3 planeNormal) {
        double denom = rayDirection.dotProduct(planeNormal);
        if (Math.abs(denom) > 1e-6) {
            double t = planePoint.subtract(rayOrigin).dotProduct(planeNormal) / denom;
            if (t >= 0) {
                return rayOrigin.addVector(
                        rayDirection.xCoord * t,
                        rayDirection.yCoord * t,
                        rayDirection.zCoord * t
                );
            }
        }
        return null;
    }

    private static boolean isPointInsideBox(Vec3 point, Vec3 min, Vec3 max) {
        return point.xCoord >= min.xCoord && point.xCoord <= max.xCoord &&
                point.yCoord >= min.yCoord && point.yCoord <= max.yCoord &&
                point.zCoord >= min.zCoord && point.zCoord <= max.zCoord;
    }

    public static void setPointToRender(Vec3 point) {
        pointToRender = point;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (pointToRender != null) {
            renderPoint(event);
        }
    }

    private void renderPoint(RenderWorldLastEvent event) {
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(-viewerX, -viewerY, -viewerZ);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(3.0F);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor4f(1.0F, 0.0F, 0.0F, 1.0F);

        float size = 0.1F;
        GL11.glVertex3d(pointToRender.xCoord - size, pointToRender.yCoord, pointToRender.zCoord);
        GL11.glVertex3d(pointToRender.xCoord + size, pointToRender.yCoord, pointToRender.zCoord);
        GL11.glVertex3d(pointToRender.xCoord, pointToRender.yCoord - size, pointToRender.zCoord);
        GL11.glVertex3d(pointToRender.xCoord, pointToRender.yCoord + size, pointToRender.zCoord);
        GL11.glVertex3d(pointToRender.xCoord, pointToRender.yCoord, pointToRender.zCoord - size);
        GL11.glVertex3d(pointToRender.xCoord, pointToRender.yCoord, pointToRender.zCoord + size);

        GL11.glEnd();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
}