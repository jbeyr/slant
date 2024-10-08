package me.jameesyy.slant.mixins;

import me.jameesyy.slant.Main;
import me.jameesyy.slant.movement.AutoJumpReset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.swing.*;


@Mixin(value = NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    public MixinNetHandlerPlayClient() {
    }

    // region auto jump reset
    @Inject(method = "handleEntityVelocity", at = {@At("HEAD")})
    public void handleEntityVelocity(S12PacketEntityVelocity velocityPacket, CallbackInfo ci) {

        if (!AutoJumpReset.isEnabled()) return;
        Minecraft mc = Main.getMc();
        if (velocityPacket.getEntityID() == mc.thePlayer.getEntityId()) {
            if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) return;
            if (mc.currentScreen != null) return;
            if (!AutoJumpReset.shouldActivate()) return;

            boolean isheld = Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
            AutoJumpReset.legitJump();

            // release the jump key
            Timer timer = new Timer(20, (actionevent) -> {
                if(!isheld) KeyBinding.setKeyBindState(Main.getMc().gameSettings.keyBindJump.getKeyCode(), false);
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    // endregion
}