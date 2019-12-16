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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ButtonInv extends CustomInventory {

	@Getter
	private static String finalButtonName;

	@Getter
	@Setter
	private static boolean awaitingCost, awaitingCooldown, awaitingPermission;

	@Getter
	@Setter
	private static Player awaitingPlayer;

	public ButtonInv(CommandButtons i, String buttonName) {

		super(45, MessageUtils.colorize("&4Command Button: &c" + buttonName));

		finalButtonName = buttonName;

		setItem(13, new ItemStack(Material.NETHER_STAR), "&cCommands", Collections.singletonList(
				"&6Change your Command Button commands."), player -> {

			setAwaitingPlayer(player);
			new CommandTypeInv(i, buttonName).open(player);
		});

		// =============================================================================================

		List<String> costLore = new ArrayList<>();

		Bukkit.getScheduler().runTaskTimerAsynchronously(i, () -> {

			costLore.clear();
			costLore.add("&6The money the button presser will be charged on use.");
			costLore.add("&6Click to edit.");
			costLore.add("&6Current Cost: &7" + i.getData().getDouble(buttonName + ".cost"));

			setItem(11, new ItemStack(Material.EMERALD), "&cCost", costLore, player -> {

				player.closeInventory();

				CustomTitle.sendTitle(player, "&4Type New Cost in Chat",
						"&6See chat for more instructions.", 5, 60, 5);

				MessageUtils.tellWithoutPrefix(player, "&c" + MessageUtils.chatLine(),
						"&6Enter the new cost in chat.",
						"&6Type &7cancel &6to cancel.",
						"&6Only type in a number.",
						"&6Decimals are supported.",
						"&c" + MessageUtils.chatLine());

				setAwaitingPlayer(player);
				setAwaitingCost(true);
			});
		}, 0L, 35L);

		// =============================================================================================

		List<String> cooldownLore = new ArrayList<>();

		Bukkit.getScheduler().runTaskTimerAsynchronously(i, () -> {
			cooldownLore.clear();
			cooldownLore.add("&6The seconds players must wait to use the button again.");
			cooldownLore.add("&6Click to edit.");
			cooldownLore.add("&6Current Cooldown: &7" + i.getData().getDouble(buttonName + ".cooldown"));

			setItem(15, new ItemStack(Material.COMPASS), "&cCooldown", cooldownLore, player -> {

				player.closeInventory();

				CustomTitle.sendTitle(player, "&4Type New Cooldown Time in Chat",
						"&6See chat for more instructions.", 5, 60, 5);

				MessageUtils.tellWithoutPrefix(player, "&c" + MessageUtils.chatLine(),
						"&6Enter the new cooldown time in chat.",
						"&6Type &7cancel &6to cancel.",
						"&6Only type in a whole number of seconds.",
						"&c" + MessageUtils.chatLine());

				setAwaitingPlayer(player);
				setAwaitingCooldown(true);
			});
		}, 0L, 35L);

		// =============================================================================================

		List<String> permissionLore = new ArrayList<>();

		Bukkit.getScheduler().runTaskTimerAsynchronously(i, () -> {
			permissionLore.clear();
			permissionLore.add("&6The permission required in order to use the button.");
			permissionLore.add("&6Click to edit.");
			permissionLore.add("&6Current Permission: &7" + i.getData().getString(buttonName + ".permission"));

			setItem(30, new ItemStack(Material.PAPER), "&cPermission", permissionLore, player -> {

				player.closeInventory();

					CustomTitle.sendTitle(player, "&4Type New Permission in Chat",
							"&6See chat for more instructions.", 5, 60, 5);

				MessageUtils.tellWithoutPrefix(player, "&c" + MessageUtils.chatLine(),
						"&6Enter the new permission node in chat.",
						"&6Type &7none &6for no permission node.",
						"&6Type &7cancel &6to cancel.",
						"&c" + MessageUtils.chatLine());

				setAwaitingPlayer(player);
				setAwaitingPermission(true);
			});
		}, 0L, 35L);

		// =============================================================================================

		setItem(32, new ItemStack(Material.SUGAR), "&cMessages", Collections.singletonList(
				"&6Change the messages that are sent when this button is used."), player -> {
			setAwaitingPlayer(player);
			new MessagesManagerInv(i, buttonName).open(player);
		});
	}
}
