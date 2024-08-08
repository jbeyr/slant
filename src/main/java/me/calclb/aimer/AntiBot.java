package me.calclb.aimer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AntiBot {
    private static final long PLAYER_EXISTENCE_TICKS_THRESHOLD = 200;
    private static final double COMBAT_RADIUS = AimAssist.RANGE * 1.25;
    private static final int COMBAT_COOLDOWN_TICKS = 50;

    private static final Map<UUID, Long> playerFirstSeenTick = Maps.newHashMap();
    private static final Set<UUID> whitelist = Sets.newHashSet();
    private static final Set<UUID> blacklist = Sets.newHashSet();

    private static long currentTick = COMBAT_COOLDOWN_TICKS;
    private static long lastCombatTick = 0;

    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent e) {
        EntityPlayer me = Minecraft.getMinecraft().thePlayer;
        if(e.entity == me) return;
        if (!(e.entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) e.entity;

        if(isPlayerInCombat() && player.getDistanceSqToEntity(me) <= COMBAT_RADIUS*COMBAT_RADIUS) {
            blacklist.add(player.getUniqueID());
            me.addChatMessage(new ChatComponentText(player.getName() + " spawned on you in combat!"));
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        currentTick++;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if(isPlayerInCombat()) lastCombatTick = currentTick;

        // Update the player list
        for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
            UUID uuid = playerInfo.getGameProfile().getId();
            if(Minecraft.getMinecraft().thePlayer.getUniqueID() == uuid) continue;
            if (!playerFirstSeenTick.containsKey(uuid)) {
                playerFirstSeenTick.put(uuid, currentTick);

                // Check if player spawned during combat
//                if (isPlayerInCombat()) {
//                    EntityPlayer player = mc.theWorld.getPlayerEntityByUUID(uuid);
//                    if (player != null && !whitelist.contains(uuid) && mc.thePlayer.getDistanceSqToEntity(player) <= COMBAT_RADIUS*COMBAT_RADIUS) {
//                        blacklist.add(uuid);
//                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(player.getName() + " spawned on you in combat!"));
//                    }
//                }
            } else if (!blacklist.contains(uuid) && currentTick - playerFirstSeenTick.get(uuid) >= PLAYER_EXISTENCE_TICKS_THRESHOLD) {
                playerFirstSeenTick.remove(uuid);
                whitelist.add(uuid);
            }
        }

        // Clean up players who are no longer in the list
        Iterator<Map.Entry<UUID, Long>> iterator = playerFirstSeenTick.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (mc.getNetHandler().getPlayerInfo(entry.getKey()) == null) {
                iterator.remove();
                blacklist.remove(entry.getKey());
            }
        }
    }

    private boolean isPlayerInCombat() {
        return currentTick - lastCombatTick < COMBAT_COOLDOWN_TICKS;
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        // Check if the player is attacking or being attacked
        if (event.entity == mc.thePlayer || event.source.getEntity() == mc.thePlayer) {
            lastCombatTick = currentTick;
            System.out.println("Attack interaction: " + mc.thePlayer.getName() + " and " + event.entity.getName());
        }
    }

    public static boolean isPlayerBot(UUID uuid) {
        if(Minecraft.getMinecraft().thePlayer.getUniqueID() == uuid) return false;
        if (whitelist.contains(uuid)) return false;
        if (blacklist.contains(uuid)) return true;
        Long firstSeenTick = playerFirstSeenTick.get(uuid);
        if (firstSeenTick == null) return true; // consider unknown players as bots
        return (currentTick - firstSeenTick) < PLAYER_EXISTENCE_TICKS_THRESHOLD;
    }

    public boolean isPlayerBot(String username) {
        NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(username);
        if (playerInfo == null) return true; // unknown players are considered bots
        return isPlayerBot(playerInfo.getGameProfile().getId());
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        EntityPlayer viewer = mc.thePlayer;
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-viewerX, -viewerY, -viewerZ);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableDepth();

        for (Object obj : mc.theWorld.loadedEntityList) {
            if (!(obj instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) obj;
            if (!(isPlayerBot(player.getUniqueID()))) continue;
            double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
            double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
            double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

            AxisAlignedBB bbox = player.getEntityBoundingBox().expand(0.1, 0.1, 0.1)
                    .offset(x - player.posX, y - player.posY, z - player.posZ);

            drawBoundingBox(bbox, 0.0F, 0.0F, 0.0F, 0.5F);
        }

        GlStateManager.enableDepth();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();

        GlStateManager.popMatrix();
    }

    private void drawBoundingBox(AxisAlignedBB bbox, float red, float green, float blue, float alpha) {
        GlStateManager.color(red, green, blue, alpha);
        RenderGlobal.drawSelectionBoundingBox(bbox);
    }
}