package com.demeng7215.commandbuttons.commands;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.demlib.api.CustomCommand;
import com.demeng7215.demlib.api.messages.MessageUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class ReloadCmd extends CustomCommand {

	private CommandButtons i;

	public ReloadCmd(CommandButtons i) {
		super("commandbuttonsreload");

		this.i = i;

		setDescription("Reloads configuration files.");
		setAliases(Arrays.asList("cbreload", "commandbuttonreload", "cmdbuttonsreload", "cmdbuttonreload"));
	}

	@Override
	protected void run(CommandSender sender, String[] args) {

		if (!checkHasPerm("commandbuttons.reload", sender, i.getLang().getString("no-perms"))) return;

		i.language.reloadConfig();
		i.data.reloadConfig();

		MessageUtils.tell(sender, i.getLang().getString("reloaded"));
	}
}
