package me.jameesyy.slant.mixins;

import me.jameesyy.slant.Main;
import me.jameesyy.slant.movement.AutoJumpReset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
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

    @Inject(method = "handleEntityVelocity", at = {@At("HEAD")})
    public void handleEntityVelocity(S12PacketEntityVelocity velocityPacket, CallbackInfo ci) {
        if (!AutoJumpReset.isEnabled()) return;
        Minecraft mc = Main.getMc();
        if (velocityPacket.getEntityID() == mc.thePlayer.getEntityId()) {
            if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) return;
            if (mc.currentScreen != null) return;
            if (!AutoJumpReset.shouldActivate()) return;

            // prevent jumping after taking fall damage
            if (velocityPacket.getMotionY() <= 0) return;

            boolean isheld = Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
            AutoJumpReset.legitJump();

            // release the jump key
            Timer timer = new Timer(20, (actionevent) -> {
                if (!isheld) KeyBinding.setKeyBindState(Main.getMc().gameSettings.keyBindJump.getKeyCode(), false);
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    // TOOD implement movement correction; aim assist flags Hypixel Watchdog simulation ac

//    // ---- Begin Movement Correction Below ----
//
//    // Correct player motion based on the rotated yaw
//    double radYaw = Math.toRadians(player.rotationYaw);
//    player.motionX = -Math.sin(radYaw) * player.movementInput.moveForward
//                       + Math.cos(radYaw) * player.movementInput.moveStrafe;
//    player.motionZ = Math.cos(radYaw) * player.movementInput.moveForward
//                       + Math.sin(radYaw) * player.movementInput.moveStrafe;
//
//    // Update position values based on motion (if needed for correction)
//    player.posX += player.motionX;
//    player.posZ += player.motionZ;
//
//    // Create corrected movement packet
//    C03PacketPlayer.C06PacketPlayerPosLook correctedPacket = new C03PacketPlayer.C06PacketPlayerPosLook(
//            player.posX,       // Corrected X position
//            player.posY,       // Current Y position remains the same
//            player.posZ,       // Corrected Z position
//            player.rotationYaw,   // Corrected Yaw
//            player.rotationPitch, // Corrected Pitch
//            player.onGround       // Correct on-ground state
//    );



//    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
//    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
//        if (packet instanceof C03PacketPlayer) {
//            // Intercept movement packets (Position, Look, PositionLook)
//            C03PacketPlayer c03Packet = (C03PacketPlayer) packet;
//
//            // Fetch the corrected rotation values
//            Minecraft mc = Minecraft.getMinecraft();
//            EntityPlayerSP player = mc.thePlayer;
//            if (player != null) {
//                // Retrieve corrected rotation and position values
//                float correctedYaw = player.rotationYaw;     // This should be the corrected yaw
//                float correctedPitch = player.rotationPitch; // This should be the corrected pitch
//                double correctedX = player.posX;             // Correct X
//                double correctedY = player.posY;             // Correct Y
//                double correctedZ = player.posZ;             // Correct Z
//
//                // Apply any needed adjustments or error correction logic
//                boolean onGround = player.onGround; // Reflect the correct on-ground state
//
//                // Create a corrected packet
//                C03PacketPlayer correctedPacket = new C03PacketPlayer.C06PacketPlayerPosLook(
//                        correctedX, correctedY, correctedZ,
//                        correctedYaw, correctedPitch, onGround
//                );
//
//                // Replace the original packet with the corrected packet
//                ci.cancel(); // Cancel the original packet
//                Minecraft.getMinecraft().getNetHandler().addToSendQueue(correctedPacket); // Send the modified packet
//            }
//        }
//    }
}