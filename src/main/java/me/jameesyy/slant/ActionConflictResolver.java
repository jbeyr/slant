package me.jameesyy.slant;

import me.jameesyy.slant.util.AutoGhead;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

public class ActionConflictResolver {

    private static boolean isActingOnPlayerBehalfAllowed() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer != null && mc.thePlayer.isEntityAlive() && mc.currentScreen == null && mc.inGameHasFocus;
    }

    public static boolean isGheadAllowed() {
        return isActingOnPlayerBehalfAllowed();
    }

    public static boolean isClickAllowed() {
        Minecraft mc = Minecraft.getMinecraft();
        return isActingOnPlayerBehalfAllowed() && !AutoGhead.isInProgress() && !mc.thePlayer.isUsingItem();
    }

    public static boolean isSneakingAllowed() {
        return isActingOnPlayerBehalfAllowed();
    }

    public static boolean isHotbarSelectedSlotChangeAllowed() {
        MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
        return isActingOnPlayerBehalfAllowed() && !AutoGhead.isInProgress() && mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK;
    }

    public static boolean isRotatingAllowed() {
        return isActingOnPlayerBehalfAllowed();
    }
}
