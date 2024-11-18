package me.jameesyy.slant.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import me.jameesyy.slant.Targeter;
import me.jameesyy.slant.util.LagUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PacketManager implements Targeter.TargetChangeListener {

    public static Minecraft mc = Minecraft.getMinecraft();
    public static ConcurrentLinkedQueue<DelayedPacket<? extends INetHandler>> inboundPacketsQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<DelayedPacket<? extends INetHandler>> outboundPacketsQueue = new ConcurrentLinkedQueue<>();

    private static EntityPlayer target;
    private static Vec3 currTickTargetPos = null;
    private static Vec3 prevTickTargetPos = null;

    private static long prevTickTargetTime;
    private static long currTickTargetTime;

    public static EntityPlayer getTarget() {
        return target;
    }

    public static Vec3 getCurrTickTargetPos() {
        return currTickTargetPos;
    }

    public static Optional<AxisAlignedBB> getTargetBox() {
        if (currTickTargetPos == null) return Optional.empty();
        return Optional.of(new AxisAlignedBB(currTickTargetPos.xCoord - 0.3, currTickTargetPos.yCoord, currTickTargetPos.zCoord - 0.3,
                currTickTargetPos.xCoord + 0.3, currTickTargetPos.yCoord + 1.8, currTickTargetPos.zCoord + 0.3));
    }

    public static void setTargetPos(Vec3 v) {
        long timestamp = v == null ? 0 : System.currentTimeMillis();

        PacketManager.prevTickTargetPos = currTickTargetPos;
        PacketManager.prevTickTargetTime = currTickTargetTime;
        PacketManager.currTickTargetPos = v;
        PacketManager.currTickTargetTime = timestamp;
    }

    /**
     * Converts a fixed-point coordinate from a Minecraft packet to a floating-point coordinate.
     *
     * @param fixedPointCoordinate The coordinate value from the packet
     * @return The actual in-game coordinate
     */
    private static double packetToWorldCoord(int fixedPointCoordinate) {
        return fixedPointCoordinate / 32D;
    }

    public static boolean shouldSpoofInboundPackets(Packet p) {
        if (isPlayClientPacket(p) && LagUtils.isIngame()) {
            if (Backtrack.isEnabled()) {
                if (p instanceof S18PacketEntityTeleport) {
                    if (((S18PacketEntityTeleport) p).getEntityId() == mc.thePlayer.getEntityId()) {
                        processWholePacketQueue(); // if the user is teleported, process inbound packets
                    } else if (target != null && ((S18PacketEntityTeleport) p).getEntityId() == target.getEntityId()) {
                        try {
                            setTargetPos(new Vec3(
                                    packetToWorldCoord(((S18PacketEntityTeleport) p).getX()),
                                    packetToWorldCoord(((S18PacketEntityTeleport) p).getY()),
                                    packetToWorldCoord(((S18PacketEntityTeleport) p).getZ())
                            ));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (p instanceof S14PacketEntity) {
                    if (((S14PacketEntity) p).getEntity(mc.theWorld) != null) {
                        if (target != null && ((S14PacketEntity) p).getEntity(mc.theWorld).getEntityId() == target.getEntityId()) {
                            try {
                                setTargetPos(new Vec3(
                                        currTickTargetPos.xCoord + packetToWorldCoord(((S14PacketEntity) p).func_149062_c()),
                                        currTickTargetPos.yCoord + packetToWorldCoord(((S14PacketEntity) p).func_149061_d()),
                                        currTickTargetPos.zCoord + packetToWorldCoord(((S14PacketEntity) p).func_149064_e())));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            if (Backtrack.isEnabled()) {
                if (Backtrack.shouldSpoof) {
                    return true;
                }
            }
            if (PingSpoofer.isEnabled()) {
                return true;
            }

            processWholePacketQueue();
        }

        return false;
    }

    public static <T extends INetHandler> void enqueueSpoofedInboundPacket(Packet<T> p) {
        inboundPacketsQueue.add(new DelayedPacket<>(p, System.currentTimeMillis()));
    }

    public static <T extends INetHandler> void enqueueSpoofedOutboundPacket(Packet<T> p) {
        outboundPacketsQueue.add(new DelayedPacket<>(p, System.currentTimeMillis()));
    }

    public static void processWholePacketQueue() {
        for (DelayedPacket packet : inboundPacketsQueue) {
            if (!PingSpoofer.isEnabled() || System.currentTimeMillis() > packet.getTime() + PingSpoofer.getDelay()) {
                try {
                    Packet p = packet.getPacket();
                    p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                inboundPacketsQueue.remove(packet);
            }
        }
    }

    public static void sendWholeOutboundQueue() {
        for (DelayedPacket packet : outboundPacketsQueue) {
            try {
                Packet p = packet.getPacket();
                mc.thePlayer.sendQueue.getNetworkManager().sendPacket(p);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            outboundPacketsQueue.remove(packet);
        }
    }

    public static boolean isPlayClientPacket(Packet<? extends INetHandler> packet) {

        // return packet instanceof INetHandlerPlayClient;

        return packet instanceof S0EPacketSpawnObject || packet instanceof S11PacketSpawnExperienceOrb || packet instanceof S2CPacketSpawnGlobalEntity || packet instanceof
                S0FPacketSpawnMob || packet instanceof S3BPacketScoreboardObjective || packet instanceof S10PacketSpawnPainting || packet instanceof S0CPacketSpawnPlayer || packet instanceof S0BPacketAnimation || packet instanceof
                S37PacketStatistics || packet instanceof S25PacketBlockBreakAnim || packet instanceof S36PacketSignEditorOpen || packet instanceof S35PacketUpdateTileEntity || packet instanceof S24PacketBlockAction || packet instanceof
                S23PacketBlockChange || packet instanceof S02PacketChat || packet instanceof S3APacketTabComplete || packet instanceof S22PacketMultiBlockChange || packet instanceof S34PacketMaps || packet instanceof S32PacketConfirmTransaction || packet instanceof
                S2EPacketCloseWindow || packet instanceof S30PacketWindowItems || packet instanceof S2DPacketOpenWindow || packet instanceof S31PacketWindowProperty || packet instanceof S2FPacketSetSlot || packet instanceof S3FPacketCustomPayload || packet instanceof S0APacketUseBed || packet instanceof S19PacketEntityStatus || packet instanceof S1BPacketEntityAttach || packet instanceof S27PacketExplosion || packet instanceof S2BPacketChangeGameState || packet instanceof
                S00PacketKeepAlive || packet instanceof S21PacketChunkData || packet instanceof S26PacketMapChunkBulk || packet instanceof S28PacketEffect || packet instanceof S14PacketEntity || packet instanceof S08PacketPlayerPosLook || packet instanceof
                S2APacketParticles || packet instanceof S39PacketPlayerAbilities || packet instanceof S38PacketPlayerListItem || packet instanceof S13PacketDestroyEntities || packet instanceof S1EPacketRemoveEntityEffect || packet instanceof S07PacketRespawn || packet instanceof
                S19PacketEntityHeadLook || packet instanceof S09PacketHeldItemChange || packet instanceof S3DPacketDisplayScoreboard || packet instanceof S1CPacketEntityMetadata || packet instanceof S12PacketEntityVelocity || packet instanceof S04PacketEntityEquipment || packet instanceof
                S1FPacketSetExperience || packet instanceof S06PacketUpdateHealth || packet instanceof S3EPacketTeams || packet instanceof S3CPacketUpdateScore || packet instanceof S05PacketSpawnPosition || packet instanceof S03PacketTimeUpdate || packet instanceof S33PacketUpdateSign || packet instanceof
                S29PacketSoundEffect || packet instanceof S0DPacketCollectItem || packet instanceof S18PacketEntityTeleport || packet instanceof S20PacketEntityProperties || packet instanceof S1DPacketEntityEffect || packet instanceof S42PacketCombatEvent || packet instanceof
                S41PacketServerDifficulty || packet instanceof S43PacketCamera || packet instanceof S44PacketWorldBorder || packet instanceof S45PacketTitle || packet instanceof S46PacketSetCompressionLevel || packet instanceof S47PacketPlayerListHeaderFooter || packet instanceof
                S48PacketResourcePackSend || packet instanceof S49PacketUpdateEntityNBT;
    }

    @SubscribeEvent
    public void r1(RenderWorldLastEvent e) {
        if (LagUtils.isIngame()) {
            if (Backtrack.isEnabled() && target != null && currTickTargetPos != null && prevTickTargetPos != null) {
                long currentTime = System.currentTimeMillis();
                long timeSinceLastUpdate = currentTime - currTickTargetTime;
                long updateInterval = currTickTargetTime - prevTickTargetTime;

                float targetPartialTicks = (float) timeSinceLastUpdate / updateInterval;
                LagUtils.drawTrueBacktrackHitbox(prevTickTargetPos, currTickTargetPos, targetPartialTicks, e.partialTicks, .3f, .7f, .3f, 1f);
            }
        }
    }

    @Override
    public void onTargetChange(Optional<EntityPlayer> oldTarget, Optional<EntityPlayer> newTarget) {
        if (!LagUtils.isIngame() || (!Backtrack.isEnabled() && !PingSpoofer.isEnabled())) return;

        target = newTarget.orElse(null);
        setTargetPos(target == null ? null : new Vec3(target.posX, target.posY, target.posZ));
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (LagUtils.isIngame() && e.phase == TickEvent.Phase.START) {
            if (Backtrack.isEnabled()) {
                Backtrack.tickCheck();
                for (DelayedPacket packet : inboundPacketsQueue) {
                    if (System.currentTimeMillis() > packet.getTime() + Backtrack.getDelay() + (PingSpoofer.isEnabled() ? PingSpoofer.getDelay() : 0)) {
                        try {
                            Packet p = packet.getPacket();
                            if (p instanceof S3EPacketTeams) {
                                if (((S3EPacketTeams) p).getPlayers() != null) {
                                    p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
                                }
                            } else if (p instanceof S0CPacketSpawnPlayer) {
                                if (((S0CPacketSpawnPlayer) p).getPlayer() != null) {
                                    p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
                                }

                            } else if (p instanceof S3BPacketScoreboardObjective) {
                                if (((S3BPacketScoreboardObjective) p).func_149337_d() != null) {
                                    p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
                                }
                            } else {
                                p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        inboundPacketsQueue.remove(packet);
                    }
                }
            }

            if (PingSpoofer.isEnabled()) {
                if (!Backtrack.shouldSpoof) {
                    PingSpoofer.tickCheck();
                }
            }
        }
    }

    public static class InboundHandlerTuplePacketListener<T extends INetHandler> {
        public final Packet<T> packet;
        public final GenericFutureListener<? extends Future<? super Void>>[] futureListeners;

        public InboundHandlerTuplePacketListener(Packet<T> p_i45146_1_, GenericFutureListener<? extends Future<? super Void>>... p_i45146_2_) {
            this.packet = p_i45146_1_;
            this.futureListeners = p_i45146_2_;
        }
    }
}