package me.jameesyy.slant.movement;

import gg.essential.universal.ChatColor;
import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.Main;
import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovementInput;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.opengl.GL11;

public class Safewalk {
    /**
     * Players bridge at a pitch above this value.
     */
    private static final float BRIDGING_PITCH = 73f;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean enabled;
    private static float edgeDistance;
    private static MovementInput lastMovementInput;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        Safewalk.enabled = b;
        ModConfig.safewalkEnabled = b;
        Reporter.reportToggled("Safewalk", b);
    }

    public static float getEdgeDistance() {
        return edgeDistance;
    }

    public static void setEdgeDistance(float f) {
        Safewalk.edgeDistance = f;
        ModConfig.safewalkEdgeDistance = f;
        Reporter.reportSet("Safewalk", "Edge Distance", f);
    }

    public static void setLastMovementInput(MovementInput lastMovementInput) {
        Safewalk.lastMovementInput = lastMovementInput;
    }

    public static void renderMovementLine(float partialTicks) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player == null || lastMovementInput == null) return;

        float forward = lastMovementInput.moveForward;
        float strafe = lastMovementInput.moveStrafe;
        float yaw = player.rotationYaw;

        if (forward != 0 || strafe != 0) {
            // Calculate movement angle
            double moveAngle;
            if (forward != 0 && strafe != 0) {
                moveAngle = Math.atan2(-strafe, forward) + Math.toRadians(yaw);
            } else if (forward != 0) {
                moveAngle = forward > 0 ? Math.toRadians(yaw) : Math.toRadians(yaw + 180);
            } else {
                moveAngle = strafe > 0 ? Math.toRadians(yaw - 90) : Math.toRadians(yaw + 90);
            }

            // Calculate movement vector
            double xMovement = -Math.sin(moveAngle);
            double zMovement = Math.cos(moveAngle);

            // Get the look vector (direction the player is facing)
            net.minecraft.util.Vec3 lookVec = player.getLook(partialTicks);

            // Calculate the start point (right in front of the player's eyes)
            double startX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks + lookVec.xCoord * 0.5;
            double startY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks + player.getEyeHeight() + lookVec.yCoord * 0.5;
            double startZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks + lookVec.zCoord * 0.5;

            // Calculate the end point (10 blocks from the start point in the movement direction)
            double endX = startX + xMovement * 10;
            double endY = startY;
            double endZ = startZ + zMovement * 10;

            // Render the line
            net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
            net.minecraft.client.renderer.WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.disableTexture2D();
            net.minecraft.client.renderer.GlStateManager.disableLighting();
            net.minecraft.client.renderer.GlStateManager.disableDepth();
            net.minecraft.client.renderer.GlStateManager.enableBlend();
            net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            net.minecraft.client.renderer.GlStateManager.color(0.0F, 1.0F, 0.0F, 1.0F); // Bright green color
            GL11.glLineWidth(5f); // Thicker line

            worldrenderer.begin(3, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
            worldrenderer.pos(startX, startY, startZ).endVertex();
            worldrenderer.pos(endX, endY, endZ).endVertex();
            tessellator.draw();

            net.minecraft.client.renderer.GlStateManager.enableTexture2D();
            net.minecraft.client.renderer.GlStateManager.enableLighting();
            net.minecraft.client.renderer.GlStateManager.enableDepth();
            net.minecraft.client.renderer.GlStateManager.disableBlend();
            net.minecraft.client.renderer.GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (Main.getSafewalkKey().isPressed()) setEnabled(!enabled);
    }

    @SubscribeEvent
    public void onRenderWorldLast(net.minecraftforge.client.event.RenderWorldLastEvent event) {
        if (isEnabled()) {
            renderMovementLine(event.partialTicks);
            handleSafewalk(event.partialTicks);
        }
    }

    private void handleSafewalk(float partialTicks) {
        if (!Safewalk.isEnabled() || !ActionConflictResolver.isSneakingAllowed()) return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player.rotationPitch < BRIDGING_PITCH) return;

        KeyBinding sneakKey = mc.gameSettings.keyBindSneak;
        boolean isSneaking = sneakKey.isKeyDown();

        ItemStack heldItem = player.getHeldItem();
        if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock)) {
            KeyBinding.setKeyBindState(sneakKey.getKeyCode(), false);
            return;
        }

        double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        float forward = lastMovementInput.moveForward;
        float strafe = lastMovementInput.moveStrafe;

        boolean isStrictlyBackwards = forward < 0 && strafe == 0;

        double xOffset = playerX - Math.floor(playerX);
        double zOffset = playerZ - Math.floor(playerZ);

        float yaw = player.rotationYaw % 360;
        if (yaw < 0) yaw += 360;
        boolean isDiagonalYaw = (Math.abs(yaw - 45) < 15 || Math.abs(yaw - 135) < 15 || Math.abs(yaw - 225) < 15 || Math.abs(yaw - 315) < 15);

        double moveAngle = Math.atan2(-strafe, forward) + Math.toRadians(yaw);
        double xMovement = -Math.sin(moveAngle);
        double zMovement = Math.cos(moveAngle);

        double futureX = playerX + xMovement * edgeDistance;
        double futureZ = playerZ + zMovement * edgeDistance;

        BlockPos futurePos = new BlockPos(futureX, playerY - 1, futureZ);
        boolean isAirLayingInWait = player.worldObj.isAirBlock(futurePos);

        IChatComponent m = new ChatComponentText("");

        boolean isXOriented = (yaw < 45 || yaw >= 315) || (yaw >= 135 && yaw < 225);


        // Check if the player is within the oriented lane
        boolean isInCenterLane;
        double laneWidth = 0.1; // Adjust this value to change the width of the lane

        if (isXOriented) {
            // X direction: use diagonal lines parallel to X-axis
            isInCenterLane = Math.abs(xOffset - zOffset) <= laneWidth ||
                    Math.abs(xOffset + zOffset - 1) <= laneWidth;
        } else {
            // Z direction: use diagonal lines parallel to Z-axis
            isInCenterLane = Math.abs(zOffset - xOffset) <= laneWidth ||
                    Math.abs(zOffset + xOffset - 1) <= laneWidth;
        }

        m.appendSibling(new ChatComponentText(ChatColor.WHITE + "orient: " + (isXOriented ? ChatColor.AQUA + "X " : ChatColor.BLUE + "Z ")));
        m.appendSibling(new ChatComponentText((isInCenterLane ? ChatColor.GREEN : ChatColor.RED) + "isInCenterLane "));

        BlockPos currentPos = new BlockPos(playerX, playerY - 1, playerZ);
        boolean currentIsSolid = !player.worldObj.isAirBlock(currentPos);

        // Check if the position directly behind the player is still on a block
        double behindX = playerX - xMovement * edgeDistance;
        double behindZ = playerZ - zMovement * edgeDistance;
        BlockPos behindPos = new BlockPos(behindX, playerY - 1, behindZ);
        boolean behindIsSolid = !player.worldObj.isAirBlock(behindPos);
        m.appendSibling(new ChatComponentText((currentIsSolid ? ChatColor.GREEN : ChatColor.RED) + "currentIsSolid "));
        m.appendSibling(new ChatComponentText((behindIsSolid ? ChatColor.GREEN : ChatColor.RED) + "behindIsSolid "));
        m.appendSibling(new ChatComponentText((isAirLayingInWait ? ChatColor.GREEN : ChatColor.RED) + "isAirLayingInWait "));

        if (isStrictlyBackwards && isDiagonalYaw) { // diagonal bridging

            if (isSneaking) {
                if (isInCenterLane && behindIsSolid) {
                    // Uncrouch if the player is in the center lane and the block behind is solid
                    KeyBinding.setKeyBindState(sneakKey.getKeyCode(), false);
                }
                // If not in the center lane or block behind isn't solid, we don't change the sneaking state
            } else {
                // Crouch if the block behind is not solid
                KeyBinding.setKeyBindState(sneakKey.getKeyCode(), !behindIsSolid);
            }
        } else { // side bridging
            KeyBinding.setKeyBindState(sneakKey.getKeyCode(), isAirLayingInWait);
        }

        Minecraft.getMinecraft().ingameGUI.setRecordPlaying(m, true);
    }
}
