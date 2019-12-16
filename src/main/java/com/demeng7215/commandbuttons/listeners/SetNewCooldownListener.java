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

public class SetNewCooldownListener implements Listener {

	private CommandButtons i;

	public SetNewCooldownListener(CommandButtons i) {
		this.i = i;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {

		if (!ButtonInv.isAwaitingCooldown()) return;
		if (e.getPlayer() != ButtonInv.getAwaitingPlayer()) return;

		e.setCancelled(true);

		if (e.getMessage().equalsIgnoreCase("cancel")) {
			returnToGUI(e.getPlayer());
			return;
		}

		final String buttonName = ButtonInv.getFinalButtonName();

		try {
			Integer.parseInt(e.getMessage());
		} catch (final Exception ex) {

			CustomTitle.sendTitle(e.getPlayer(), "&4Invalid Cooldowm",
					"&6Not a valid number.", 5, 20, 5);

			returnToGUI(e.getPlayer());
			return;
		}

		i.getData().set(buttonName + ".cooldown", Integer.valueOf(e.getMessage()));

		try {
			i.data.saveConfig();
		} catch (final IOException ex) {
			MessageUtils.error(ex, 5, "Failed to save data.", true);
			ButtonInv.setAwaitingCooldown(false);
			return;
		}

		CustomTitle.sendTitle(e.getPlayer(), "&4Cooldown Set", "&6" + e.getMessage(),
				5, 20, 5);

		returnToGUI(e.getPlayer());
	}

	private void returnToGUI(Player p) {
		Bukkit.getScheduler().runTaskLater(i, () -> new ButtonInv(i, ButtonInv.getFinalButtonName())
				.open(p), 25L);
		ButtonInv.setAwaitingCooldown(false);
	}
}
