package me.jameesyy.slant.util;

import me.jameesyy.slant.ActionConflictResolver;
import me.jameesyy.slant.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Optional;

public class AutoGhead {

    public static final String SKULLOWNER_TEXTURE = "eyJ0aW1lc3RhbXAiOjE0ODUwMjM0NDEyNzAsInByb2ZpbGVJZCI6ImRhNDk4YWM0ZTkzNzRlNWNiNjEyN2IzODA4NTU3OTgzIiwicHJvZmlsZU5hbWUiOiJOaXRyb2hvbGljXzIiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y5MzdlMWM0NWJiOGRhMjliMmM1NjRkZDlhN2RhNzgwZGQyZmU1NDQ2OGE1ZGZiNDExM2I0ZmY2NThmMDQzZTEifX19";

    private static boolean enabled;
    private static float healthThreshold;

    private static long inProgressUntil = 0;
    private static long cooldownUntil = 0;


    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final long IN_PROGRESS_DURATION = 50;

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

    public static void setInProgress(int cooldownMs) {
        inProgressUntil = System.currentTimeMillis() + IN_PROGRESS_DURATION;
        cooldownUntil = System.currentTimeMillis() + cooldownMs;
    }

    private static boolean isGhead(ItemStack item) {
        if (item == null) return false;

        NBTTagCompound nbt = item.getTagCompound();
        if (nbt == null || !nbt.hasKey("SkullOwner", Constants.NBT.TAG_COMPOUND)) {
            return false;
        }

        NBTTagCompound skullOwner = nbt.getCompoundTag("SkullOwner");
        if (!skullOwner.hasKey("Properties", Constants.NBT.TAG_COMPOUND)) {
            return false;
        }

        NBTTagCompound properties = skullOwner.getCompoundTag("Properties");
        if (!properties.hasKey("textures", Constants.NBT.TAG_LIST)) {
            return false;
        }

        NBTTagList textures = properties.getTagList("textures", Constants.NBT.TAG_COMPOUND);
        if (textures.tagCount() == 0) return false;

        NBTTagCompound textureEntry = textures.getCompoundTagAt(0);
        if (!textureEntry.hasKey("Value", Constants.NBT.TAG_STRING)) {
            return false;
        }

        String textureValue = textureEntry.getString("Value");
        return textureValue.contains(SKULLOWNER_TEXTURE);
    }

    public static void setHealthThreshold(float ratio) {
        AutoGhead.healthThreshold = ratio;
        ModConfig.autoGheadHealthThreshold = ratio;
        Reporter.reportSet("Auto Ghead", "Health Threshold", ratio);
    }

    private int swappedFrom = 0;
    private boolean needToSwapBack = false;

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

        if (System.currentTimeMillis() < cooldownUntil) return; // still on cooldown, so don't use another ghead
        if (me.getAbsorptionAmount() > 1f || me.getHealth() / me.getMaxHealth() > healthThreshold) return;

        swappedFrom = me.inventory.currentItem;
        Optional<NBTComparer.HealingItem> slot = NBTComparer.getFirstHealingItemInHotbar(me);
        if (!slot.isPresent()) return;

        setInProgress(slot.get().cooldownMs);
        me.inventory.currentItem = slot.get().slot;

        // swap to item and use it
        int useItemKey = mc.gameSettings.keyBindUseItem.getKeyCode();
        KeyBinding.setKeyBindState(useItemKey, true);
        KeyBinding.onTick(useItemKey);
        KeyBinding.setKeyBindState(useItemKey, false);

        // Schedule swap back for next tick
        needToSwapBack = true;
    }
}