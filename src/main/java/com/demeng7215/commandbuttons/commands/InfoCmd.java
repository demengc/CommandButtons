package com.demeng7215.commandbuttons.commands;

import com.demeng7215.demlib.api.Common;
import com.demeng7215.demlib.api.CustomCommand;
import com.demeng7215.demlib.api.messages.MessageUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class InfoCmd extends CustomCommand {

	public InfoCmd() {
		super("commandbuttonsinfo");

		setDescription("Displays plugin information.");
		setAliases(Arrays.asList("cbinfo", "commandbuttoninfo", "cmdbuttonsinfo", "cmdbuttoninfo"));
	}

	@Override
	protected void run(CommandSender sender, String[] args) {
		MessageUtils.tellWithoutPrefix(sender, "&6Running CommandButtons v" + Common.getVersion() +
						" by Demeng7215.",
				"&7https://spigotmc.org/resources/60991/",
				"&6Type &7/cbhelp &6for a list of commands.");
	}
}
