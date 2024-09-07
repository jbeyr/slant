package me.calclb.aimer.esp;

import me.calclb.aimer.util.Renderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class BedEsp {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final int RANGE = 20;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        EntityPlayer player = mc.thePlayer;
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-x, -y, -z);

        BlockPos playerPos = player.getPosition();

        for (int dx = -RANGE; dx <= RANGE; dx++) {
            for (int dy = -RANGE; dy <= RANGE; dy++) {
                for (int dz = -RANGE; dz <= RANGE; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();

                    if (block instanceof BlockBed) {
                        drawOutlinedBoundingBox(pos);
                    }
                }
            }
        }

        GlStateManager.popMatrix();
    }

    private void drawOutlinedBoundingBox(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        AxisAlignedBB box = block.getSelectedBoundingBox(mc.theWorld, pos);

        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Set color to light gray with low opacity
        GlStateManager.color(0.8F, 0.8F, 0.8F, 0.2F);

        RenderGlobal.drawOutlinedBoundingBox(box, Renderer.scaleZeroOneRangeTo255(0.8f), Renderer.scaleZeroOneRangeTo255(0.8f), Renderer.scaleZeroOneRangeTo255(0.8f), Renderer.scaleZeroOneRangeTo255(0.2f));

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
    }
}