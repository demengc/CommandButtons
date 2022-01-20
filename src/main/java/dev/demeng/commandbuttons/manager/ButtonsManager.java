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

package dev.demeng.commandbuttons.manager;

import dev.demeng.commandbuttons.CommandButtons;
import dev.demeng.commandbuttons.model.CommandButton;
import dev.demeng.commandbuttons.util.LocationSerializer;
import dev.demeng.pluginbase.Common;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

/**
 * The manager for keeping track of buttons.
 */
public class ButtonsManager {

  private final Map<String, CommandButton> buttons = new HashMap<>();

  private final CommandButtons i;

  /**
   * Initializes a new manager and loads all buttons from config.
   *
   * @param i The plugin instance
   */
  public ButtonsManager(CommandButtons i) {
    this.i = i;
    loadAllButtons();
  }

  /**
   * Loads all command buttons from config.
   */
  public void loadAllButtons() {
    for (String key : Objects.requireNonNull(i.getData().getConfigurationSection("buttons"))
        .getKeys(false)) {
      final CommandButton button = CommandButton.fromConfig(key);
      buttons.put(button.getId(), button);
    }
  }

  /**
   * Gets the command button with the specified ID.
   *
   * @param id The ID of the button
   * @return The command button with the given ID
   */
  public CommandButton getButton(String id) {
    return buttons.get(id);
  }

  /**
   * Gets the command button at the specified location.
   *
   * @param loc The location
   * @return The command button at the location, or null
   */
  public CommandButton getButtonByLocation(Location loc) {

    for (CommandButton button : buttons.values()) {
      for (Location buttonLoc : button.getLocations()) {
        if (Objects.equals(loc.getWorld(), buttonLoc.getWorld())
            && loc.getBlockX() == buttonLoc.getBlockX()
            && loc.getBlockY() == buttonLoc.getBlockY()
            && loc.getBlockZ() == buttonLoc.getBlockZ()) {
          return button;
        }
      }
    }

    return null;
  }

  /**
   * Registers and saves a new command button.
   *
   * @param button The command button to register and save
   */
  public void saveButton(CommandButton button) {

    final ConfigurationSection section = i.getData().createSection("buttons." + button.getId());

    final List<String> locations = button.getLocations().stream()
        .map(LocationSerializer::serialize)
        .collect(Collectors.toList());

    section.set("locations", locations);
    section.set("permission", button.getPermission());
    section.set("cooldown.per-player", button.isPerPlayerCooldown());
    section.set("cooldown.duration", button.getCooldownDuration());
    section.set("cost", button.getCost());
    section.set("commands", button.getCommands());
    section.set("messages", button.getMessages());

    try {
      i.getDataFile().save();
    } catch (IOException ex) {
      Common.error(ex, "Failed to save data.", false);
      return;
    }

    buttons.put(button.getId(), button);
  }

  /**
   * Deletes and unregisters the button.
   *
   * @param id The ID of the button to delete and unregister
   */
  public void deleteButton(String id) {

    i.getData().set("buttons." + id, null);

    try {
      i.getDataFile().save();
    } catch (IOException ex) {
      Common.error(ex, "Failed to save data.", false);
      return;
    }

    buttons.remove(id);
  }

  /**
   * Gets all registered command buttons.
   *
   * @return The collection of all command buttons
   */
  public Collection<CommandButton> getButtons() {
    return buttons.values();
  }
}
