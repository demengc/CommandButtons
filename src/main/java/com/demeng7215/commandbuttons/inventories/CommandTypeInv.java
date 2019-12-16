package com.demeng7215.commandbuttons.inventories;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.demlib.api.gui.CustomInventory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;

class CommandTypeInv extends CustomInventory {

	CommandTypeInv(CommandButtons i, String buttonName) {
		super(36, "&4Select a Command Type");

		setItem(11, new ItemStack(Material.NETHER_STAR), "&cConsole Commands", Arrays.asList(
				"&6These commands are executed by the console.",
				"&6You should put commands here if the player",
				"&6wouldn't have the permissions to execute it."), player ->
				new CommandsManagerInv(i, buttonName, true).open(player));

		setItem(15, new ItemStack(Material.STONE_SWORD), "&cPlayer Commands", Arrays.asList(
				"&6These commands are executed by the button presser.",
				"&6You should put commands here if the player, would",
				"&6have permissions to execute it."), player ->
				new CommandsManagerInv(i, buttonName, false).open(player));

		setItem(35, new ItemStack(Material.ARROW), "&c&lBack", Collections.singletonList(
				"&7Return to the previous menu."), new ButtonInv(i, buttonName)::open);
	}
}
