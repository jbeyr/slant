package me.jameesyy.slant;

import me.jameesyy.slant.util.AntiBot;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

import static net.minecraftforge.fml.common.gameevent.TickEvent.*;

public class Targeter {

    private static Optional<EntityPlayer> target = Optional.empty();
    private static long timeSet;
    private static final List<TargetChangeListener> listeners = new ArrayList<>();
    private static final Map<UUID, Long> hurtTickedTargets = new HashMap<>();
    private static long activeTick = 0;

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
        hurtTickedTargets.put(e.target.getUniqueID(), activeTick);

        if (e.target instanceof EntityPlayer && AntiBot.isRecommendedTarget((EntityLivingBase)e.target)) {
            Targeter.setTarget((EntityPlayer) e.target);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != Phase.END) return;
        activeTick += 1;

        if (!target.isPresent()) return;

        EntityPlayer me = Minecraft.getMinecraft().thePlayer;
        EntityPlayer ten = target.get();
        if (!ten.isEntityAlive() || ten.getDistanceSqToEntity(me) > 64f || timeSet >= System.currentTimeMillis() + (10 * 1000)) {
            clearTarget();
        }
    }

    @SubscribeEvent
    public void onMeJoin(EntityJoinWorldEvent e) {
        Entity me = Minecraft.getMinecraft().thePlayer;
        if(e.entity != me) return;
        activeTick = 0;
        hurtTickedTargets.clear();
    }

    public static boolean hasHurtTicks(UUID entityUuid) {
        return activeTick <= hurtTickedTargets.getOrDefault(entityUuid, 0L) + 10;
    }

    public interface TargetChangeListener {
        void onTargetChange(Optional<EntityPlayer> oldTarget, Optional<EntityPlayer> newTarget);
    }
}