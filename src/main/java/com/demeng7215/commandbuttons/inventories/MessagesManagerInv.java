package com.demeng7215.commandbuttons.inventories;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.demlib.api.gui.CustomInventory;
import com.demeng7215.demlib.api.messages.MessageUtils;
import com.demeng7215.demlib.api.titles.CustomTitle;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessagesManagerInv extends CustomInventory {

	private CommandButtons i;
	private List<String> currentMessagesLore = new ArrayList<>();

	@Getter
	@Setter
	private static boolean awaitingMessage;

	@Getter
	@Setter
	private static Player awaitingPlayer;

	@Getter
	private static String currentButtonName;

	public MessagesManagerInv(CommandButtons i, String buttonName) {
		super(36, "&4Edit Your Messages");

		this.i = i;

		currentButtonName = buttonName;

		setItem(11, new ItemStack(Material.EMERALD_BLOCK), "&cAdd Message", Collections.singletonList(
				"&6Add a message that should be sent."), player -> {

			player.closeInventory();

			CustomTitle.sendTitle(player, "&4Type New Message in Chat",
					"&6See chat for more instructions.", 5, 60, 5);

			MessageUtils.tellWithoutPrefix(player, "&c" + MessageUtils.chatLine(),
					"&6Enter the new message in chat.",
					"&6Type &7cancel &6to cancel.",
					"&6Use &7%player% &6for the presser's name.",
					"&c" + MessageUtils.chatLine());

			setAwaitingPlayer(player);
			setAwaitingMessage(true);
		});

		Bukkit.getScheduler().runTaskTimerAsynchronously(i, () -> {
			refresh(buttonName);
			setItem(13, new ItemStack(Material.OBSIDIAN), "&c&lCurrent Messages", currentMessagesLore,
					Player::updateInventory);
		}, 0L, 35L);

		setItem(15, new ItemStack(Material.REDSTONE_BLOCK), "&cClear Messages", Collections.singletonList(
				"&6Delete all messages for this button."), player -> {
			i.getData().set(buttonName + ".messages", Collections.singletonList("none"));
			try {
				i.data.saveConfig();
			} catch (final IOException ex) {
				MessageUtils.error(ex, 4, "Failed to save data.", true);
			}
		});

		setItem(35, new ItemStack(Material.ARROW), "&c&lBack", Collections.singletonList(
				"&7Return to the previous menu."), new ButtonInv(i, buttonName)::open);

	}

	private void refresh(String buttonName) {
		this.currentMessagesLore.clear();
		this.currentMessagesLore.add("&6Current messages are listed below.");

		for (String message : i.getData().getStringList(buttonName + ".messages"))
			this.currentMessagesLore.add("&6- &7" + message);
	}
}
