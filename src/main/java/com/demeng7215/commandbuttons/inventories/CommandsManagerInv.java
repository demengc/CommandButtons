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

public class CommandsManagerInv extends CustomInventory {

	private CommandButtons i;
	private List<String> currentCommandsLore = new ArrayList<>();

	@Getter
	@Setter
	private static boolean awaitingCommand;

    @Getter
    @Setter
    private static Player awaitingPlayer;

	@Getter
	private static String currentButtonName, currentCommandType;

	public CommandsManagerInv(CommandButtons i, String buttonName, boolean consoleCommand) {
		super(36, "&4Edit Your Commands");

		this.i = i;

		currentButtonName = buttonName;

		String commandType;
		if (consoleCommand) commandType = "console";
		else commandType = "player";

		currentCommandType = commandType;

		setItem(11, new ItemStack(Material.EMERALD_BLOCK), "&cAdd Command", Collections.singletonList(
				"&6Add a " + commandType + " command that should be executed."), player -> {

			player.closeInventory();

			CustomTitle.sendTitle(player, "&4Type New Command in Chat",
					"&6See chat for more instructions.", 5, 60, 5);

			MessageUtils.tellWithoutPrefix(player, "&c" + MessageUtils.chatLine(),
					"&6Enter the new command in chat.",
					"&6Type &7cancel &6to cancel.",
					"&6Use &7%player% &6for the button presser's username.",
					"&6Do not include the &7/ &6at the start of the command.",
					"&c" + MessageUtils.chatLine());

            setAwaitingPlayer(player);
			setAwaitingCommand(true);
		});
		;

		Bukkit.getScheduler().runTaskTimerAsynchronously(i, () -> {
			refresh(buttonName, commandType);
			setItem(13, new ItemStack(Material.OBSIDIAN), "&c&lCurrent Commands", currentCommandsLore,
					Player::updateInventory);
		}, 0L, 35L);

		setItem(15, new ItemStack(Material.REDSTONE), "&cClear Commands", Collections.singletonList(
				"&6Delete all " + commandType + " commands for this button."), player -> {
			i.getData().set(buttonName + ".commands." + commandType, Collections.singletonList("none"));
			try {
				i.data.saveConfig();
			} catch (final IOException ex) {
				MessageUtils.error(ex, 4, "Failed to save data.", true);
			}
		});

		setItem(35, new ItemStack(Material.ARROW), "&c&lBack", Collections.singletonList(
				"&7Return to the previous menu."), new CommandTypeInv(i, buttonName)::open);
	}

	private void refresh(String buttonName, String commandType) {
		this.currentCommandsLore.clear();
		this.currentCommandsLore.add("&6Current " + commandType + " commands are listed below.");

		for (String command : i.getData().getStringList(buttonName + ".commands." + commandType))
			this.currentCommandsLore.add("&6- &7" + command);
	}
}
