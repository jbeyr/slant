package me.jameesyy.slant.util;

import gg.essential.universal.ChatColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Reporter {

    private static final int MOD_CHAT_LINE_IDENTIFIER = 0xA5000000;
    private static final int MAX_QUEUED_MESSAGES = 50;
    private static final Queue<QueuedMessage> messageQueue = new LinkedList<QueuedMessage>();
    private static boolean isInitialized = false;

    private static class QueuedMessage {
        String moduleName;
        String settingName;
        IChatComponent message;

        QueuedMessage(String moduleName, String settingName, IChatComponent message) {
            this.moduleName = moduleName;
            this.settingName = settingName;
            this.message = message;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !isInitialized) {
            if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null) {
                isInitialized = true;
                processQueuedMessages();
            }
        }
    }

    private static int generateMessageId(String moduleName, String settingName) {
        String combinedName = moduleName + ":" + settingName;
        int hash = combinedName.hashCode() & 0x00FFFFFF;
        return MOD_CHAT_LINE_IDENTIFIER | hash;
    }

    private static void sendMessage(String moduleName, String settingName, IChatComponent cc) {
        if (!isInitialized) {
            queueMessage(moduleName, settingName, cc);
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.ingameGUI == null || mc.ingameGUI.getChatGUI() == null) {
            queueMessage(moduleName, settingName, cc);
            return;
        }

        GuiNewChat chatGui = mc.ingameGUI.getChatGUI();
        int messageId = generateMessageId(moduleName, settingName);

        deletePreviousMessages(chatGui, messageId);
        chatGui.printChatMessageWithOptionalDeletion(cc, messageId);
    }

    private static void queueMessage(String moduleName, String settingName, IChatComponent cc) {
        messageQueue.offer(new QueuedMessage(moduleName, settingName, cc));
        if (messageQueue.size() > MAX_QUEUED_MESSAGES) {
            messageQueue.poll();
        }
    }

    private static void processQueuedMessages() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.ingameGUI == null || mc.ingameGUI.getChatGUI() == null) {
            return;
        }

        GuiNewChat chatGui = mc.ingameGUI.getChatGUI();
        while (!messageQueue.isEmpty()) {
            QueuedMessage qm = messageQueue.poll();
            int messageId = generateMessageId(qm.moduleName, qm.settingName);
            deletePreviousMessages(chatGui, messageId);
            chatGui.printChatMessageWithOptionalDeletion(qm.message, messageId);
        }
    }

    private static void deletePreviousMessages(GuiNewChat chatGui, int messageId) {
        List<ChatLine> chatLines = ReflectionHelper.getPrivateValue(GuiNewChat.class, chatGui, "field_146253_i", "chatLines");
        if (chatLines != null) {
            chatLines.removeIf(chatLine -> (chatLine.getChatLineID() & 0xFF000000) == MOD_CHAT_LINE_IDENTIFIER
                    && (chatLine.getChatLineID() & 0x00FFFFFF) == (messageId & 0x00FFFFFF));
        }

        List<ChatLine> drawnChatLines = ReflectionHelper.getPrivateValue(GuiNewChat.class, chatGui, "field_146252_h", "drawnChatLines");
        if (drawnChatLines != null) {
            drawnChatLines.removeIf(chatLine -> (chatLine.getChatLineID() & 0xFF000000) == MOD_CHAT_LINE_IDENTIFIER
                    && (chatLine.getChatLineID() & 0x00FFFFFF) == (messageId & 0x00FFFFFF));
        }
    }

    public static void queueEnableMsg(String moduleName, boolean to) {
        IChatComponent cc = new ChatComponentText(ChatColor.ITALIC + "" + ChatColor.DARK_GRAY + "slant" + ChatColor.DARK_GRAY + " ⁄  " + ChatColor.RESET + moduleName + " " + (to ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF") + ChatColor.RESET);
        sendMessage(moduleName, "toggle", cc);
    }

    public static void queueSetMsg(String moduleName, String settingName, Object to) {
        IChatComponent cc = new ChatComponentText(ChatColor.ITALIC + "" + ChatColor.DARK_GRAY + "slant" + ChatColor.DARK_GRAY + " ⁄  " + ChatColor.RESET + moduleName + " " + ChatColor.DARK_AQUA + settingName + " " + ChatColor.AQUA + to.toString() + ChatColor.RESET);
        sendMessage(moduleName, settingName, cc);
    }

    public static void msg(String msg) {
        IChatComponent cc = new ChatComponentText(ChatColor.ITALIC + "" + ChatColor.DARK_GRAY + "slant" + ChatColor.DARK_GRAY + " ⁄  " + ChatColor.RESET + msg);
        Minecraft.getMinecraft().thePlayer.addChatMessage(cc);
    }
}