package com.demeng7215.commandbuttons.commands;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.commandbuttons.inventories.ButtonInv;
import com.demeng7215.demlib.api.CustomCommand;
import com.demeng7215.demlib.api.messages.MessageUtils;
import com.demeng7215.demlib.api.titles.CustomTitle;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.demeng7215.demlib.api.titles.CustomTitle.sendTitle;

public class CreateCmd extends CustomCommand {

	private CommandButtons i;

	public CreateCmd(CommandButtons i) {
		super("commandbuttonscreate");

		this.i = i;

		setDescription("Creates a new Command Button.");
		setAliases(Arrays.asList("cbcreate", "commandbuttoncreate", "cmdbuttonscreate", "cmdbuttoncreate",
				"cbadd", "commandbuttonadd", "cmdbuttonsadd", "cmdbuttonadd", "createcommandbutton",
				"createcb", "createcmdbutton", "addcommandbutton", "addcb", "addcmdbutton"));
	}

	@Override
	protected void run(CommandSender sender, String[] args) {

		if (!checkIsPlayer(sender, i.getLang().getString("console"))) return;

		if (!checkHasPerm("commandbuttons.create", sender, i.getLang().getString("no-perms"))) return;

		if (!checkArgsStrict(args, 1, sender, i.getLang().getString("invalid-args"))) return;

		final Player p = (Player) sender;

		final Block targetBlock = p.getTargetBlock(null, 5);

		if (!targetBlock.getType().toString().endsWith("_BUTTON") &&
				!targetBlock.getType().toString().endsWith("_PLATE") &&
				!targetBlock.getType().toString().contains("SIGN")) {
			MessageUtils.tell(p, i.getLang().getString("not-button"));
			return;
		}

		final String parsedLocation = targetBlock.getLocation().getWorld().getName() + "." +
				targetBlock.getLocation().getBlockX() + "." +
				targetBlock.getLocation().getBlockY() + "." +
				targetBlock.getLocation().getBlockZ();

		if (i.getData().getConfigurationSection(args[0]) != null || i.getData().contains(parsedLocation)) {
			MessageUtils.tell(p, i.getLang().getString("already-exists"));
			return;
		}

		sendTitle(p, "&4Creating Command Button...", "&6" + args[0],
				5, 50, 5);

		i.getData().set(args[0] + ".location", parsedLocation);
		i.getData().set(args[0] + ".commands.console", Collections.singletonList("say Hello! My name " +
				"is Console, and you just pressed a Command Button!"));
		i.getData().set(args[0] + ".commands.player", Arrays.asList("me just pressed a Command Button!",
				"give %player% dirt 3"));
		i.getData().set(args[0] + ".cost", 10D);
		i.getData().set(args[0] + ".permission", "commandbuttons.custompermission");
		i.getData().set(args[0] + ".messages", Collections.singletonList("You got 3 dirt for using a Command Button."));
		i.getData().set(args[0] + ".cooldown", 3L);

		try {
			i.data.saveConfig();
		} catch (final IOException ex) {
			MessageUtils.error(ex, 4, "Failed to save data.", true);
			return;
		}

		Bukkit.getScheduler().runTaskLater(i, () -> {
			CustomTitle.sendTitle(p, "&6" + args[0] + " Created Successfully", "&7Opening GUI...",
					5, 50, 5);
			Bukkit.getScheduler().runTaskLater(i, () -> new ButtonInv(i, args[0]).open(p), 60L);
		}, 55L);
	}
}
