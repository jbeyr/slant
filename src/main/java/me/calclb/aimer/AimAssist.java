package me.calclb.aimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class AimAssist {

    public static final double RANGE = 5.0; // Range of aim assist
    private static EntityLivingBase target = null;
    private static final Random random = new Random();
    private static float FOV = 110;
    private static float MAX_SPEED = 1f; // Maximum rotation speed in degrees per frame
    private static float ACCELERATION = 0.025f; // How quickly to reach max speed
    private static float DECELERATION_DISTANCE = 15f; // Degrees from target to start slowing down
    private static float BASE_RANDOMNESS = 0.2f; // Base value for random adjustments
    private double currentSpeedYaw = 0;
    private double currentSpeedPitch = 0;


    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (Minecraft.getMinecraft().thePlayer == null) return;

        if (Main.getAimAssistKey().isKeyDown() || Main.getAimAssistKey().isPressed()) {
            if (isValidTarget(target)) aimAtTarget(target);
            else { // find new target
                target = findTarget();
                if (target != null) aimAtTarget(target);
            }
        } else target = null; // reset target if key not pressed
    }

    private EntityLivingBase findTarget() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase closestTarget = null;
        double closestDistance = RANGE * RANGE;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if(!(entity instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer)entity;
            if (player.isPotionActive(Potion.invisibility)) continue;
            if (player == mc.thePlayer || !isValidTarget(player)) continue;
            double distance = mc.thePlayer.getDistanceSqToEntity(player);
            if (distance < closestDistance) {
                closestTarget = player;
                closestDistance = distance;
            }
        }
        return closestTarget;
    }


    private boolean isValidTarget(EntityLivingBase entity) {
        if(entity == null) return false;
        EntityPlayer me = Minecraft.getMinecraft().thePlayer;
        if(me.getTeam() != null && me.getTeam().isSameTeam(entity.getTeam())) return false;
        double distance = me.getDistanceSqToEntity(entity);
        return distance < RANGE*RANGE && entity.isEntityAlive() && entity instanceof EntityPlayer && !AntiBot.isPlayerBot(entity.getUniqueID()) && me.canEntityBeSeen(entity) && isInFOV(entity, FOV);
    }


    private boolean isInFOV(Entity entity, float fov) {
        Minecraft mc = Minecraft.getMinecraft();
        double d0 = entity.posX - mc.thePlayer.posX;
        double d1 = entity.posZ - mc.thePlayer.posZ;
        double d2 = entity.posY + entity.getEyeHeight() - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());

        double d3 = MathHelper.sqrt_double(d0 * d0 + d1 * d1);
        float yaw = (float) (MathHelper.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(d2, d3) * 180.0D / Math.PI));

        float yawDiff = MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw);
        float pitchDiff = MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch);

        return yawDiff >= -fov / 2 && yawDiff <= fov / 2 && pitchDiff >= -fov / 2 && pitchDiff <= fov / 2;
    }

    private void aimAtTarget(EntityLivingBase target) {
        Minecraft mc = Minecraft.getMinecraft();

        // Get the optimal aiming point
        Vec3 aimPoint = Pointer.getNearestPointOnEntityHitbox(target, mc.thePlayer.getPositionEyes(1.0f));

        // Calculate the desired yaw and pitch
        double d0 = aimPoint.xCoord - mc.thePlayer.posX;
        double d1 = aimPoint.zCoord - mc.thePlayer.posZ;
        double d2 = aimPoint.yCoord - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());

        double d3 = MathHelper.sqrt_double(d0 * d0 + d1 * d1);
        float targetYaw = (float) (MathHelper.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
        float targetPitch = (float) (-(MathHelper.atan2(d2, d3) * 180.0D / Math.PI));

        // Add slight randomness to target angles
        targetYaw += (random.nextFloat() - 0.5f) * BASE_RANDOMNESS;
        targetPitch += (random.nextFloat() - 0.5f) * BASE_RANDOMNESS;

        float deltaYaw = MathHelper.wrapAngleTo180_float(targetYaw - mc.thePlayer.rotationYaw);
        float deltaPitch = MathHelper.wrapAngleTo180_float(targetPitch - mc.thePlayer.rotationPitch);

        // Apply acceleration and deceleration for yaw
        if (Math.abs(deltaYaw) > DECELERATION_DISTANCE) {
            currentSpeedYaw = Math.min(currentSpeedYaw + ACCELERATION, MAX_SPEED);
        } else {
            currentSpeedYaw = Math.max(currentSpeedYaw - ACCELERATION, 0);
        }

        // Apply acceleration and deceleration for pitch
        if (Math.abs(deltaPitch) > DECELERATION_DISTANCE) {
            currentSpeedPitch = Math.min(currentSpeedPitch + ACCELERATION, MAX_SPEED);
        } else {
            currentSpeedPitch = Math.max(currentSpeedPitch - ACCELERATION, 0);
        }

        // Apply movement
        double moveYaw = Math.min(Math.abs(deltaYaw), currentSpeedYaw) * Math.signum(deltaYaw);
        double movePitch = Math.min(Math.abs(deltaPitch), currentSpeedPitch) * Math.signum(deltaPitch);

        // Add slight randomness to movement
        moveYaw += (random.nextFloat() - 0.5f) * BASE_RANDOMNESS;
        movePitch += (random.nextFloat() - 0.5f) * BASE_RANDOMNESS;

        mc.thePlayer.rotationYaw += (float) moveYaw;
        mc.thePlayer.rotationPitch += (float) movePitch;

        // Ensure pitch stays within valid range
        mc.thePlayer.rotationPitch = MathHelper.clamp_float(mc.thePlayer.rotationPitch, -90.0F, 90.0F);
    }

    private float smoothAngle(float currentAngle, float targetAngle, double strength) {
        float delta = (targetAngle - currentAngle) % 360.0F;
        if (delta > 180.0F) delta -= 360.0F;
        if (delta < -180.0F) delta += 360.0F;
        return currentAngle + (float) (delta * strength);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        Entity viewer = mc.getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-viewerX, -viewerY, -viewerZ);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);

        // Use the interpolated player position for smooth rendering
        double x = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * event.partialTicks;
        double y = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * event.partialTicks;
        double z = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * event.partialTicks;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; i++) {
            double angle = Math.toRadians(i);
            double xPos = x + RANGE * Math.cos(angle);
            double zPos = z + RANGE * Math.sin(angle);
            worldrenderer.pos(xPos, y, zPos).color(1.0F, 1.0F, 1.0F, 0.2F).endVertex();
        }
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(7424);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void setAimFov(float fov) {
        FOV = Math.min(360, Math.max(1, fov));
    }

    public static void setMaxSpeed(float speed) {
        MAX_SPEED = Math.min(5, Math.max(0, speed));
    }

    public static void setAcceleration(float accel) {
        ACCELERATION = Math.min(1, Math.max(0, accel));
    }

    public static void setDecelerationDistance(float distance) {
        DECELERATION_DISTANCE = Math.min(360, Math.max(0, distance));
    }

    public static void setBaseRandomness(float randomness) {
        BASE_RANDOMNESS = Math.min(1, Math.max(0, randomness));
    }

    public static float getFov() {
        return FOV;
    }

    public static float getMaxSpeed() {
        return MAX_SPEED;
    }

    public static float getAcceleration() {
        return ACCELERATION;
    }

    public static float getDecelerationDistance() {
        return DECELERATION_DISTANCE;
    }

    public static float getBaseRandomness() {
        return BASE_RANDOMNESS;
    }

}