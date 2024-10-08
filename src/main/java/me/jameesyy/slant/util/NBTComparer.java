package me.jameesyy.slant.util;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NBTComparer {

    private static final Map<String, Integer> itemCooldowns = new HashMap<>();
    private static final String FIRST_AID_EGG_JSON = "{id:\"minecraft:spawn_egg\",Count:1b,tag:{display:{Lore:[0:\"§eSpecial item\",1:\"§7Heals §c2.5❤\",2:\"§710 seconds cooldown.\"],Name:\"§cFirst-Aid Egg\"},ExtraAttributes:{ITEM_MARKERS:\"CANNOT_DROP\"}},Damage:96s}";
    private static final String FRACTURED_SOUL_JSON = "{id:\"minecraft:magma_cream\",Count:1b,tag:{display:{Lore:[\"§7Event item\",\"§9Resistance II (0:03)\",\"§74§c❤ §7heal\",\"§73§6❤ §7absorption\"],Name:\"§bFractured Soul\"},ExtraAttributes:{ITEM_MARKERS:\"EVENT_ITEM\"}},Damage:0s}";
    private static final String GOLDEN_HEAD_JSON_PARTIAL = "eyJ0aW1lc3RhbXAiOjE0ODUwMjM0NDEyNzAsInByb2ZpbGVJZCI6ImRhNDk4YWM0ZTkzNzRlNWNiNjEyN2IzODA4NTU3OTgzIiwicHJvZmlsZU5hbWUiOiJOaXRyb2hvbGljXzIiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y5MzdlMWM0NWJiOGRhMjliMmM1NjRkZDlhN2RhNzgwZGQyZmU1NDQ2OGE1ZGZiNDExM2I0ZmY2NThmMDQzZTEifX19";

    static {
        itemCooldowns.put(FIRST_AID_EGG_JSON, 200);
        itemCooldowns.put(FRACTURED_SOUL_JSON, 200);
        itemCooldowns.put(GOLDEN_HEAD_JSON_PARTIAL, 200);
    }

    public static boolean exactlyNbtString(ItemStack item, String nbtStr) {
        if (item != null && item.hasTagCompound()) {
            JsonParser parser = new JsonParser();
            String nbtJson = item.writeToNBT(new NBTTagCompound()).toString();
            JsonElement expectedJson = parser.parse(nbtStr);
            JsonElement actualJson = parser.parse(nbtJson);
            return expectedJson.equals(actualJson);
        }
        return false;
    }

    /**
     * @return true if <code>nbtStr</code> is contained in the item, or vice versa.
     */
    public static boolean overlappingNbt(ItemStack item, String nbtStr) {
        if (item != null && item.hasTagCompound()) {
            JsonParser parser = new JsonParser();
            String itemNbtJson = item.writeToNBT(new NBTTagCompound()).toString();
            String s1, s2;

            try {
                s1 = parser.parse(nbtStr).toString();
            } catch (JsonSyntaxException e) { // If parsing fails, assume it's the Base64 encoded string
                s1 = nbtStr;
            }

            try {
                s2 = parser.parse(itemNbtJson).toString();
            } catch (JsonSyntaxException e) { // If parsing fails, use the raw string
                s2 = itemNbtJson;
            }

            return s1.contains(s2) || s2.contains(s1);
        }
        return false;
    }

    public static class HealingItem {
        public final int slot;
        public final int cooldownMs;

        public HealingItem(int slot, int cooldownMs) {
            this.slot = slot;
            this.cooldownMs = cooldownMs;
        }
    }

    public static Optional<HealingItem> getFirstHealingItemInHotbar(EntityPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            for(Map.Entry<String, Integer> ent : itemCooldowns.entrySet()) {
                if (overlappingNbt(stack, ent.getKey())) return Optional.of(new HealingItem(i, ent.getValue()));
            }
        }
        return Optional.empty();
    }
}
