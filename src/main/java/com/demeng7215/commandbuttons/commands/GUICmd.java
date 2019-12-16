package com.demeng7215.commandbuttons.commands;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.commandbuttons.inventories.ButtonInv;
import com.demeng7215.demlib.api.CustomCommand;
import com.demeng7215.demlib.api.messages.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class GUICmd extends CustomCommand {

	private CommandButtons i;

	public GUICmd(CommandButtons i) {
		super("commandbuttonsgui");

		this.i = i;

		setDescription("Open the CommandButtons GUI.");
		setAliases(Arrays.asList("cbgui", "commandbuttongui", "cmdbuttonsgui", "cmdbuttongui",
				"cbinv", "commandbuttoninv", "cmdbuttonsinv", "cmdbuttoninv",
				"cbinventory", "commandbuttoninventory", "cmdbuttonsinventory", "cmdbuttoninventory"));
	}

	@Override
	protected void run(CommandSender sender, String[] args) {

		if (!checkIsPlayer(sender, i.getLang().getString("console"))) return;

		if (!checkHasPerm("commandbuttons.gui", sender, i.getLang().getString("no-perms"))) return;

		if (args.length < 1) {
			MessageUtils.tell(sender, i.getLang().getString("invalid-args"));
			return;
		}

		if (!i.getData().getKeys(false).contains(args[0])) {
			MessageUtils.tell(sender, i.getLang().getString("invalid-button-name"));
			return;
		}

		if (!sender.hasPermission("commandbuttons.gui." + args[0])) {
			MessageUtils.tell(sender, i.getLang().getString("no-perms"));
			return;
		}

		final Player p = (Player) sender;
		new ButtonInv(i, args[0]).open(p);
	}
}
