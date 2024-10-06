package me.jameesyy.slant.mixins;

import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WorldClient.class})
public class MixinWorldClient {
    public MixinWorldClient() {}

    @Inject(method = "sendQuittingDisconnectingPacket", at = {@At("HEAD")})
    public void sendQuittingDisconnectingPacket(CallbackInfo ci) {
    }
}
