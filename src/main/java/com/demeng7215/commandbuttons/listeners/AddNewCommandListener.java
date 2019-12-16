package com.demeng7215.commandbuttons.listeners;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.commandbuttons.inventories.CommandsManagerInv;
import com.demeng7215.demlib.api.messages.MessageUtils;
import com.demeng7215.demlib.api.titles.CustomTitle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.IOException;
import java.util.List;

public class AddNewCommandListener implements Listener {

	private CommandButtons i;

	public AddNewCommandListener(CommandButtons i) {
		this.i = i;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {

		if (!CommandsManagerInv.isAwaitingCommand()) return;
		if (e.getPlayer() != CommandsManagerInv.getAwaitingPlayer()) return;

		e.setCancelled(true);

		if (e.getMessage().equalsIgnoreCase("cancel")) {
			returnToGUI(e.getPlayer());
			return;
		}

		final String buttonName = CommandsManagerInv.getCurrentButtonName();
		final String commandType = CommandsManagerInv.getCurrentCommandType();

		if (i.getData().getStringList(buttonName + ".commands." + commandType).get(0).equals("none")) {
			final String[] firstCommand = new String[]{e.getMessage()};
			i.getData().set(buttonName + ".commands." + commandType, firstCommand);
		} else {
			List<String> currentCommands = i.getData().getStringList(buttonName + ".commands." + commandType);
			currentCommands.add(e.getMessage());
			i.getData().set(buttonName + ".commands." + commandType, currentCommands);
		}

		try {
			i.data.saveConfig();
		} catch (final IOException ex) {
			MessageUtils.error(ex, 5, "Failed to save data.", true);
			CommandsManagerInv.setAwaitingCommand(false);
			return;
		}

		CustomTitle.sendTitle(e.getPlayer(), "&4Command Added", "&6" + e.getMessage(),
				5, 20, 5);

		returnToGUI(e.getPlayer());
	}

	private void returnToGUI(Player p) {
		Bukkit.getScheduler().runTaskLater(i, () -> {
			boolean consoleCommand = false;
			if (CommandsManagerInv.getCurrentCommandType().equals("console")) consoleCommand = true;

			new CommandsManagerInv(i, CommandsManagerInv.getCurrentButtonName(),
					consoleCommand).open(p);

			CommandsManagerInv.setAwaitingCommand(false);
		}, 25L);
	}
}
