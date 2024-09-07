package me.calclb.aimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class Pointer {
    private static Vec3 pointToRender = null;
    private static long fadeStartTime = 0;
    private static boolean isFadingIn = false;
    private static final long FADE_IN_DURATION = 200; // 200ms fade in
    private static final long FADE_OUT_DURATION = 100; // 100ms fade out
    private static EntityPlayer targetPlayer = null;
    private static final double MAXDISTSQ = 25f;
    private static final double ANGLE_RAD = Math.toRadians(90f);

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer me = mc.thePlayer;

        if (me == null || mc.theWorld == null) return;

        EntityPlayer target = findClosestAttackablePlayerInRange(me, MAXDISTSQ, ANGLE_RAD, event.partialTicks);
        if (target != null) pointAtTarget(me, target, event.partialTicks);
        else startFadeOut();

        renderPoint(event);

        // Check if we should clear the target after fade out
        if (!isFadingIn && calculateFadeAlpha() == 0) {
            targetPlayer = null;
            pointToRender = null;
        }
    }

    public static Vec3 getNearestPointOnBox(Vec3 origin, Entity entity, double partialTicks) {
        AxisAlignedBB box = entity.getEntityBoundingBox();

        // Interpolate the entity's position
        double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        // Calculate the offset from the entity's interpolated position to its current bounding box
        double offsetX = entityX - entity.posX;
        double offsetY = entityY - entity.posY;
        double offsetZ = entityZ - entity.posZ;

        // Apply the offset to the bounding box
        AxisAlignedBB interpolatedBox = box.offset(offsetX, offsetY, offsetZ);

        // Find the nearest point on the interpolated box
        double x = Math.max(interpolatedBox.minX, Math.min(origin.xCoord, interpolatedBox.maxX));
        double y = Math.max(interpolatedBox.minY, Math.min(origin.yCoord, interpolatedBox.maxY));
        double z = Math.max(interpolatedBox.minZ, Math.min(origin.zCoord, interpolatedBox.maxZ));

        return new Vec3(x, y, z);
    }

    public static EntityPlayer findClosestAttackablePlayerInRange(EntityPlayer me, double maxDistanceSq, double maxAngleRadians, double partialTicks) {
        EntityPlayer closestPlayer = null;
        double closestAngleRadians = Double.MAX_VALUE;

        for (EntityPlayer otherPlayer : me.worldObj.playerEntities) {
            if (otherPlayer == me) continue;
            if(!AntiBot.isValidTarget(otherPlayer, maxAngleRadians)) continue;

            Vec3 lookVec = me.getLook(1.0F);
            Vec3 eyePos = new Vec3(me.posX, me.posY + me.getEyeHeight(), me.posZ);
            Vec3 targetVec = getNearestPointOnBox(eyePos, otherPlayer, partialTicks);

            double dx = targetVec.xCoord - eyePos.xCoord;
            double dy = targetVec.yCoord - eyePos.yCoord;
            double dz = targetVec.zCoord - eyePos.zCoord;

            double distanceSq = dx * dx + dy * dy + dz * dz;
            if (distanceSq > maxDistanceSq) continue;

            Vec3 toTargetVec = new Vec3(dx, dy, dz);
            double lookDotTarget = lookVec.dotProduct(toTargetVec);
            double lookLengthSq = lookVec.lengthVector();

            double angle = Math.acos(lookDotTarget / (Math.sqrt(lookLengthSq * distanceSq)));

            if (angle < closestAngleRadians) {
                closestAngleRadians = angle;
                closestPlayer = otherPlayer;
            }
        }

        return (closestAngleRadians <= maxAngleRadians) ? closestPlayer : null;
    }

    private void startFadeOut() {
        if (isFadingIn || (pointToRender != null && fadeStartTime == 0)) {
            fadeStartTime = System.currentTimeMillis();
            isFadingIn = false;
        }
    }

    /**
     * Set the sphere location to a particular entity. If the entity changes, fade in starts.
     * @param src the camera position
     * @param newTarget the target to set the new point to
     */
    public static void pointAtTarget(EntityPlayer src, EntityPlayer newTarget, double partialTicks) {
        if(newTarget != targetPlayer) {
            fadeStartTime = System.currentTimeMillis();
            isFadingIn = true;
            targetPlayer = newTarget;
        }
        pointToRender = getNearestPointOnBox(src.getPositionEyes(1f), newTarget, partialTicks);
    }

    public static void renderPoint(RenderWorldLastEvent event) {
        if (pointToRender == null) return;

        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-viewerX, -viewerY, -viewerZ);
        GlStateManager.translate(pointToRender.xCoord, pointToRender.yCoord, pointToRender.zCoord);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();

        float radius = 0.2F;
        int slices = 8;
        int stacks = 8;

        float alpha = calculateFadeAlpha();
        GL11.glColor4f(1.0f, 0.0f, 0.0f, alpha); // Red color with fading opacity

        // Draw the sphere
        for (int i = 0; i <= stacks; i++) {
            double lat0 = Math.PI * (-0.5 + (double) (i - 1) / stacks);
            double z0 = Math.sin(lat0);
            double zr0 = Math.cos(lat0);

            double lat1 = Math.PI * (-0.5 + (double) i / stacks);
            double z1 = Math.sin(lat1);
            double zr1 = Math.cos(lat1);

            GL11.glBegin(GL11.GL_QUAD_STRIP);
            for (int j = 0; j <= slices; j++) {
                double lng = 2 * Math.PI * (double) (j - 1) / slices;
                double x = Math.cos(lng);
                double y = Math.sin(lng);

                GL11.glNormal3d(x * zr0, y * zr0, z0);
                GL11.glVertex3d(x * zr0 * radius, y * zr0 * radius, z0 * radius);
                GL11.glNormal3d(x * zr1, y * zr1, z1);
                GL11.glVertex3d(x * zr1 * radius, y * zr1 * radius, z1 * radius);
            }
            GL11.glEnd();
        }

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static Vec3 getVisiblePart(EntityPlayer me, EntityPlayer target) {
        Minecraft mc = Minecraft.getMinecraft();
        AxisAlignedBB boundingBox = target.getEntityBoundingBox();
        Vec3 eyePos = me.getPositionEyes(1.0F);

        // Check corners of the bounding box
        Vec3[] points = new Vec3[] {
                new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ),
                new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ),
                // Center of each face
                new Vec3(boundingBox.minX, (boundingBox.minY + boundingBox.maxY) / 2, (boundingBox.minZ + boundingBox.maxZ) / 2),
                new Vec3(boundingBox.maxX, (boundingBox.minY + boundingBox.maxY) / 2, (boundingBox.minZ + boundingBox.maxZ) / 2),
                new Vec3((boundingBox.minX + boundingBox.maxX) / 2, boundingBox.minY, (boundingBox.minZ + boundingBox.maxZ) / 2),
                new Vec3((boundingBox.minX + boundingBox.maxX) / 2, boundingBox.maxY, (boundingBox.minZ + boundingBox.maxZ) / 2),
                new Vec3((boundingBox.minX + boundingBox.maxX) / 2, (boundingBox.minY + boundingBox.maxY) / 2, boundingBox.minZ),
                new Vec3((boundingBox.minX + boundingBox.maxX) / 2, (boundingBox.minY + boundingBox.maxY) / 2, boundingBox.maxZ)
        };

        for (Vec3 point : points) {
            if (mc.theWorld.rayTraceBlocks(eyePos, point) == null) {
                return point;
            }
        }

        return null;
    }

    private static float calculateFadeAlpha() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - fadeStartTime;

        if (isFadingIn) {
            if (elapsedTime >= FADE_IN_DURATION) return 1f;
            return (float) elapsedTime / FADE_IN_DURATION;
        } else {
            if (elapsedTime >= FADE_OUT_DURATION) return 0f;
            return 1f - ((float) elapsedTime / FADE_OUT_DURATION);
        }
    }
}