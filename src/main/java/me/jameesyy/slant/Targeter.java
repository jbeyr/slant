package me.jameesyy.slant;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Targeter {

    private static Optional<EntityPlayer> target = Optional.empty();
    private static long timeSet;
    private static final List<TargetChangeListener> listeners = new ArrayList<>();

    public static Optional<EntityPlayer> getTarget() {
        return target;
    }

    public static void setTarget(EntityPlayer newTarget) {
        Optional<EntityPlayer> oldTarget = target;
        target = Optional.of(newTarget);
        timeSet = System.currentTimeMillis();
        notifyListeners(oldTarget, target);
    }

    public static void clearTarget() {
        Optional<EntityPlayer> oldTarget = target;
        target = Optional.empty();
        notifyListeners(oldTarget, Optional.empty());
    }

    public static void addListener(TargetChangeListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(TargetChangeListener listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners(Optional<EntityPlayer> oldTarget, Optional<EntityPlayer> newTarget) {
        for (TargetChangeListener listener : listeners) {
            listener.onTargetChange(oldTarget, newTarget);
        }
    }

    @SubscribeEvent
    public void onPlayerAttackedByMe(AttackEntityEvent e) {
        if (e.entityPlayer != Minecraft.getMinecraft().thePlayer) return;

        if (e.target instanceof EntityPlayer) {
            Targeter.setTarget((EntityPlayer) e.target);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!target.isPresent()) return;

        EntityPlayer me = Minecraft.getMinecraft().thePlayer;
        if (target.get().getDistanceSqToEntity(me) > 36f && timeSet >= System.currentTimeMillis() + (10 * 1000)) {
            clearTarget();
        }
    }

    public interface TargetChangeListener {
        void onTargetChange(Optional<EntityPlayer> oldTarget, Optional<EntityPlayer> newTarget);
    }
}