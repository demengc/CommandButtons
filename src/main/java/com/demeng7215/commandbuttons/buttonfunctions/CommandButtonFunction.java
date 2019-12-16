package com.demeng7215.commandbuttons.buttonfunctions;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.demlib.api.messages.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.HashMap;

public class CommandButtonFunction {

	public static HashMap<String, Double> timeouts = new HashMap<>();

	public CommandButtonFunction(CommandButtons i, Location buttonLocation, Player p) {

		final String parsedLocation = buttonLocation.getWorld().getName() + "." +
				buttonLocation.getBlockX() + "." +
				buttonLocation.getBlockY() + "." +
				buttonLocation.getBlockZ();

		for (String path : i.getData().getKeys(false)) {

			if (i.getData().getString(path + ".location") != null &&
					i.getData().getString(path + ".location").equals(parsedLocation)) {

				final String permission = i.getData().getString(path + ".permission");
				if (!permission.equals("none") && !p.hasPermission(permission)) {
					MessageUtils.tell(p, i.getLang().getString("no-perms"));
					return;
				}

				if (timeouts.containsKey(path)) {

					DecimalFormat seconds = new DecimalFormat("#.#");

					MessageUtils.tell(p, i.getLang().getString("cooldown-wait")
							.replace("%seconds%", seconds.format(timeouts.get(path) / 20)));
					return;
				}

				if (!CommandButtons.getEconomy().has(p, i.getData().getDouble(path + ".cost"))) {
					MessageUtils.tell(p, i.getLang().getString("insufficient-funds")
							.replace("%cost%", String.valueOf(i.getData().getDouble(path + ".cost"))));
					return;
				}

				CommandButtons.getEconomy().withdrawPlayer(p, i.getData().getDouble(path + ".cost"));

				if (!i.getData().getStringList(path + ".commands.console").contains("none"))
					for (String cmd : i.getData().getStringList(path + ".commands.console"))
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()));

				if (!i.getData().getStringList(path + ".commands.player").contains("none"))
					for (String cmd : i.getData().getStringList(path + ".commands.player"))
						Bukkit.dispatchCommand(p, cmd.replace("%player%", p.getName()));

				if (!i.getData().getStringList(path + ".messages").contains("none"))
					for (String msg : i.getData().getStringList(path + ".messages"))
						MessageUtils.tellWithoutPrefix(p, msg.replace("%player%", p.getName()));

				timeouts.put(path, i.getData().getDouble(path + ".cooldown") * 20);
			}
		}
	}
}
