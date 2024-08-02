package me.calclb.aimer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

public class AimAssist {

    private static final double RANGE = 5.0; // Range of aim assist
    private static final double STRENGTH = 0.05; // Strength of aim assist (0.0 - 1.0)
    private static final float FOV = 90;
    private static EntityLivingBase target = null;

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

        // Get the optimal aiming point using the helper method
        Vec3 aimPoint = Pointer.getNearestPointOnEntityHitbox(target, mc.thePlayer.getPositionEyes(1.0f));

        // Calculate the yaw and pitch needed to aim at the point
        double d0 = aimPoint.xCoord - mc.thePlayer.posX;
        double d1 = aimPoint.zCoord - mc.thePlayer.posZ;
        double d2 = aimPoint.yCoord - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());

        double d3 = MathHelper.sqrt_double(d0 * d0 + d1 * d1);
        float yaw = (float) (MathHelper.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(d2, d3) * 180.0D / Math.PI));

        // Smoothly adjust the player's view angles
        mc.thePlayer.rotationYaw = smoothAngle(mc.thePlayer.rotationYaw, yaw, STRENGTH);
        mc.thePlayer.rotationPitch = smoothAngle(mc.thePlayer.rotationPitch, pitch, STRENGTH);
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
}