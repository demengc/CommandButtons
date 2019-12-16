package com.demeng7215.commandbuttons;

import com.demeng7215.commandbuttons.buttonfunctions.CooldownTask;
import com.demeng7215.commandbuttons.commands.*;
import com.demeng7215.commandbuttons.listeners.*;
import com.demeng7215.demlib.DemLib;
import com.demeng7215.demlib.api.Common;
import com.demeng7215.demlib.api.DeveloperNotifications;
import com.demeng7215.demlib.api.Registerer;
import com.demeng7215.demlib.api.SpigotUpdateChecker;
import com.demeng7215.demlib.api.files.CustomConfig;
import com.demeng7215.demlib.api.messages.MessageUtils;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandButtons extends JavaPlugin {

    /* ERROR CODES
    1: Failed to load files.
    2: Outdated config.
    3: Failed to hook into Vault and an economy plugin.
    4: Failed to save data.
     */

	public CustomConfig language;
	public CustomConfig data;

	private static final int LANGUAGE_VERSION = 1;

	private static Economy eco = null;

	@Override
	public void onEnable() {

		final long startTime = System.currentTimeMillis();

		DemLib.setPlugin(this, "N/A");

		MessageUtils.setPrefix("&8[&4CommandButtons&8] &r");

		MessageUtils.console("Beginning to enable CommandButtons...\n" +
				"&c   _____                                          _ ____        _   _                  \n" +
				"&c  / ____|                                        | |  _ \\      | | | |                 \n" +
				"&c | |     ___  _ __ ___  _ __ ___   __ _ _ __   __| | |_) |_   _| |_| |_ ___  _ __  ___ \n" +
				"&4 | |    / _ \\| '_ ` _ \\| '_ ` _ \\ / _` | '_ \\ / _` |  _ <| | | | __| __/ _ \\| '_ \\/ __|\n" +
				"&4 | |___| (_) | | | | | | | | | | | (_| | | | | (_| | |_) | |_| | |_| || (_) | | | \\__ \\\n" +
				"&4  \\_____\\___/|_| |_| |_|_| |_| |_|\\__,_|_| |_|\\__,_|____/ \\__,_|\\__|\\__\\___/|_| |_|___/\n");

		getLogger().info("Loading files...");
		if (!setupFiles()) return;

		getLogger().info("Hooking into Vault...");

		if (!setupEconomy()) {
			MessageUtils.error(null, 3, "Failed to hook into Vault and an economy plugin.", true);
			return;
		}

		getLogger().info("Registering commands...");
		Registerer.registerCommand(new CommandButtonsCmd(this));
		Registerer.registerCommand(new InfoCmd());
		Registerer.registerCommand(new HelpCmd(this));
		Registerer.registerCommand(new CreateCmd(this));
		Registerer.registerCommand(new ReloadCmd(this));
		Registerer.registerCommand(new GUICmd(this));
		Registerer.registerCommand(new ListCmd(this));

		getLogger().info("Registering listeners...");
		Registerer.registerListeners(new AddNewCommandListener(this));
		Registerer.registerListeners(new SetNewCostListener(this));
		Registerer.registerListeners(new SetNewCooldownListener(this));
		Registerer.registerListeners(new SetNewPermissionListener(this));
		Registerer.registerListeners(new AddNewMessageListener(this));
		Registerer.registerListeners(new CommandButtonUseListeners(this));
		DeveloperNotifications.enableNotifications("ca19af04-a156-482e-a35d-3f5f434975b5");

		getLogger().info("Starting scheduled tasks...");
		new CooldownTask().runTaskTimerAsynchronously(this, 0L, 1L);

		getLogger().info("Loading metrics...");
		new Metrics(this);

		SpigotUpdateChecker.checkForUpdates(60991);

		final long loadTime = System.currentTimeMillis() - startTime;

		MessageUtils.console("&aCommandButtons v" + Common.getVersion() +
				" by Demeng7215 has been successfully enabled in " + loadTime + "ms.");
	}

	@Override
	public void onDisable() {
		MessageUtils.console("&cCommandButtons v" + Common.getVersion() +
				" by Demeng7215 has been successfully disabled.");
	}

	private boolean setupEconomy() {

		if (getServer().getPluginManager().getPlugin("Vault") == null) return false;

		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

		if (rsp == null) return false;

		eco = rsp.getProvider();

		return eco != null;
	}

	private boolean setupFiles() {

		try {
			language = new CustomConfig("language.yml");
			data = new CustomConfig("data.yml");
		} catch (final Exception ex) {
			MessageUtils.error(ex, 1, "Failed to load files.", true);
			return false;
		}

		if (getLang().getInt("config-version") != LANGUAGE_VERSION) {
			MessageUtils.error(new Exception(), 2, "Outdated configuration file: language.yml", true);
			return false;
		}

		MessageUtils.setPrefix(getLang().getString("prefix"));

		return true;
	}

	public FileConfiguration getLang() {
		return language.getConfig();
	}

	public FileConfiguration getData() {
		return data.getConfig();
	}

	public static Economy getEconomy() {
		return eco;
	}
}