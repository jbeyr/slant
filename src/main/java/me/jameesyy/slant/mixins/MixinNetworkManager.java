package me.jameesyy.slant.mixins;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import me.jameesyy.slant.network.PacketManager;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Mixin(priority = 995, value = NetworkManager.class)
public abstract class MixinNetworkManager {

    @Shadow
    private Channel channel;
    @Shadow
    private INetHandler packetListener;
    @Shadow
    @Final
    private ReentrantReadWriteLock readWriteLock;
    @Shadow
    @Final
    private Queue<PacketManager.InboundHandlerTuplePacketListener<?>> outboundPacketsQueue;

    @Shadow
    public abstract boolean isChannelOpen();

    @Shadow
    protected abstract void flushOutboundQueue();

    @Shadow
    protected abstract void dispatchPacket(final Packet<?> p_dispatchPacket_1_, final GenericFutureListener<? extends Future<? super Void>>[] p_dispatchPacket_2_);

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    protected void channelRead0(final ChannelHandlerContext context, final Packet<?> packet, final CallbackInfo ci) {
        if (packet != null && isChannelOpen()) {
            if (PacketManager.InboundSpoofCheck(packet)) {
                PacketManager.SpoofInboundPacket(packet);
                ci.cancel();
            }
        }
    }
}
