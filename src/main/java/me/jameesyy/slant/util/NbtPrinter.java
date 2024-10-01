package me.jameesyy.slant.util;

import me.jameesyy.slant.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NbtPrinter {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Main.getPrintNbtKey().isPressed()) {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            ItemStack heldItem = player.getHeldItem();

            if (heldItem == null) {
                player.addChatMessage(new ChatComponentText("You are not holding any item"));
                return;
            }

            NBTTagCompound nbtData = new NBTTagCompound();
            heldItem.writeToNBT(nbtData);

            String nbtString = nbtData.toString();
            GuiScreen.setClipboardString(nbtString);
            player.addChatMessage(new ChatComponentText("NBT data copied to clipboard!"));
        }
    }
}