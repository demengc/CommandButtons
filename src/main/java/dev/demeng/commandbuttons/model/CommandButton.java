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

package dev.demeng.commandbuttons.model;

import dev.demeng.commandbuttons.CommandButtons;
import dev.demeng.commandbuttons.util.LocationSerializer;
import dev.demeng.pluginbase.TimeUtils.DurationFormatter;
import dev.demeng.pluginbase.chat.ChatUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
  private static final String CONSOLE_PREFIX = "{CONSOLE}";

  private final String id;
  private List<Location> locations;
  private String permission;
  private boolean perPlayerCooldown;
  private long cooldownDuration;
  private double cost;
  private List<String> commands;
  private List<String> messages;

  private final Map<Player, Long> lastUsed = new HashMap<>();

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

    return new CommandButton(id,
        locations,
        section.getString("permission"),
        section.getBoolean("cooldown.per-player"),
        section.getLong("cooldown.duration"),
        section.getDouble("cost"),
        section.getStringList("commands"),
        section.getStringList("messages"));
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
      ChatUtils.tell(p, Objects.requireNonNull(messagesConfig.getString("insufficient-permission"))
          .replace("%permission%", permission));
      return false;
    }

    if (!p.hasPermission("commandbuttons.bypass.cooldown")) {
      final long remainingCooldown = getRemainingCooldown(p);

      if (remainingCooldown > 0) {
        ChatUtils.tell(p, Objects.requireNonNull(messagesConfig.getString("cooldown-active"))
            .replace("%remaining%",
                DurationFormatter.LONG.format(Duration.ofMillis(remainingCooldown))));
        return false;
      }
    }

    if (!p.hasPermission("commandbuttons.bypass.cost")) {
      final Economy econ = CommandButtons.getInstance().getEconomyHook();

      if (econ != null) {

        if (!econ.has(p, cost)) {
          ChatUtils.tell(p, Objects.requireNonNull(messagesConfig.getString("insufficient-funds"))
              .replace("%cost%", String.format("%.2f", cost)));
          return false;
        }

        econ.withdrawPlayer(p, cost);
      }
    }

    lastUsed.put(perPlayerCooldown ? p : null, System.currentTimeMillis());

    for (String str : commands) {
      final String command = str.replace("%player%", p.getName());

      if (command.startsWith(CONSOLE_PREFIX)) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace(CONSOLE_PREFIX, ""));
        continue;
      }

      Bukkit.dispatchCommand(p, command);
    }

    for (String msg : messages) {
      ChatUtils.coloredTell(p, msg.replace("%player%", p.getName()));
    }

    return true;
  }

  /**
   * Gets the remaining cooldown, in milliseconds, before the player (or global) can use the button
   * again.
   *
   * @param p The player to check, or null for global
   * @return The remaining cooldown, or -1 is no cooldown
   */
  private long getRemainingCooldown(Player p) {

    final long remaining =
        (getLastUsed(perPlayerCooldown ? p : null) + cooldownDuration) - System.currentTimeMillis();

    if (remaining <= 0) {
      lastUsed.remove(p);
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

    final Long playerLastUsed = lastUsed.get(p);

    if (playerLastUsed == null) {
      return -1;
    }

    return playerLastUsed;
  }
}
