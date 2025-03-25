package me.jameesyy.slant.render;

import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.AntiBot;
import me.jameesyy.slant.util.Renderer;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static me.jameesyy.slant.network.PacketManager.mc;
import static me.jameesyy.slant.util.Renderer.interpolatedPos;

public class Pointer {
    private static final long FADE_IN_DURATION = 200;
    private static final long FADE_OUT_DURATION = 100;
    private static long fadeStartTime = 0;
    private static boolean isFadingIn = false;
    private static boolean enabled;

    public static void setActivationRadius(float radius) {
        ModConfig.pointerActivationRadius = radius;
        Reporter.queueSetMsg("Pointer", "Activation Radius", radius);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        enabled = b;
        ModConfig.pointerEnabled = b;
        Reporter.queueEnableMsg("Pointer", b);
    }

    public static Vec3 getNearestPointOnBoxFromMyEyes(Entity entity, double partialTicks) {
        EntityPlayer me = Minecraft.getMinecraft().thePlayer;

        Vec3 intPosMe = interpolatedPos(me, partialTicks);
        Vec3 intEyePosMe = intPosMe.addVector(0, me.getEyeHeight(), 0);
        Vec3 intPosEn = interpolatedPos(entity, partialTicks);
        AxisAlignedBB entityBox = entity.getEntityBoundingBox();

        // bounding box offset by both (1) the ideal correction and (2) the entity's position
        AxisAlignedBB interpolatedBox = entityBox
                .offset(-entity.posX, -entity.posY, -entity.posZ) // start @ origin
                .offset(intPosEn.xCoord, intPosEn.yCoord, intPosEn.zCoord); // then translate to interpolated position

        // nearest point to player's eyes on the interpolated bounding box
        double nx = clamp(intEyePosMe.xCoord, interpolatedBox.minX, interpolatedBox.maxX);
        double ny = clamp(intEyePosMe.yCoord, interpolatedBox.minY, interpolatedBox.maxY);
        double nz = clamp(intEyePosMe.zCoord, interpolatedBox.minZ, interpolatedBox.maxZ);

        return new Vec3(nx, ny, nz);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static Vec3 getVisiblePart(EntityLivingBase me, EntityLivingBase target) {
        Minecraft mc = Minecraft.getMinecraft();
        AxisAlignedBB boundingBox = target.getEntityBoundingBox();
        Vec3 eyePos = me.getPositionEyes(1.0F);

        Vec3[] points = new Vec3[]{
                // corners of the bounding box
                new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ),
                new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ),

                // center of each face
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


    // region xia
    public static Vec3 getNearestPointOnHitboxFromMyEyes(Entity entity, double partialTicks) {
        EntityPlayer me = mc.thePlayer;

        Vec3 intPosMe = interpolatedPos(me, partialTicks);
        Vec3 intEyePosMe = intPosMe.addVector(0, me.getEyeHeight(), 0);
        Vec3 intPosEn = interpolatedPos(entity, partialTicks);
        AxisAlignedBB entityBox = entity.getEntityBoundingBox();

        // bounding box offset by both (1) the ideal correction and (2) the entity's position
        AxisAlignedBB interpolatedBox = entityBox
                .offset(-entity.posX, -entity.posY, -entity.posZ) // start @ origin
                .offset(intPosEn.xCoord, intPosEn.yCoord, intPosEn.zCoord); // then translate to interpolated position

        // Calculate the optimal point (closest point on box to eye position)
        double nx = clamp(intEyePosMe.xCoord, interpolatedBox.minX, interpolatedBox.maxX);
        double ny = clamp(intEyePosMe.yCoord, interpolatedBox.minY, interpolatedBox.maxY);
        double nz = clamp(intEyePosMe.zCoord, interpolatedBox.minZ, interpolatedBox.maxZ);
        Vec3 optimalPoint = new Vec3(nx, ny, nz);

        // First check if we have line of sight to the optimal point
        if (hasLineOfSight(intEyePosMe, optimalPoint, partialTicks)) {
            return optimalPoint;
        }

        // If no direct line of sight to optimal point, find visible points on hitbox
        List<Vec3> visiblePoints = getVisiblePointsOnHitbox(interpolatedBox, intEyePosMe, partialTicks);

        // If visible points exist, use closest
        if (!visiblePoints.isEmpty()) {
            Vec3 closestPoint = null;
            double closestDistanceSq = Double.MAX_VALUE;

            for (Vec3 point : visiblePoints) {
                double distanceSq = intEyePosMe.squareDistanceTo(point);
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closestPoint = point;
                }
            }

            return closestPoint;
        }

        // Fallback to optimal point even if not visible (rare case)
        return optimalPoint;
    }

    public static List<Vec3> getVisiblePointsOnHitbox(AxisAlignedBB box, Vec3 eyePos, double partialTicks) {
        List<Vec3> visiblePoints = new ArrayList<>();

        // Sample points on the entity hitbox (more samples = more precision but lower performance)
        // Adjust these values for performance vs. precision
        int xSamples = 5;
        int ySamples = 7; // More samples on Y-axis for better vertical precision
        int zSamples = 5;

        // Sample points on the surface of the hitbox
        for (int i = 0; i <= xSamples; i++) {
            for (int j = 0; j <= ySamples; j++) {
                for (int k = 0; k <= zSamples; k++) {
                    // skip internal points; only check surface points
                    if (i > 0 && i < xSamples && j > 0 && j < ySamples && k > 0 && k < zSamples) continue;

                    double x = box.minX + (box.maxX - box.minX) * (i / (double) xSamples);
                    double y = box.minY + (box.maxY - box.minY) * (j / (double) ySamples);
                    double z = box.minZ + (box.maxZ - box.minZ) * (k / (double) zSamples);

                    Vec3 point = new Vec3(x, y, z);

                    // if there's a clear line of sight to this point
                    if (hasLineOfSight(eyePos, point, partialTicks)) visiblePoints.add(point);
                }
            }
        }

        return visiblePoints;
    }

    private static boolean hasLineOfSight(Vec3 from, Vec3 to, double partialTicks) {
        // Check if there are any blocks in the way
        MovingObjectPosition result = mc.theWorld.rayTraceBlocks(from, to, false, true, false);

        // If result is null or the hit position is very close to the target, we have line of sight
        if (result == null || result.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return true;
        }

        // Special handling for partial blocks (slabs, stairs, etc.)
        if (result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos blockPos = result.getBlockPos();
            IBlockState blockState = mc.theWorld.getBlockState(blockPos);
            Block block = blockState.getBlock();

            // Get the actual collision boxes for the block (handles stairs, slabs, etc. properly)
            List<AxisAlignedBB> collisionBoxes = new ArrayList<>();
            AxisAlignedBB mask = new AxisAlignedBB(blockPos, blockPos.add(1, 1, 1));
            block.addCollisionBoxesToList(mc.theWorld, blockPos, blockState, mask, collisionBoxes, null);

            // If the block has no collision boxes (like air) or the ray doesn't hit any of them, we have line of sight
            boolean blocked = false;
            for (AxisAlignedBB collisionBox : collisionBoxes) {
                // Adjust collision box to world coordinates
                AxisAlignedBB worldBox = collisionBox.offset(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                if (worldBox.calculateIntercept(from, to) != null) {
                    blocked = true;
                    break;
                }
            }
            return !blocked;
        }
        return false;
    }


    // endregion
}