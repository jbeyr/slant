package me.jameesyy.slant.movement;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.Main;
import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class BridgeAssist {
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
        BridgeAssist.enabled = b;
        ModConfig.bridgeAssistEnabled = b;
        Reporter.queueEnableMsg("Bridge Assist", b);

        if(b) return;
        KeyBinding sneakKey = mc.gameSettings.keyBindSneak;
        KeyBinding.setKeyBindState(sneakKey.getKeyCode(), false);
    }

    public static float getEdgeDistance() {
        return edgeDistance;
    }

    public static void setEdgeDistance(float f) {
        BridgeAssist.edgeDistance = f;
        ModConfig.bridgeAssistEdgeDistance = f;
        Reporter.queueSetMsg("Bridge Assist", "Edge Distance", f);
    }

    public static void setLastMovementInput(MovementInput lastMovementInput) {
        BridgeAssist.lastMovementInput = lastMovementInput;
    }

    public static void setDisableIfNotBridgingPitch(boolean b) {
        BridgeAssist.disableIfNotBridgingPitch = b;
        ModConfig.bridgeAssistDisableIfNotBridgingPitch = b;
        Reporter.queueSetMsg("Bridge Assist", "Disable If Not Bridging Pitch", b);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (isEnabled()) {
            if (shouldSwitchBlocksNextFrame) {
                switchToMostBlocks();
                shouldSwitchBlocksNextFrame = false;
            }
            handleBridgeAssist(event.partialTicks);
        }
    }

    private boolean shouldSwitchBlocksNextFrame = false;

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!enabled) return;
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem != null && heldItem.getItem() instanceof ItemBlock) {
            if (heldItem.stackSize <= 1) { // Will be empty after placement
                shouldSwitchBlocksNextFrame = true;
            }
        }
    }

    private void handleBridgeAssist(float partialTicks) {
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
    }

    /**
     * Swaps to the hotbar slot with the most blocks.
     */
    private void switchToMostBlocks() {
        ItemStack currentStack = mc.thePlayer.getHeldItem();
        if (currentStack != null && currentStack.stackSize > 0) return; // current slot still has blocks

        int bestSlot = -1;
        int mostBlocks = 0;

        // Search hotbar for slot with most blocks
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                // Check if block is solid/full
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (block.isFullBlock()) {
                    if (stack.stackSize > mostBlocks) {
                        mostBlocks = stack.stackSize;
                        bestSlot = i;
                    }
                }
            }
        }

        // Switch to best slot if found
        if (bestSlot != -1) {
            mc.thePlayer.inventory.currentItem = bestSlot;
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (Main.getBridgeAssistKey().isPressed()) {
            setEnabled(!enabled);
        }

        if (enabled && mc.gameSettings.keyBindForward.isKeyDown()) {
            setEnabled(false);
        }
    }
}
