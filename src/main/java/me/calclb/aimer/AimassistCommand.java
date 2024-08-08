package me.calclb.aimer;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class AimassistCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "aimsettings";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "aimsettings <fov|maxspeed|acceleration|deceleration|randomness> <value>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || args.length > 2) {
            throw new CommandException("Invalid number of arguments. Usage: " + getCommandUsage(sender));
        }

        String s = args[0].toLowerCase();
        if (args.length == 1) {
            if (s.equals("maxspeed")) {
                sender.addChatMessage(new ChatComponentText("Max speed: " + AimAssist.getMaxSpeed()));
            } else if (s.equals("acceleration")) {
                sender.addChatMessage(new ChatComponentText("Acceleration: " + AimAssist.getAcceleration()));
            } else if (s.equals("deceleration")) {
                sender.addChatMessage(new ChatComponentText("Deceleration distance: " + AimAssist.getDecelerationDistance()));
            } else if (s.equals("randomness")) {
                sender.addChatMessage(new ChatComponentText("Base randomness: " + AimAssist.getBaseRandomness()));
            } else if (s.equals("fov")) {
                sender.addChatMessage(new ChatComponentText("FOV: " + AimAssist.getFov()));
            } else {
                throw new CommandException("Invalid parameter. Use fov, maxspeed, acceleration, deceleration, or randomness.");
            }
            return;
        }
        // invariant: args.length == 2

        float value;
        try {
            value = Float.parseFloat(args[1]);
        } catch (NumberFormatException e) {
            throw new CommandException("Invalid value. Must be a number.");
        }

        if (s.equals("maxspeed")) {
            AimAssist.setMaxSpeed(value);
            sender.addChatMessage(new ChatComponentText("Max speed set to " + AimAssist.getMaxSpeed()));
        } else if (s.equals("acceleration")) {
            AimAssist.setAcceleration(value);
            sender.addChatMessage(new ChatComponentText("Acceleration set to " + AimAssist.getAcceleration()));
        } else if (s.equals("deceleration")) {
            AimAssist.setDecelerationDistance(value);
            sender.addChatMessage(new ChatComponentText("Deceleration distance set to " + AimAssist.getDecelerationDistance()));
        } else if (s.equals("randomness")) {
            AimAssist.setBaseRandomness(value);
            sender.addChatMessage(new ChatComponentText("Base randomness set to " + AimAssist.getBaseRandomness()));
        } else if (s.equals("fov")) {
            AimAssist.setAimFov(value);
            sender.addChatMessage(new ChatComponentText("FOV " + AimAssist.getFov()));
        } else {
            throw new CommandException("Invalid parameter. Use fov, maxspeed, acceleration, deceleration, or randomness.");
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}