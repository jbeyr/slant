package me.calclb.aimer.render;

import me.calclb.aimer.Reporter;
import me.calclb.aimer.util.AntiBot;
import me.calclb.aimer.util.Renderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class Pointer {
    private static Vec3 pointToRender = null;
    private static EntityPlayer targetPlayer = null;
    private static long fadeStartTime = 0;
    private static boolean isFadingIn = false;
    private static final long FADE_IN_DURATION = 200; // 200ms fade in
    private static final long FADE_OUT_DURATION = 100; // 100ms fade out
    private static float activationRadiusSqr = 25f;
    private static final double ANGLE_RAD = Math.toRadians(90f);
    private static boolean enabled;

    public static void setEnabled(boolean b) {
        enabled = b;
        Reporter.reportToggled("Pointer", b);
    }

    public static void setActivationRadius(float radius) {
        activationRadiusSqr = radius*radius;
        Reporter.reportSet("Pointer", "Activation Radius", radius);

    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static float getActivationRadiusSqr() {
        return activationRadiusSqr;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if(!enabled) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer me = mc.thePlayer;

        if (me == null || mc.theWorld == null) return;

        EntityPlayer target = findClosestAttackablePlayerInRange(me, activationRadiusSqr, ANGLE_RAD, event.partialTicks);
        if (target != null) pointAtTarget(target, event.partialTicks);
        else startFadeOut();

        renderPoint(event);

        // Check if we should clear the target after fade out
        if (!isFadingIn && calculateFadeAlpha() == 0) {
            targetPlayer = null;
            pointToRender = null;
        }
    }


    public static void renderPoint(RenderWorldLastEvent event) {
        if (pointToRender == null) return;

        EntityPlayer me = Minecraft.getMinecraft().thePlayer;
        Vec3 intPosMe = Renderer.interpolatedPos(me, event.partialTicks);
        Vec3 pointDv = pointToRender.subtract(intPosMe);

        GlStateManager.pushMatrix();
        GlStateManager.translate(pointDv.xCoord, pointDv.yCoord, pointDv.zCoord);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();

        float radius = 0.15F;
        int slices = 8;
        int stacks = 8;

        float alpha = calculateFadeAlpha();
        GL11.glColor4f(.8f, 0.0f, 0.0f, alpha); // Red color with fading opacity

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
                double x1 = Math.cos(lng);
                double y1 = Math.sin(lng);

                GL11.glNormal3d(x1 * zr0, y1 * zr0, z0);
                GL11.glVertex3d(x1 * zr0 * radius, y1 * zr0 * radius, z0 * radius);
                GL11.glNormal3d(x1 * zr1, y1 * zr1, z1);
                GL11.glVertex3d(x1 * zr1 * radius, y1 * zr1 * radius, z1 * radius);
            }
            GL11.glEnd();
        }

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static Vec3 getNearestPointOnBoxFromMyEyes(Entity entity, double partialTicks) {
        EntityPlayer me = Minecraft.getMinecraft().thePlayer;

        Vec3 intPosMe = Renderer.interpolatedPos(me, partialTicks);
        Vec3 intEyePosMe = intPosMe.addVector(0, me.getEyeHeight(), 0);
        Vec3 intPosEn = Renderer.interpolatedPos(entity, partialTicks);

        // Calculate the offset between the entity's current position and interpolated position
        Vec3 correctionOffset = intPosEn.subtract(entity.getPositionVector());

        // Get the entity's bounding box
        AxisAlignedBB entityBox = entity.getEntityBoundingBox();

        // Create a new bounding box offset by both the correction and the entity's position
        AxisAlignedBB interpolatedBox = entityBox
                .offset(-entity.posX, -entity.posY, -entity.posZ) // First, move to origin
                .offset(intPosEn.xCoord, intPosEn.yCoord, intPosEn.zCoord); // Then, move to interpolated position

        // Find the nearest point on the interpolated bounding box to the player's eyes
        double nx = clamp(intEyePosMe.xCoord, interpolatedBox.minX, interpolatedBox.maxX);
        double ny = clamp(intEyePosMe.yCoord, interpolatedBox.minY, interpolatedBox.maxY);
        double nz = clamp(intEyePosMe.zCoord, interpolatedBox.minZ, interpolatedBox.maxZ);

        return new Vec3(nx, ny, nz);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Set the sphere location to a particular entity. If the entity changes, fade in starts.
     *
     * @param newTarget the target to set the new point to
     */
    public static void pointAtTarget(EntityPlayer newTarget, double partialTicks) {
        if (newTarget != targetPlayer) {
            fadeStartTime = System.currentTimeMillis();
            isFadingIn = true;
            targetPlayer = newTarget;
        }
        pointToRender = getNearestPointOnBoxFromMyEyes(newTarget, partialTicks);
    }

    public static EntityPlayer findClosestAttackablePlayerInRange(EntityPlayer me, double maxDistanceSq, double maxAngleRadians, double partialTicks) {
        EntityPlayer closestPlayer = null;
        double closestAngleRadians = Double.MAX_VALUE;

        for (Entity en : me.worldObj.loadedEntityList) {
            if (!(en instanceof EntityPlayer)) continue;
            EntityPlayer otherPlayer = (EntityPlayer) en;

            if (otherPlayer == me) continue;
            if (!AntiBot.isRecommendedTarget(otherPlayer, maxDistanceSq)) continue;

            Vec3 lookVec = me.getLook(1.0F);
            Vec3 eyePos = me.getPositionEyes(1f);
            Vec3 targetVec = getNearestPointOnBoxFromMyEyes(otherPlayer, partialTicks);

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

    public static Vec3 getVisiblePart(EntityLivingBase me, EntityLivingBase target) {
        Minecraft mc = Minecraft.getMinecraft();
        AxisAlignedBB boundingBox = target.getEntityBoundingBox();
        Vec3 eyePos = me.getPositionEyes(1.0F);

        // Check corners of the bounding box
        Vec3[] points = new Vec3[]{new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.minZ), new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.maxZ), new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.minZ), new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ), new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.minZ), new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ), new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ), new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ),
                // Center of each face
                new Vec3(boundingBox.minX, (boundingBox.minY + boundingBox.maxY) / 2, (boundingBox.minZ + boundingBox.maxZ) / 2), new Vec3(boundingBox.maxX, (boundingBox.minY + boundingBox.maxY) / 2, (boundingBox.minZ + boundingBox.maxZ) / 2), new Vec3((boundingBox.minX + boundingBox.maxX) / 2, boundingBox.minY, (boundingBox.minZ + boundingBox.maxZ) / 2), new Vec3((boundingBox.minX + boundingBox.maxX) / 2, boundingBox.maxY, (boundingBox.minZ + boundingBox.maxZ) / 2), new Vec3((boundingBox.minX + boundingBox.maxX) / 2, (boundingBox.minY + boundingBox.maxY) / 2, boundingBox.minZ), new Vec3((boundingBox.minX + boundingBox.maxX) / 2, (boundingBox.minY + boundingBox.maxY) / 2, boundingBox.maxZ)};

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

    private void startFadeOut() {
        if (isFadingIn || (pointToRender != null && fadeStartTime == 0)) {
            fadeStartTime = System.currentTimeMillis();
            isFadingIn = false;
        }
    }
}