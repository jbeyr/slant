package me.calclb.aimer.commands;

import me.calclb.aimer.movement.NoJumpDelay;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class NoJumpDelayCommand extends CommandBase {
    public String getCommandName() {
        return "nojumpdelay";
    }

    public void processCommand(ICommandSender sender, String[] args) {
        NoJumpDelay.toggle();
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("No Jump Delay is "+(NoJumpDelay.isEnabled() ? "§aEnabled" : "§cDisabled")));
    }

    public String getCommandUsage(ICommandSender sender) {
        return "/nojumpdelay";
    }

    public int getRequiredPermissionLevel() {
        return 0;
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
