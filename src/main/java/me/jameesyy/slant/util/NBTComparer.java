package me.jameesyy.slant.util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;

public class NBTComparer {

    private final Gson gson = new Gson();

    public boolean nbtMatch(NBTTagCompound nbt) {
        JsonObject expectedNBTJson = spireHealItemNbt();
        String expectedNBTString = gson.toJson(expectedNBTJson);

        String actualNBTString = nbt.toString();
        JsonObject actualNBTJson = gson.fromJson(actualNBTString, JsonObject.class);

        // Compare the actual NBT JSON with expected NBT JSON
        if (expectedNBTJson.equals(actualNBTJson)) {
            System.out.println("Item matches the expected NBT!");
            return true;
        } else {
            return false;
        }
    }

    private JsonObject spireHealItemNbt() {
        JsonObject json = new JsonObject();
        json.addProperty("id", "minecraft:magma_cream");

        JsonObject tag = new JsonObject();
        JsonObject display = new JsonObject();

        // Create Lore array
        display.add("Lore", gson.toJsonTree(new String[]{
                "§7Event item",
                "§9Resistance II (0:03)",
                "§74§c❤ §7heal",
                "§73§6❤ §7absorption"
        }));

        display.addProperty("Name", "§bFractured Soul");
        tag.add("display", display);

        JsonObject extraAttributes = new JsonObject();
        extraAttributes.addProperty("ITEM_MARKERS", "EVENT_ITEM");
        tag.add("ExtraAttributes", extraAttributes);

        json.add("tag", tag);
        json.addProperty("Damage", (short) 0);

        return json;
    }
}
