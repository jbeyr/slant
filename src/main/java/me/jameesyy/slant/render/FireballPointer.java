package me.jameesyy.slant.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class FireballPointer {

    private static final double DANGER_DISTANCE = 5.0;
    private static final int POINTER_DISTANCE = 50; // Distance from crosshair to place the pointer

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        Entity player = mc.thePlayer;
        List<Entity> entities = mc.theWorld.loadedEntityList;

        for (Entity entity : entities) {
            if (entity instanceof EntityFireball) {
                renderFireballPointer(mc, player, (EntityFireball) entity, event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
            }
        }
    }

    private void renderFireballPointer(Minecraft mc, Entity player, EntityFireball fireball, int screenWidth, int screenHeight) {
        Vec3 playerPos = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        Vec3 fireballPos = fireball.getPositionVector();
        Vec3 direction = fireballPos.subtract(playerPos);

        double distance = playerPos.distanceTo(fireballPos);
        String distanceText = String.format("%.1f", distance);

        // Transform the direction vector based on player's rotation
        Vec3 transformedDir = transformDirection(direction, player);

        // Calculate screen position
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        double screenX = transformedDir.xCoord / transformedDir.zCoord * mc.gameSettings.fovSetting;
        double screenY = transformedDir.yCoord / transformedDir.zCoord * mc.gameSettings.fovSetting;

        int pointerX, pointerY;

        if (Math.abs(screenX) < 0.1 && Math.abs(screenY) < 0.1) {
            // Fireball is near the crosshair, render pointer at fireball position
            pointerX = centerX + (int)(screenX * centerX);
            pointerY = centerY - (int)(screenY * centerY);
        } else {
            // Calculate pointer position around the crosshair
            double angle = Math.atan2(screenY, screenX);
            pointerX = centerX + (int)(Math.cos(angle) * POINTER_DISTANCE);
            pointerY = centerY - (int)(Math.sin(angle) * POINTER_DISTANCE);
        }

        // Render arrowhead
        GlStateManager.pushMatrix();
        GlStateManager.translate(pointerX, pointerY, 0);
        GlStateManager.rotate((float) Math.toDegrees(Math.atan2(centerY - pointerY, pointerX - centerX)), 0, 0, 1);
        GlStateManager.scale(1, 1, 1);

        int color = distance <= DANGER_DISTANCE ? 0xFFFF0000 : 0xFFFFFFFF; // Red if close, white otherwise
        drawArrowhead(color);

        GlStateManager.popMatrix();

        // Render distance text
        FontRenderer fontRenderer = mc.fontRendererObj;
        fontRenderer.drawStringWithShadow(distanceText, pointerX + 5, pointerY - 4, color);
    }

    private Vec3 transformDirection(Vec3 dir, Entity player) {
        double yaw = Math.toRadians(-player.rotationYaw);
        double pitch = Math.toRadians(-player.rotationPitch);

        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);

        double x = dir.xCoord;
        double y = dir.yCoord;
        double z = dir.zCoord;

        double xz = z * cosYaw - x * sinYaw;
        double x2 = z * sinYaw + x * cosYaw;
        double y2 = y * cosPitch - xz * sinPitch;
        double z2 = y * sinPitch + xz * cosPitch;

        return new Vec3(x2, y2, z2);
    }

    private void drawArrowhead(int color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

        // Draw the triangle
        worldrenderer.pos(0, -5, 0).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF).endVertex();
        worldrenderer.pos(-5, 5, 0).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF).endVertex();
        worldrenderer.pos(5, 5, 0).color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF).endVertex();

        tessellator.draw();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}