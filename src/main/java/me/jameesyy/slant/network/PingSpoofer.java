package me.jameesyy.slant.network;

import me.jameesyy.slant.ModConfig;
import me.jameesyy.slant.util.Reporter;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3EPacketTeams;

import static me.jameesyy.slant.network.PacketManager.inboundPacketsQueue;
import static me.jameesyy.slant.network.PacketManager.mc;

public class PingSpoofer {

    private static boolean enabled;
    private static int delay;

    public static void tickCheck() {
        for (DelayedPacket packet : inboundPacketsQueue) {
            if (System.currentTimeMillis() > packet.getTime() + delay) {
                try {
                    Packet p = packet.getPacket();
                    if (p instanceof S3EPacketTeams) {
                        if (((S3EPacketTeams) p).getPlayers() != null) {
                            p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
                        }
                    } else if (p instanceof S0CPacketSpawnPlayer){
                        if (((S0CPacketSpawnPlayer) p).getPlayer()!=null) {
                            p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
                        }

                    }else if (p instanceof S3BPacketScoreboardObjective){
                        if (((S3BPacketScoreboardObjective) p).func_149337_d()!=null) {
                            p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
                        }

                    }else{

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

//    public static void tickCheck() {
//        for (DelayedPacket<? extends INetHandler> packet : inboundPacketsQueue) {
//
//            // ignore packets in queue for later if we hadn't received it by now according to our emulated ping
//            if (System.currentTimeMillis() <= packet.getTime() + delay) continue;
//
//            try {
//                Packet p = packet.getPacket();
//                if (p instanceof S3EPacketTeams) {
//                    if (((S3EPacketTeams) p).getPlayers() != null) p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
//                } else if (p instanceof S0CPacketSpawnPlayer) {
//                    if (((S0CPacketSpawnPlayer) p).getPlayer() != null) p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
//                } else if (p instanceof S3BPacketScoreboardObjective) {
//                    if (((S3BPacketScoreboardObjective) p).func_149337_d() != null) p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
//                } else p.processPacket(mc.thePlayer.sendQueue.getNetworkManager().getNetHandler());
//            } catch (Exception exception) {
//                //   System.out.println("Error! - "+packet +", "+packets.size());
//                exception.printStackTrace();
//            }
//            inboundPacketsQueue.remove(packet);
//        }
//    }

    public static int getDelay() {
        return delay;
    }

    public static void setDelay(int delay) {
        PingSpoofer.delay = delay;
        ModConfig.pingSpooferDelayMs = delay;
        Reporter.reportSet("Ping Spoofer", "Delay", delay);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        PingSpoofer.enabled = b;
        ModConfig.pingSpooferEnabled = b;
        Reporter.reportToggled("Ping Spoofer", b);
    }
}


/*

package me.jameesyy.slant.network;

import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3EPacketTeams;

import static me.jameesyy.slant.network.PacketManager.inboundPacketsQueue;
import static me.jameesyy.slant.network.PacketManager.mc;

public class PingChanger {


}


*/