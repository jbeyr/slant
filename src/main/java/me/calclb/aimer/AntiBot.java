package me.calclb.aimer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AntiBot {
    private static final long BOT_THRESHOLD_TICKS = 200; // 10 seconds (20 ticks per second)
    private static final Map<UUID, Long> playerFirstSeenTick = Maps.newHashMap();
    private static long currentTick = 0;

    private static final Set<UUID> whitelist = Sets.newHashSet();
    private final File whitelistFile;

    public AntiBot() {
        File configDir = new File(Minecraft.getMinecraft().mcDataDir, "config");
        this.whitelistFile = new File(configDir, "antibot_whitelist.txt");
        loadWhitelist();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        currentTick++;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Update the player list
        for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
            UUID uuid = playerInfo.getGameProfile().getId();
            if (!playerFirstSeenTick.containsKey(uuid)) {
                playerFirstSeenTick.put(uuid, currentTick);
            } else if (currentTick - playerFirstSeenTick.get(uuid) >= BOT_THRESHOLD_TICKS) {
                whitelist.add(uuid);
            }
        }

        // Clean up players who are no longer in the list
        Iterator<Map.Entry<UUID, Long>> iterator = playerFirstSeenTick.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (mc.getNetHandler().getPlayerInfo(entry.getKey()) == null) {
                iterator.remove();
            }
        }

        // Save whitelist periodically (e.g., every 5 minutes)
        if (currentTick % (20 * 60 * 5) == 0) {
            saveWhitelist();
        }
    }

    public static boolean isPlayerBot(UUID uuid) {
        if (whitelist.contains(uuid)) return false;
        Long firstSeenTick = playerFirstSeenTick.get(uuid);
        if (firstSeenTick == null) return true; // Consider unknown players as bots
        return (currentTick - firstSeenTick) < BOT_THRESHOLD_TICKS;
    }

    public boolean isPlayerBot(String username) {
        NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(username);
        if (playerInfo == null) return true; // Consider unknown players as bots
        return isPlayerBot(playerInfo.getGameProfile().getId());
    }

    private void saveWhitelist() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(whitelistFile));
            for (UUID uuid : whitelist) {
                writer.write(uuid.toString());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadWhitelist() {
        if (!whitelistFile.exists()) return;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(whitelistFile));
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    whitelist.add(UUID.fromString(line.trim()));
                } catch (IllegalArgumentException e) {
                    // Invalid UUID, skip this line
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

            drawBoundingBox(bbox, 1.0F, 0.0F, 0.0F, 0.5F);
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