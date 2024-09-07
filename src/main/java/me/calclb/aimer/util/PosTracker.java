package me.calclb.aimer.util;

import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class PosTracker<T extends Entity> {
    private static final Map<Entity, PosTracker<?>> trackers = new HashMap<Entity, PosTracker<?>>();

    private double lastX, lastY, lastZ;

    private PosTracker(T t) {
        updatePosTo(t);
    }

    public static <T extends Entity> PosTracker<T> getTracker(T entity) {
        @SuppressWarnings("unchecked")
        PosTracker<T> tracker = (PosTracker<T>) trackers.get(entity);
        if (tracker == null) {
            tracker = new PosTracker<T>(entity);
            trackers.put(entity, tracker);
        }
        return tracker;
    }

    public void updatePosTo(T t) {
        this.lastX = t.lastTickPosX;
        this.lastY = t.lastTickPosY;
        this.lastZ = t.lastTickPosZ;
    }

    public double getInterpolatedX(T t, float partialTicks) {
        return lastX + (t.posX - lastX) * partialTicks;
    }

    public double getInterpolatedY(T t, float partialTicks) {
        return lastY + (t.posY - lastY) * partialTicks;
    }

    public double getInterpolatedZ(T t, float partialTicks) {
        return lastZ + (t.posZ - lastZ) * partialTicks;
    }

    public static void clearTrackers() {
        trackers.clear();
    }
}