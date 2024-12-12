package me.jameesyy.slant.movement;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.Main;
import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.client.Minecraft;
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

public class Safewalk {
    /**
     * Players bridge at a pitch above this value.
     */
    private static final float BRIDGING_PITCH = 68f;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean enabled;
    private static float edgeDistance;
    private static MovementInput lastMovementInput;
    private static boolean disableIfNotBridgingPitch;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        Safewalk.enabled = b;
        ModConfig.safewalkEnabled = b;
        Reporter.queueEnableMsg("Safewalk", b);

        if(b) return;
        KeyBinding sneakKey = mc.gameSettings.keyBindSneak;
        KeyBinding.setKeyBindState(sneakKey.getKeyCode(), false);
    }

    public static float getEdgeDistance() {
        return edgeDistance;
    }

    public static void setEdgeDistance(float f) {
        Safewalk.edgeDistance = f;
        ModConfig.safewalkEdgeDistance = f;
        Reporter.queueSetMsg("Safewalk", "Edge Distance", f);
    }

    public static void setLastMovementInput(MovementInput lastMovementInput) {
        Safewalk.lastMovementInput = lastMovementInput;
    }

    public static void setDisableIfNotBridgingPitch(boolean b) {
        Safewalk.disableIfNotBridgingPitch = b;
        ModConfig.safewalkDisableIfNotBridgingPitch = b;
        Reporter.queueSetMsg("Safewalk", "Disable If Not Bridging Pitch", b);
    }

    @SubscribeEvent
    public void onRenderWorldLast(net.minecraftforge.client.event.RenderWorldLastEvent event) {
        if (isEnabled()) {
            handleSafewalk(event.partialTicks);
        }
    }

    private void handleSafewalk(float partialTicks) {
        if (!ActionConflictResolver.isSneakingAllowed()) return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player.rotationPitch < BRIDGING_PITCH) {
            if(disableIfNotBridgingPitch) setEnabled(!enabled);
            return;
        }

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


        // check if oriented within the central lane
        boolean isInCenterLane;
        double laneWidth = 0.1;

        // use diagonal lines parallel to respective axes
        if (isXOriented) {
            isInCenterLane = Math.abs(xOffset - zOffset) <= laneWidth ||
                    Math.abs(xOffset + zOffset - 1) <= laneWidth;
        } else {
            isInCenterLane = Math.abs(zOffset - xOffset) <= laneWidth ||
                    Math.abs(zOffset + xOffset - 1) <= laneWidth;
        }

        // check if position behind the player is still a solid block
        double behindX = playerX - xMovement * edgeDistance;
        double behindZ = playerZ - zMovement * edgeDistance;
        BlockPos behindPos = new BlockPos(behindX, playerY - 1, behindZ);
        boolean behindIsSolid = !player.worldObj.isAirBlock(behindPos);

        if (isStrictlyBackwards && isDiagonalYaw) { // diagonal bridging

            if (isSneaking) {
                if (isInCenterLane && behindIsSolid) { // uncrouch if the player is in the center lane and the block behind is solid
                    KeyBinding.setKeyBindState(sneakKey.getKeyCode(), false);
                }
            } else { // crouch if block behind is not solid
                KeyBinding.setKeyBindState(sneakKey.getKeyCode(), !behindIsSolid);
            }
        } else { // side bridging
            KeyBinding.setKeyBindState(sneakKey.getKeyCode(), isAirLayingInWait);
        }

        Minecraft.getMinecraft().ingameGUI.setRecordPlaying(m, true);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (Main.getSafewalkKey().isPressed()) {
            setEnabled(!enabled);
        }

        if (enabled && mc.gameSettings.keyBindForward.isKeyDown()) {
            setEnabled(false);
        }
    }
}
