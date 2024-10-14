package me.jameesyy.slant.network;

import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

public class DelayedPacket<T extends INetHandler> {
    private final long time;
    private final Packet<T> packet;
    public DelayedPacket(final Packet<T> packet, final long time) {
        this.packet = packet;
        this.time = time;
    }
    public long getTime() {
        return time;
    }
    public Packet<T> getPacket() {
        return packet;
    }
}
