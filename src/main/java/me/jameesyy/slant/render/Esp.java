package me.jameesyy.slant.render;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraftforge.client.event.RenderWorldLastEvent;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import org.lwjgl.opengl.GL11;
//
public class Esp {
//
//    private static boolean do3dRender = false;
//
//
//    private final Minecraft mc = Minecraft.getMinecraft();
//
//    @SubscribeEvent
//    public void onRender(RenderWorldLastEvent event) {
//        if (mc.theWorld == null || mc.thePlayer == null) return;
//
//        for (Entity entity : mc.theWorld.loadedEntityList) {
//            if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
//                double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks;
//                double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks;
//                double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks;
//
//                drawESPBox(entity, x - mc.getRenderManager().viewerPosX, y - mc.getRenderManager().viewerPosY, z - mc.getRenderManager().viewerPosZ);
//            }
//        }
//    }
//
//    private void drawESPBox(Entity entity, double x, double y, double z) {
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(x, y, z);
//        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
//        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
//        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
//        GlStateManager.disableLighting();
//        GlStateManager.depthMask(false);
//        GlStateManager.disableDepth();
//        GlStateManager.enableBlend();
//        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//
//        GL11.glLineWidth(2.0F);
//        GL11.glColor4f(1.0F, 0.0F, 0.0F, 1.0F);
//        GL11.glBegin(GL11.GL_LINE_LOOP);
//        GL11.glVertex2d(-0.5, -0.1);
//        GL11.glVertex2d(-0.5, 2);
//        GL11.glVertex2d(0.5, 2);
//        GL11.glVertex2d(0.5, -0.1);
//        GL11.glEnd();
//
//        GlStateManager.enableDepth();
//        GlStateManager.depthMask(true);
//        GlStateManager.enableLighting();
//        GlStateManager.disableBlend();
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//        GlStateManager.popMatrix();
//    }
}
