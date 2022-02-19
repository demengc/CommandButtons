/*
 * MIT License
 *
 * Copyright (c) 2018-2022 Demeng Chen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.demeng.commandbuttons;

import dev.demeng.commandbuttons.commands.CommandButtonsCmd;
import dev.demeng.commandbuttons.listeners.ButtonListener;
import dev.demeng.commandbuttons.manager.ButtonsManager;
import dev.demeng.pluginbase.BaseSettings;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.Registerer;
import dev.demeng.pluginbase.TaskUtils;
import dev.demeng.pluginbase.UpdateChecker;
import dev.demeng.pluginbase.UpdateChecker.Result;
import dev.demeng.pluginbase.YamlConfig;
import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.plugin.BasePlugin;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * The main class for CommandButtons.
 */
public final class CommandButtons extends BasePlugin {

  @Getter @Setter(AccessLevel.PACKAGE) private static CommandButtons instance;

  // Managers for the corresponding configuration file.
  @Getter private YamlConfig settingsFile;
  @Getter private YamlConfig messagesFile;
  @Getter private YamlConfig dataFile;

  // Versions of the corresponding configuration file.
  private static final int SETTINGS_VERSION = 1;
  private static final int MESSAGES_VERSION = 2;
  private static final int DATA_VERSION = 2;

  @Getter private ButtonsManager buttonsManager;

  // Vault API economy hook.
  @Getter private Economy economyHook;

  @Override
  public void enable() {

    final long startTime = System.currentTimeMillis();

    setInstance(this);

    ChatUtils.coloredConsole("\n\n"
        + "&4___________________ \n"
        + "&4\\_   ___ \\______   \\\n"
        + "&4/    \\  \\/|    |  _/\n"
        + "&c\\     \\___|    |   \\\n"
        + "&c \\______  |______  /\n"
        + "&c        \\/       \\/ \n");

    getLogger().info("Loading configuration files...");
    if (!loadFiles()) {
      return;
    }

    getLogger().info("Initializing base settings...");
    updateBaseSettings();

    getLogger().info("Hooking into Vault and economy plugin...");
    if (!hookEconomy()) {
      getLogger().warning("Vault and/or economy plugin not found! Skipping...");
    }

    getLogger().info("Registering commands...");
    getCommandManager().register(new CommandButtonsCmd(this));

    getLogger().info("Registering listeners...");
    Registerer.registerListener(new ButtonListener(this));

    getLogger().info("Loading metrics...");
    loadMetrics();

    getLogger().info("Checking for updates...");
    checkUpdates();

    ChatUtils.console("&aCommandButtons v" + Common.getVersion()
        + " by Demeng has been enabled in "
        + (System.currentTimeMillis() - startTime) + " ms.");

    TaskUtils.delay(task -> {
      buttonsManager = new ButtonsManager(this);
      getLogger().info("Loaded " + buttonsManager.getButtons().size() + " command button(s).");
    }, 5L);
  }

  @Override
  public void disable() {
    ChatUtils.console("&cCommandButtons v" + Common.getVersion() + " by Demeng has been disabled.");
  }

  /**
   * Loads all configuration files and performs a quick version check to make sure the file is not
   * outdated.
   *
   * @return true if successful, false otherwise
   */
  private boolean loadFiles() {

    // Name of the file that is currently being loading, used for the error message.
    String currentlyLoading = "configuration files";

    try {
      currentlyLoading = "settings.yml";
      settingsFile = new YamlConfig(currentlyLoading);

      if (settingsFile.isOutdated(SETTINGS_VERSION)) {
        Common.error(null, "Outdated settings.yml file.", true);
        return false;
      }

      currentlyLoading = "messages.yml";
      messagesFile = new YamlConfig(currentlyLoading);

      if (messagesFile.isOutdated(MESSAGES_VERSION)) {
        Common.error(null, "Outdated messages.yml file.", true);
        return false;
      }

      currentlyLoading = "data.yml";
      dataFile = new YamlConfig(currentlyLoading);

      if (dataFile.isOutdated(DATA_VERSION)) {
        Common.error(null, "Outdated data.yml file.", true);
        return false;
      }

    } catch (IOException | InvalidConfigurationException ex) {
      Common.error(ex, "Failed to load " + currentlyLoading + ".", true);
      return false;
    }

    return true;
  }

  /**
   * Updates the settings for PluginBase.
   */
  public void updateBaseSettings() {
    setBaseSettings(new BaseSettings() {
      @Override
      public String prefix() {
        return getMessages().getString("prefix");
      }

      @Override
      public String notPlayer() {
        return getMessages().getString("not-player");
      }

      @Override
      public String insufficientPermission() {
        return getMessages().getString("insufficient-permission");
      }

      @Override
      public String incorrectUsage() {
        return getMessages().getString("incorrect-usage");
      }
    });
  }

  /**
   * Hooks into an economy plugin using Vault, if available
   *
   * @return true if hooked, false otherwise
   */
  private boolean hookEconomy() {

    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }

    final RegisteredServiceProvider<Economy> provider =
        getServer().getServicesManager().getRegistration(Economy.class);

    if (provider == null) {
      return false;
    }

    economyHook = provider.getProvider();
    return true;
  }

  /**
   * Loads bStats metrics (sends stats if enabled in bStats config).
   */
  private void loadMetrics() {
    try {
      new Metrics(this, 3241);
    } catch (IllegalStateException ex) {
      if (ex.getMessage().equals("bStats Metrics class has not been relocated correctly!")) {
        // Send warning instead of disabling, since bStats is not relocated when I'm testing.
        getLogger().warning("bStats has not been relocated, skipping.");
      }
    }
  }

  /**
   * Checks if the current plugin version matches the one on SpigotMC.
   */
  private void checkUpdates() {
    TaskUtils.runAsync(task -> {
      final UpdateChecker checker = new UpdateChecker(60991);

      if (checker.getResult() == Result.OUTDATED) {
        ChatUtils.coloredConsole(
            "&2" + ChatUtils.CONSOLE_LINE,
            "&aA newer version of CommandButtons is available!",
            "&aCurrent version: &r" + Common.getVersion(),
            "&aLatest version: &r" + checker.getLatestVersion(),
            "&aGet the update: &rhttps://spigotmc.org/resources/60991",
            "&2" + ChatUtils.CONSOLE_LINE);
        return;
      }

      if (checker.getResult() == Result.ERROR) {
        getLogger().warning("Failed to check for updates.");
      }
    });
  }

  public FileConfiguration getSettings() {
    return settingsFile.getConfig();
  }

  public FileConfiguration getMessages() {
    return messagesFile.getConfig();
  }

  public FileConfiguration getData() {
    return dataFile.getConfig();
  }
}
