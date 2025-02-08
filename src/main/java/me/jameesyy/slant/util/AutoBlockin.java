package me.jameesyy.slant.util;

import gg.essential.universal.ChatColor;
import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.Main;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

public class AutoBlockin {

    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean isActive = false; // Whether the module is enabled
    private BlockPos targetBed = null; // Reference to the first bed found

    private static final BlockPos[] adjacentOffsets = {
        new BlockPos(0, 2, 0),  // Block directly above the player's head

        // horizontal blocks (feet level)
        new BlockPos(0, 0, -1), // North (feet level)
        new BlockPos(0, 0, 1),  // South (feet level)
        new BlockPos(-1, 0, 0), // West (feet level)
        new BlockPos(1, 0, 0),  // East (feet level)

        // horizontal blocks (head level)
        new BlockPos(0, 1, -1), // North (head level)
        new BlockPos(0, 1, 1),  // South (head level)
        new BlockPos(-1, 1, 0), // West (head level)
        new BlockPos(1, 1, 0),   // East (head level)

        new BlockPos(0, -1, 0) // Block directly at the player's feet (the one they're standing on)
    };

    /**
     * @return all the block positions adjacent to the player model (one below and above them, and also two in each direction).
     */
    private static BlockPos[] neighborBlocks() {
        Minecraft mc = Minecraft.getMinecraft();
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

        BlockPos[] adjacent = new BlockPos[adjacentOffsets.length];
        for(int i = 0; i < adjacent.length; i++) adjacent[i] = playerPos.add(adjacentOffsets[i]);
        return adjacent;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Main.getAutoBlockinKey().isPressed()) {
            activate();
        }
    }

    private float lastPlayerYaw;
    private float lastPlayerPitch;

    public void activate() {
        isActive = true;
        targetBed = null;
        // Store initial rotations when activated
        lastPlayerYaw = mc.thePlayer.rotationYaw;
        lastPlayerPitch = mc.thePlayer.rotationPitch;
        Reporter.queueEnableMsg("Auto Block-in", true);
    }

    // Deactivate the module
    public void deactivate() {
        isActive = false;
        Reporter.queueEnableMsg("Auto Block-in", false);

    }

    // Add to class
    @SubscribeEvent
    public void onRender3D(RenderWorldLastEvent event) {
        if (!isActive || currentTarget == null) return;

        double x = currentTarget.getX() - mc.getRenderManager().viewerPosX;
        double y = currentTarget.getY() - mc.getRenderManager().viewerPosY;
        double z = currentTarget.getZ() - mc.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        // Box color (red with 40% alpha)
        GlStateManager.color(1.0F, 0.0F, 0.0F, 0.4F);

        // Draw box
        AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
        RenderGlobal.drawSelectionBoundingBox(bb);

        // Draw outline
        GlStateManager.color(1.0F, 0.0F, 0.0F, 1.0F);
        GL11.glLineWidth(2.0F);
        RenderGlobal.drawSelectionBoundingBox(bb);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }


    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (isActive && (event.dx != 0 || event.dy != 0)) {
            Reporter.msg("Mouse moved - deactivating");
            deactivate();
        }
    }


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(!ActionConflictResolver.isRotatingAllowed()) return;

        boolean covered = isCovered();
        IChatComponent m = new ChatComponentText("Covered? " + (covered ? ChatColor.GREEN + "yes" : ChatColor.RED + "no"));
        Minecraft.getMinecraft().ingameGUI.setRecordPlaying(m, true);

        if(!isActive) return;
        if (!covered) {
            coverPlayer();
        } else {
            targetBed = findNearbyBed();
            if (targetBed != null) rotateToBed(targetBed);
            deactivate();
        }
    }

    /**
     * @return true if the player is completely surrounded by solid blocks; false otherwise
     */
    private boolean isCovered() {
        for (BlockPos neighborPos : neighborBlocks()) {
            if (!mc.theWorld.getBlockState(neighborPos).getBlock().isFullBlock()) return false;
        }
        return true;
    }

    // Covers the player by placing blocks on all exposed faces
    private boolean isRotating = false;
    private BlockPos currentTarget = null;
    private float targetYaw, targetPitch;

    private void coverPlayer() {
        if (isRotating) {
            if (!mc.theWorld.isAirBlock(currentTarget)) {
                isRotating = false;
                currentTarget = null;
                return;
            }

            if (hasReachedTargetRotation()) {
                placeBlock(currentTarget);
                isRotating = false;
                currentTarget = null;
            }
            return;
        }

        // Find next position that needs a block
        for (BlockPos neighborPos : neighborBlocks()) {
            if (mc.theWorld.isAirBlock(neighborPos)) {
                // Find a valid adjacent face to place against
                for (EnumFacing side : EnumFacing.values()) {
                    BlockPos placementNeighbor = neighborPos.offset(side);
                    if (mc.theWorld.getBlockState(placementNeighbor).getBlock().isFullBlock()) {
                        // Calculate rotations to the center of the face we're clicking
                        Vec3 hitVec = getHitVec(placementNeighbor, side.getOpposite());
                        float[] rotations = getRotationsToVector(hitVec);

                        targetYaw = rotations[0];
                        targetPitch = rotations[1];
                        isRotating = true;
                        currentTarget = neighborPos;
                        return;
                    }
                }
            }
        }
    }

    private Vec3 getHitVec(BlockPos pos, EnumFacing face) {
        double x = pos.getX() + 0.5 + face.getFrontOffsetX() * 0.5;
        double y = pos.getY() + 0.5 + face.getFrontOffsetY() * 0.5;
        double z = pos.getZ() + 0.5 + face.getFrontOffsetZ() * 0.5;
        return new Vec3(x, y, z);
    }

    private float[] getRotationsToVector(Vec3 vec) {
        double xDiff = vec.xCoord - mc.thePlayer.posX;
        double yDiff = vec.yCoord - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double zDiff = vec.zCoord - mc.thePlayer.posZ;

        double distance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(yDiff, distance) * 180.0D / Math.PI);

        return new float[]{yaw, pitch};
    }

    private boolean hasReachedTargetRotation() {
        float yawDiff = Math.abs(mc.thePlayer.rotationYaw - targetYaw);
        float pitchDiff = Math.abs(mc.thePlayer.rotationPitch - targetPitch);

        // Smoothly interpolate rotations
        mc.thePlayer.rotationYaw = interpolateRotation(mc.thePlayer.rotationYaw, targetYaw, 15f);
        mc.thePlayer.rotationPitch = interpolateRotation(mc.thePlayer.rotationPitch, targetPitch, 15f);

        // Consider rotation complete if we're close enough to target angles
        return yawDiff < 2f && pitchDiff < 2f;
    }

    private float interpolateRotation(float current, float target, float speed) {
        float diff = MathHelper.wrapAngleTo180_float(target - current);
        if (Math.abs(diff) > speed) {
            diff = speed * Math.signum(diff);
        }
        return MathHelper.wrapAngleTo180_float(current + diff);
    }

    // Places a block at the specified position
    private void placeBlock(BlockPos pos) {
        int blockSlot = findBlockInHotbar();
        if (blockSlot == -1) return;

        mc.thePlayer.inventory.currentItem = blockSlot;

        // Special case for block above player - we need to place it from the side first
        if (pos.equals(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 2, mc.thePlayer.posZ))) {
            // Try to place a temporary block at head level if needed
            BlockPos headLevel = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1, mc.thePlayer.posZ);
            for (EnumFacing horizontal : new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST}) {
                BlockPos adjacent = headLevel.offset(horizontal);
                if (mc.theWorld.isAirBlock(adjacent)) {
                    // Place temporary block at head level
                    for (EnumFacing side : EnumFacing.values()) {
                        BlockPos neighbor = adjacent.offset(side);
                        if (mc.theWorld.getBlockState(neighbor).getBlock().isFullBlock()) {
                            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(),
                                    neighbor, side.getOpposite(),
                                    new Vec3(adjacent.getX() + 0.5, adjacent.getY() + 0.5, adjacent.getZ() + 0.5));

                            // Now place the block above using the temporary block
                            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(),
                                    adjacent, EnumFacing.UP,
                                    new Vec3(pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5));
                            return;
                        }
                    }
                }
            }
        }

        // Normal block placement for other positions
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(side);
            EnumFacing opposite = side.getOpposite();

            if (mc.theWorld.getBlockState(neighbor).getBlock().isFullBlock()) {
                mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(),
                        neighbor, opposite, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                return;
            }
        }
    }

    // Finds the index of the first block in the player's hotbar
    private int findBlockInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                return i;
            }
        }
        return -1; // No block found
    }

    // Finds the first bed within a 10-block radius around the player
    private BlockPos findNearbyBed() {
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                    if (block == Blocks.bed) {
                        return pos; // Return the first bed found
                    }
                }
            }
        }

        return null;
    }

    // Rotates the player to face the specified bed position
    private void rotateToBed(BlockPos bedPos) {
        double xDiff = bedPos.getX() + 0.5 - mc.thePlayer.posX;
        double yDiff = bedPos.getY() + 0.5 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double zDiff = bedPos.getZ() + 0.5 - mc.thePlayer.posZ;

        double distance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) (Math.atan2(zDiff, xDiff) * (180 / Math.PI)) - 90;
        float pitch = (float) -(Math.atan2(yDiff, distance) * (180 / Math.PI));

        mc.thePlayer.rotationYaw = yaw;
        mc.thePlayer.rotationPitch = pitch;
    }
}
