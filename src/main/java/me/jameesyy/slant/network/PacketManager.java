package me.jameesyy.slant.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import me.jameesyy.slant.Targeter;
import me.jameesyy.slant.util.LagUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PacketManager implements Targeter.TargetChangeListener {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static ConcurrentLinkedQueue<DelayedPacket<? extends INetHandler>> inboundPacketsQueue = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<DelayedPacket<? extends INetHandler>> outboundPacketsQueue = new ConcurrentLinkedQueue<>();

    public static Optional<EntityPlayer> target;
    public static AxisAlignedBB targetpos;

    public static boolean anythingEnabled() {
        return Backtrack.isEnabled() || PingSpoofer.isEnabled();
    }


    public static boolean InboundSpoofCheck(Packet p) {
        if (IsPlayClientPacket(p) && LagUtils.isIngame()) {
            if (Backtrack.isEnabled()) {
                if (p instanceof S18PacketEntityTeleport) {
                    if (((S18PacketEntityTeleport) p).getEntityId() == mc.thePlayer.getEntityId()) {
                        clearInboundQueue();
                    } else if (target.isPresent() && ((S18PacketEntityTeleport) p).getEntityId() == target.get().getEntityId()) {
                        try {
                            targetpos = new AxisAlignedBB((((S18PacketEntityTeleport) p).getX() / 32D) - 0.3, ((S18PacketEntityTeleport) p).getY() / 32D, (((S18PacketEntityTeleport) p).getZ() / 32D) - 0.3, (((S18PacketEntityTeleport) p).getX() / 32D) + 0.3D, (((S18PacketEntityTeleport) p).getY() / 32D) + 1.8, (((S18PacketEntityTeleport) p).getZ() / 32D) + 0.3);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (p instanceof S14PacketEntity) {
                    if (((S14PacketEntity) p).getEntity(mc.theWorld) != null) {
                        if (target.isPresent() && ((S14PacketEntity) p).getEntity(mc.theWorld).getEntityId() == target.get().getEntityId()) {
                            try {
                                targetpos = new AxisAlignedBB((((S14PacketEntity) p).func_149062_c() / 32D) + targetpos.minX, (((S14PacketEntity) p).func_149061_d() / 32D) + targetpos.minY, (((S14PacketEntity) p).func_149064_e() / 32D) + targetpos.minZ, (((S14PacketEntity) p).func_149062_c() / 32D) + targetpos.minX + 0.6, (((S14PacketEntity) p).func_149061_d() / 32D) + targetpos.maxY, (((S14PacketEntity) p).func_149064_e() / 32D) + targetpos.minZ + 0.6);
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

            clearInboundQueue();
        }

        return false;
    }

    public static void SpoofInboundPacket(Packet<? extends INetHandler> p) {
        inboundPacketsQueue.add(new DelayedPacket(p, System.currentTimeMillis()));
    }

    public static void SpoofOutboundPacket(Packet p) {
        outboundPacketsQueue.add(new DelayedPacket(p, System.currentTimeMillis()));
    }

    public static void clearInboundQueue() {
        for (DelayedPacket packet : inboundPacketsQueue) {
            if (!PingSpoofer.isEnabled() || System.currentTimeMillis() > packet.getTime() + PingSpoofer.getDelay()) {
                try {
                    Packet p = packet.getPacket();
                    p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                inboundPacketsQueue.remove(packet);
            }
        }
    }

    public static void clearOutboundQueue() {
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

    public static boolean IsPlayClientPacket(Packet packet) {

        //return packet instanceof INetHandlerPlayClient;

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
            if (Backtrack.isEnabled() && target.isPresent() && targetpos != null) {
                int rgb = new Color(10, 255, 20).getRGB();
                LagUtils.reAxis(target.get(), targetpos, rgb, true, e.partialTicks);
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (LagUtils.isIngame() && e.phase == TickEvent.Phase.START) {
            if (Backtrack.isEnabled()) {
                Backtrack.tickCheck();
                for (DelayedPacket<? extends INetHandler> packet : inboundPacketsQueue) {
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
                            //   System.out.println("Error! - "+packet +", "+packets.size());
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

    @SubscribeEvent
    public void attack(AttackEntityEvent e) {
        if (LagUtils.isIngame() && anythingEnabled() && e.target instanceof EntityPlayer) {
            if (!target.isPresent() || target.get() != e.target) {
                target = Optional.of((EntityPlayer) e.target);
                targetpos = target.get().getEntityBoundingBox();
            }
        }
    }

    @Override
    public void onTargetChange(Optional<EntityPlayer> oldTarget, Optional<EntityPlayer> newTarget) {
//        if(!LagUtils.isIngame() || !anythingEnabled()) return;
//
//        target = newTarget;
//        targetpos = target.map(Entity::getEntityBoundingBox).orElse(null);
//        Minecraft.getMinecraft().ingameGUI.setRecordPlaying(new ChatComponentText("Target: " + (!target.isPresent() ? "N/A" : target.get().getName())), true);
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