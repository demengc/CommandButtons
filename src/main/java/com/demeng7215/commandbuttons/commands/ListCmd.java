package com.demeng7215.commandbuttons.commands;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.demlib.api.CustomCommand;
import com.demeng7215.demlib.api.messages.MessageUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class ListCmd extends CustomCommand {

	private CommandButtons i;

	public ListCmd(CommandButtons i) {
		super("commandbuttonslist");

		this.i = i;

		setDescription("Displays a list of commands.");
		setAliases(Arrays.asList("cblist", "commandbuttonlist", "cmdbuttonslist", "cmdbuttonlist"));
	}

	@Override
	protected void run(CommandSender sender, String[] args) {

		MessageUtils.tellWithoutPrefix(sender, "&c&lList of Command Buttons:");

		if (i.getData().getKeys(false).isEmpty() ||
				(i.getData().getKeys(false).size() == 1 &&
						i.getData().getKeys(false).toArray()[0].equals("commandbuttonsdata"))) {
			MessageUtils.tellWithoutPrefix(sender, "&7None");
			return;
		}

		for (String buttonName : i.getData().getKeys(false))
			MessageUtils.tellWithoutPrefix(sender, "&7- &6" + buttonName);
	}
}
