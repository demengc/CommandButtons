/*
 * MIT License
 *
 * Copyright (c) 2023 Demeng Chen
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

package dev.demeng.commandbuttons.model;

import dev.demeng.commandbuttons.CommandButtons;
import dev.demeng.commandbuttons.util.LocationSerializer;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.Time;
import dev.demeng.pluginbase.Time.DurationFormatter;
import dev.demeng.pluginbase.text.Text;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * The object for all command buttons.
 */
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CommandButton {

  // The prefix that all commands executed by the console should have.
  public static final String CONSOLE_PREFIX = "{CONSOLE}";
  // The configuration key that indicates the data is for the global context.
  private static final String GLOBAL_KEY = "GLOBAL";
  // The minimum cooldown duration for it to be saved and persist across restarts/reloads.
  private static final int COOLDOWN_SAVE_THRESHOLD = 5000;

  private final String id;
  private List<Location> locations;
  private String permission;
  private boolean perPlayerCooldown;
  private long cooldownDuration;
  private double cost;
  private List<String> commands;
  private List<String> messages;

  private final Map<UUID, Long> lastUsed = new HashMap<>();

  /**
   * Gets a command button object from config.
   *
   * @param id The ID of the command button
   * @return The command button with the given ID, or null
   */
  public static CommandButton fromConfig(String id) {

    final FileConfiguration data = CommandButtons.getInstance().getData();
    final ConfigurationSection section = data.getConfigurationSection("buttons." + id);
    Objects.requireNonNull(section, "Button section is null: " + id);

    final List<Location> locations = new ArrayList<>();

    for (String strLoc : section.getStringList("locations")) {
      locations.add(LocationSerializer.deserialize(strLoc));
    }

    final CommandButton button = new CommandButton(id,
        locations,
        section.getString("permission"),
        section.getBoolean("cooldown.per-player"),
        section.getLong("cooldown.duration"),
        section.getDouble("cost"),
        section.getStringList("commands"),
        section.getStringList("messages"));

    final ConfigurationSection lastUsedSection = section.getConfigurationSection("last-used");

    if (lastUsedSection != null) {

      boolean modified = false;

      for (String key : lastUsedSection.getKeys(false)) {
        final long playerLastUsed = lastUsedSection.getLong(key);

        // Cooldown already expired, just remove.
        if (playerLastUsed + button.getCooldownDuration() <= System.currentTimeMillis()) {
          lastUsedSection.set(key, null);
          modified = true;
          continue;
        }

        button.getLastUsed().put(key.equals(GLOBAL_KEY) ? null : UUID.fromString(key),
            lastUsedSection.getLong(key));
      }

      if (modified) {
        try {
          CommandButtons.getInstance().getDataFile().save();
        } catch (IOException ex) {
          Common.error(ex, "Failed to save data.", false);
        }
      }
    }

    return button;
  }

  /**
   * Attempts to use the command button.
   *
   * @param p The player using the command button
   * @return True if successful, false otherwise
   */
  public boolean use(Player p) {

    final FileConfiguration messagesConfig = CommandButtons.getInstance().getMessages();

    if (!permission.equalsIgnoreCase("none") && !p.hasPermission(permission)) {
      Text.tell(p, Objects.requireNonNull(messagesConfig.getString("no-permission"))
          .replace("%permission%", permission));
      return false;
    }

    if (!p.hasPermission("commandbuttons.bypass.cooldown")) {
      final long remainingCooldown = getRemainingCooldown(p);

      if (remainingCooldown > 0) {
        Text.tell(p, Objects.requireNonNull(messagesConfig.getString("cooldown-active"))
            .replace("%remaining%", Time.formatDuration(
                DurationFormatter.LONG, remainingCooldown)));
        return false;
      }
    }

    if (!p.hasPermission("commandbuttons.bypass.cost")) {
      final Economy econ = CommandButtons.getInstance().getEconomyHook();

      if (econ != null) {

        if (!econ.has(p, cost)) {
          Text.tell(p, Objects.requireNonNull(messagesConfig.getString("insufficient-funds"))
              .replace("%cost%", String.format("%.2f", cost)));
          return false;
        }

        econ.withdrawPlayer(p, cost);
      }
    }

    lastUsed.put(perPlayerCooldown ? p.getUniqueId() : null, System.currentTimeMillis());
    saveLastUsed(perPlayerCooldown ? p : null);

    for (String str : commands) {
      final String command = str.replace("%player%", p.getName());

      if (command.startsWith(CONSOLE_PREFIX)) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace(CONSOLE_PREFIX, ""));
        continue;
      }

      Bukkit.dispatchCommand(p, command);
    }

    for (String msg : messages) {
      Text.coloredTell(p, msg.replace("%player%", p.getName()));
    }

    return true;
  }

  /**
   * Gets the remaining cooldown, in milliseconds, before the player (or global) can use the button
   * again.
   *
   * @param p The player to check
   * @return The remaining cooldown, or -1 is no cooldown
   */
  private long getRemainingCooldown(Player p) {

    final long remaining =
        (getLastUsed(perPlayerCooldown ? p : null) + cooldownDuration) - System.currentTimeMillis();

    if (remaining <= 0) {
      lastUsed.remove(perPlayerCooldown ? p : null);

      if (cooldownDuration >= COOLDOWN_SAVE_THRESHOLD) {
        deleteLastUsed(perPlayerCooldown ? p : null);
      }

      return -1;
    }

    return remaining;
  }

  /**
   * Gets the timestamp, in milliseconds since unix epoch, of when the player last successfully used
   * the button.
   *
   * @param p The player to check, or null for global
   * @return The last used timestamp, or -1 if never used
   */
  private long getLastUsed(Player p) {

    final Long playerLastUsed = lastUsed.get(p == null ? null : p.getUniqueId());

    if (playerLastUsed == null) {
      return -1;
    }

    return playerLastUsed;
  }

  /**
   * Saves the last time the player used the button to config.
   *
   * @param p The player to save, or null for global
   */
  private void saveLastUsed(Player p) {

    // If the cooldown is less than the threshold, don't bother saving.
    if (cooldownDuration < COOLDOWN_SAVE_THRESHOLD) {
      return;
    }

    final String key = p == null ? GLOBAL_KEY : p.getUniqueId().toString();
    final long playerLastUsed = getLastUsed(p);

    if (playerLastUsed != -1) {

      CommandButtons.getInstance().getData()
          .set("buttons." + id + ".last-used." + key, playerLastUsed);

      try {
        CommandButtons.getInstance().getDataFile().save();
      } catch (IOException ex) {
        Common.error(ex, "Failed to save data.", false);
      }
    }
  }

  /**
   * Deletes the last used data of the player from config.
   *
   * @param p The player data to delete, or null for global
   */
  private void deleteLastUsed(Player p) {

    final String key = p == null ? GLOBAL_KEY : p.getUniqueId().toString();

    CommandButtons.getInstance().getData().set("buttons." + id + ".last-used." + key, null);

    try {
      CommandButtons.getInstance().getDataFile().save();
    } catch (IOException ex) {
      Common.error(ex, "Failed to save data.", false);
    }
  }
}
