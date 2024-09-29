package me.calclb.aimer.commands;

import me.calclb.aimer.movement.AutoJumpReset;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class AutoJumpResetCommand extends CommandBase {
    public String getCommandName() {
        return "autojumpreset";
    }

    public void processCommand(ICommandSender sender, String[] args) {
        AutoJumpReset.toggle();
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Auto Jump Reset is " + (AutoJumpReset.isEnabled() ? "§aEnabled" : "§cDisabled")));
    }

    public String getCommandUsage(ICommandSender sender) {
        return "/autojumpreset";
    }

    public int getRequiredPermissionLevel() {
        return 0;
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
