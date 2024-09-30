package me.jameesyy.slant.mixins;

import me.jameesyy.slant.combat.NoHitDelay;
import me.jameesyy.slant.util.OppTracker;
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
        // TODO add disconnection handlers for modules if needed
    }
}
