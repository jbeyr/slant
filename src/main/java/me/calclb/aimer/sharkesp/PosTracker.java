package me.calclb.aimer.sharkesp;

import net.minecraft.entity.Entity;

public class PosTracker<T extends Entity> {
    private double lastX, lastY, lastZ;

    public PosTracker(T t) {
        updatePosTo(t);
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
}