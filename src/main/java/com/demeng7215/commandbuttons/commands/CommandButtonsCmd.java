package com.demeng7215.commandbuttons.commands;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.demlib.api.CustomCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class CommandButtonsCmd extends CustomCommand {

    private CommandButtons i;

    public CommandButtonsCmd(CommandButtons i) {
        super("commandbuttons");

        this.i = i;

        setDescription("Main command for CommandButtons.");
        setAliases(Arrays.asList("cb", "commandbutton", "cmdbuttons", "cmdbutton"));
    }

    @Override
    protected void run(CommandSender sender, String[] args) {

        if (args.length < 1) {
            Bukkit.dispatchCommand(sender, "commandbuttonsinfo");
            return;
        }

        switch (args[0]) {

            case "help":
                Bukkit.dispatchCommand(sender, "commandbuttonshelp");
                return;

            case "create":
            case "add":
                if (!checkArgsStrict(args, 2, sender,
                        i.getLang().getString("invalid-args"))) return;
                Bukkit.dispatchCommand(sender, "commandbuttonscreate " + args[1]);
                return;

            case "reload":
                Bukkit.dispatchCommand(sender, "commandbuttonsreload");
                return;

            case "inv":
            case "gui":
            case "inventory":
                if (args.length == 2) {
                    Bukkit.dispatchCommand(sender, "commandbuttonsgui " + args[1]);
                    return;
                }
                Bukkit.dispatchCommand(sender, "commandbuttonsgui");
                return;

            default:
                Bukkit.dispatchCommand(sender, "commandbuttonsinfo");
        }
    }
}
