package com.demeng7215.commandbuttons.listeners;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.commandbuttons.inventories.MessagesManagerInv;
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

public class AddNewMessageListener implements Listener {

	private CommandButtons i;

	public AddNewMessageListener(CommandButtons i) {
		this.i = i;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {

		if (!MessagesManagerInv.isAwaitingMessage()) return;
		if (e.getPlayer() != MessagesManagerInv.getAwaitingPlayer()) return;

		e.setCancelled(true);

		if (e.getMessage().equalsIgnoreCase("cancel")) {
			returnToGUI(e.getPlayer());
			return;
		}

		final String buttonName = MessagesManagerInv.getCurrentButtonName();

		if (i.getData().getStringList(buttonName + ".messages").get(0).equals("none")) {
			final String[] firstMessage = new String[]{e.getMessage()};
			i.getData().set(buttonName + ".messages", firstMessage);
		} else {
			List<String> currentMessages = i.getData().getStringList(buttonName + ".messages");
			currentMessages.add(e.getMessage());
			i.getData().set(buttonName + ".messages", currentMessages);
		}

		try {
			i.data.saveConfig();
		} catch (final IOException ex) {
			MessageUtils.error(ex, 5, "Failed to save data.", true);
			MessagesManagerInv.setAwaitingMessage(false);
			return;
		}

		CustomTitle.sendTitle(e.getPlayer(), "&4Message Added", "&6" + e.getMessage(),
				5, 20, 5);

		returnToGUI(e.getPlayer());
	}

	private void returnToGUI(Player p) {
		Bukkit.getScheduler().runTaskLater(i, () -> {
			new MessagesManagerInv(i, MessagesManagerInv.getCurrentButtonName()).open(p);
			MessagesManagerInv.setAwaitingMessage(false);
		}, 25L);
	}
}
