package com.demeng7215.commandbuttons.commands;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.demlib.api.CustomCommand;
import com.demeng7215.demlib.api.messages.MessageUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class HelpCmd extends CustomCommand {

	private CommandButtons i;

	public HelpCmd(CommandButtons i) {
		super("commandbuttonshelp");

		this.i = i;

		setDescription("Displays a list of commands.");
		setAliases(Arrays.asList("cbhelp", "commandbuttonhelp", "cmdbuttonshelp", "cmdbuttonhelp"));
	}

	@Override
	protected void run(CommandSender sender, String[] args) {
		for (String helpMsg : i.getLang().getStringList("help-menu")) {
			MessageUtils.tellWithoutPrefix(sender, helpMsg);
		}
	}
}
