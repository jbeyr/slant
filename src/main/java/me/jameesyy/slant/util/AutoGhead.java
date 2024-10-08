package me.jameesyy.slant.util;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static me.jameesyy.slant.util.NbtComparer.overlappingNbt;

public class AutoGhead {

    private static final Map<NbtComparer.HealingItem, Long> individualCooldowns = new HashMap<>();
    private static final long IN_PROGRESS_DURATION = 50;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean enabled;
    private static long inProgressUntil = 0;
    private int swappedFrom = 0;
    private boolean needToSwapBack = false;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean b) {
        AutoGhead.enabled = b;
        ModConfig.autoGheadEnabled = b;
        Reporter.reportToggled("Auto Ghead", b);
    }

    public static boolean isInProgress() {
        return System.currentTimeMillis() < inProgressUntil;
    }

    public static void setInProgress(NbtComparer.HealingItem healingItem) {
        inProgressUntil = System.currentTimeMillis() + IN_PROGRESS_DURATION;
        individualCooldowns.put(healingItem, System.currentTimeMillis() + healingItem.cooldownMs);
    }


    /**
     * @param player the player whose hotbar should be scanned for a suitable item
     * @return the healing item if (1) the player health threshold is below it, (2) if the item isn't on cooldown, and (3) if the nbt matches
     */
    public static Optional<NbtComparer.HealingItemResult> getFirstHealingItemInHotbarNotOnCooldown(EntityPlayer player) {

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            for (NbtComparer.HealingItem hItem : NbtComparer.getItemCooldowns()) {
                if (!overlappingNbt(stack, hItem.sourceNbt)) continue;
                if (player.getAbsorptionAmount() > 1f || player.getHealth() / player.getMaxHealth() > hItem.usageThreshold) continue;

                Long healingItemCooldown = individualCooldowns.getOrDefault(hItem, 0L);
                if(System.currentTimeMillis() > healingItemCooldown) {
                    return Optional.of(new NbtComparer.HealingItemResult(hItem, i));
                }
            }
        }
        return Optional.empty();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!enabled) return;
        if (!ActionConflictResolver.isGheadAllowed()) return;
        if (event.phase != TickEvent.Phase.START) return;

        EntityPlayer me = mc.thePlayer;

        if (needToSwapBack) {
            me.inventory.currentItem = swappedFrom;
            needToSwapBack = false;
            return;
        }

        Optional<NbtComparer.HealingItemResult> res = getFirstHealingItemInHotbarNotOnCooldown(me);
        if (!res.isPresent()) return;

        swappedFrom = me.inventory.currentItem;

        setInProgress(res.get().hItem);
        me.inventory.currentItem = res.get().slot;

        // swap to item and use it
        int useItemKey = mc.gameSettings.keyBindUseItem.getKeyCode();
        KeyBinding.setKeyBindState(useItemKey, true);
        KeyBinding.onTick(useItemKey);
        KeyBinding.setKeyBindState(useItemKey, false);

        // Schedule swap back for next tick
        needToSwapBack = true;
    }
}