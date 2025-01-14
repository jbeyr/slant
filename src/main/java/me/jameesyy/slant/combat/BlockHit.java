package me.jameesyy.slant.combat;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.util.CoolDown;
import me.jameesyy.slant.util.Reporter;
import me.jameesyy.slant.util.Robo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.ThreadLocalRandom;

public class BlockHit {

    // FIXME not triggering blockhits if enabled (it had one job but I don't have time to deal w/ this atm)
    private static boolean enabled = false;

    private static float range = 3f;
    private static float chance = 1f;
    private static boolean onlyPlayers = true;
    private static boolean onlyForward = true;
    private static float waitMsMin = 37f, waitMsMax = 59f;
    private static float actionMsMin = 12f, actionMsMax = 41f;
    private static float hitPerMin = 1, hitPerMax = 1;

    private static boolean executingAction;
    private static int hits, rHit;
    private static boolean call, tryStartCombo;
    private final CoolDown actionTimer = new CoolDown(0);
    private final CoolDown waitTimer = new CoolDown(0);

    public static void setEnabled(boolean b) {
        BlockHit.enabled = b;
//        ModConfig.blockhitEnabled = b;
        Reporter.queueEnableMsg("Blockhit", b);
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        if(!enabled) return;
        if (!ActionConflictResolver.isActingOnPlayerBehalfAllowed()) return;

        if (tryStartCombo && waitTimer.hasFinished()) {
            tryStartCombo = false;
            startCombo();
        }

        if (actionTimer.hasFinished() && executingAction) finishCombo();
    }

    @SubscribeEvent
    public void onHit(AttackEntityEvent fe) {
        if (!enabled) return;
        if (isSecondCall() || executingAction) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer me = mc.thePlayer;
        hits++;

        if (hits > rHit) {
            hits = 1;
            int eaSports = (int) (hitPerMax - hitPerMin + 1);
            rHit = ThreadLocalRandom.current().nextInt((eaSports));
            rHit += (int) hitPerMin;
        }

        ItemStack heldItem = me.getCurrentEquippedItem();

        if (    (!(fe.target instanceof EntityPlayer) && onlyPlayers)
                || !(Math.random() <= chance / 100)
                || !(heldItem != null && heldItem.getItem() instanceof ItemSword)
                || mc.thePlayer.getDistanceToEntity(fe.target) > range
                || !(rHit == hits))
            return;

        tryStartCombo();
    }

    private void finishCombo() {
        Minecraft mc = Minecraft.getMinecraft();
        executingAction = false;
        int key = mc.gameSettings.keyBindUseItem.getKeyCode();
        KeyBinding.setKeyBindState(key, false);
        Robo.setMouseButtonState(1, false);
    }

    private void startCombo() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) && onlyForward) return;

        executingAction = true;
        int useItemKey = mc.gameSettings.keyBindUseItem.getKeyCode();
        KeyBinding.setKeyBindState(useItemKey, true);
        KeyBinding.onTick(useItemKey);
        Robo.setMouseButtonState(1, true);
        actionTimer.setCooldown((long) ThreadLocalRandom.current().nextDouble(waitMsMin, waitMsMax + 0.01));
        actionTimer.start();
    }

    public void tryStartCombo() {
        tryStartCombo = true;
        waitTimer.setCooldown((long) ThreadLocalRandom.current().nextDouble(actionMsMin, actionMsMax + 0.01));
        waitTimer.start();
    }

    private static boolean isSecondCall() {
        if (call) {
            call = false;
            return true;
        } else {
            call = true;
            return false;
        }
    }
}