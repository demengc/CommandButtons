package com.demeng7215.commandbuttons.listeners;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.commandbuttons.inventories.ButtonInv;
import com.demeng7215.demlib.api.messages.MessageUtils;
import com.demeng7215.demlib.api.titles.CustomTitle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.IOException;

public class SetNewCostListener implements Listener {

	private CommandButtons i;

	public SetNewCostListener(CommandButtons i) {
		this.i = i;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {

		if (!ButtonInv.isAwaitingCost()) return;
		if (e.getPlayer() != ButtonInv.getAwaitingPlayer()) return;

		e.setCancelled(true);

		if (e.getMessage().equalsIgnoreCase("cancel")) {
			returnToGUI(e.getPlayer());
			return;
		}

		final String buttonName = ButtonInv.getFinalButtonName();

		try {
			Double.parseDouble(e.getMessage());
		} catch (final Exception ex) {

			CustomTitle.sendTitle(e.getPlayer(), "&4Invalid Cost",
					"&6That's not a number.", 5, 20, 5);

			returnToGUI(e.getPlayer());
			return;
		}

		i.getData().set(buttonName + ".cost", Double.valueOf(e.getMessage()));

		try {
			i.data.saveConfig();
		} catch (final IOException ex) {
			MessageUtils.error(ex, 5, "Failed to save data.", true);
			ButtonInv.setAwaitingCost(false);
			return;
		}

		i.data.reloadConfig();

		ButtonInv.setAwaitingCost(false);

		CustomTitle.sendTitle(e.getPlayer(), "&4Cost Set", "&6" + e.getMessage(),
				5, 20, 5);


		returnToGUI(e.getPlayer());
	}

	private void returnToGUI(Player p) {
		Bukkit.getScheduler().runTaskLater(i, () -> new ButtonInv(i, ButtonInv.getFinalButtonName())
				.open(p), 25L);
		ButtonInv.setAwaitingCost(false);
	}
}
