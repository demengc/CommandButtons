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

package dev.demeng.commandbuttons.menus;

import dev.demeng.commandbuttons.CommandButtons;
import dev.demeng.commandbuttons.model.CommandButton;
import dev.demeng.commandbuttons.util.LocationSerializer;
import dev.demeng.commandbuttons.util.Utils;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.Time;
import dev.demeng.pluginbase.Time.DurationFormatter;
import dev.demeng.pluginbase.input.ChatInputRequest;
import dev.demeng.pluginbase.item.ItemBuilder;
import dev.demeng.pluginbase.lib.xseries.XMaterial;
import dev.demeng.pluginbase.menu.layout.Menu;
import dev.demeng.pluginbase.menu.model.MenuButton;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * The menu for editing a specific button.
 */
public class ButtonMenu extends Menu {

  private final CommandButtons i;
  private final Player p;
  private final CommandButton button;

  public ButtonMenu(CommandButtons i, Player p, CommandButton button) {
    super(45, "Edit Button");

    this.i = i;
    this.p = p;
    this.button = button;

    addLocationsButton();
    addPermissionButton();
    addCooldownTypeButton();
    addCooldownDurationButton();
    addCostButton();
    addCommandsButton();
    addMessagesButton();
    addDeleteButton();
  }

  private void addLocationsButton() {

    final List<String> lore = new ArrayList<>();
    lore.add("&7Locations of the blocks that trigger this button.");
    lore.add("");
    lore.add("&6Current:");

    for (Location loc : button.getLocations()) {
      final String blockName = (Utils.isItemFrame(loc)) ? "Item Frame" : loc.getBlock().getType().name();

      lore.add("&f- " + LocationSerializer.serialize(loc)
          + " (" + blockName + ")");
    }

    lore.add("");
    lore.add("&e/cb addLocation " + button.getId() + " on a block to add.");
    lore.add("&e/cb removeLocation " + button.getId() + " on a block to remove.");

    addButton(MenuButton.create(10, ItemBuilder.create(XMaterial.COMPASS.parseItem())
        .name("&cLocations")
        .lore(lore)
        .get(), null));
  }

  private void addPermissionButton() {
    addButton(MenuButton.create(12, ItemBuilder.create(XMaterial.PAPER.parseItem())
        .name("&cPermission")
        .addLore("&7The permission required to use the button.")
        .addLore("")
        .addLore("&6Current: &f" + button.getPermission())
        .addLore("")
        .addLore("&eClick to edit.")
        .get(), event -> {
      p.closeInventory();
      ChatInputRequest.create(str -> str)
          .withInitialMessage("&6Enter a custom permission node, or type &ecancel &6to return.")
          .onExit(() -> new ButtonMenu(i, p, button).open(p))
          .onFinish(permission -> {
            button.setPermission(permission);
            i.getButtonsManager().saveButton(button);
          })
          .start(p);
    }));
  }

  private void addCooldownTypeButton() {
    addButton(MenuButton.create(14, ItemBuilder.create(XMaterial.ICE.parseItem())
        .name("&cCooldown Type")
        .addLore("&7Global cooldowns affect the entire server, while")
        .addLore("&7per-player cooldowns will only apply to the player")
        .addLore("&7who used the button.")
        .addLore("")
        .addLore("&6Current: &f" + (button.isPerPlayerCooldown() ? "Per-Player" : "Global"))
        .addLore("")
        .addLore("&eClick to toggle.")
        .get(), event -> {
      button.setPerPlayerCooldown(!button.isPerPlayerCooldown());
      addCooldownTypeButton();
      i.getButtonsManager().saveButton(button);
    }));
  }

  private void addCooldownDurationButton() {
    addButton(MenuButton.create(16, ItemBuilder.create(XMaterial.CLOCK.parseItem())
        .name("&cCooldown Duration")
        .addLore("&7The duration of the cooldown.")
        .addLore("")
        .addLore("&6Current: &f"
            + Time.formatDuration(DurationFormatter.CONCISE, button.getCooldownDuration()))
        .addLore("")
        .addLore("&eClick to edit.")
        .get(), event -> {
      p.closeInventory();
      ChatInputRequest.create(str -> Time.parseSafely(str).orElse(null))
          .withInitialMessage(
              "&6Enter the cooldown duration (ex. &e30s&6), or type &ecancel &6to return.")
          .onExit(() -> new ButtonMenu(i, p, button).open(p))
          .onFinish(duration -> {
            button.setCooldownDuration(duration.toMillis());
            i.getButtonsManager().saveButton(button);
          })
          .start(p);
    }));
  }

  private void addCostButton() {
    addButton(MenuButton.create(28, ItemBuilder.create(XMaterial.SUNFLOWER.parseItem())
        .name("&cCost")
        .addLore("&7In-game currency withdrawn per use.")
        .addLore("")
        .addLore("&6Current: &f" + String.format("%.2f", button.getCost()))
        .addLore("")
        .addLore("&eClick to edit.")
        .get(), event -> {
      p.closeInventory();
      ChatInputRequest.create(Common::checkDouble)
          .withInitialMessage("&6Enter the cost, or type &ecancel &6to return.")
          .onExit(() -> new ButtonMenu(i, p, button).open(p))
          .onFinish(cost -> {
            button.setCost(cost);
            i.getButtonsManager().saveButton(button);
          })
          .start(p);
    }));
  }

  private void addCommandsButton() {

    final List<String> lore = new ArrayList<>();
    lore.add("&7Commands executed on button use. Commands");
    lore.add("&7annotated with &a* &7are executed by console.");
    lore.add("&7Otherwise, they are executed by the player.");
    lore.add("");
    lore.add("&6Current:");

    for (String cmd : button.getCommands()) {
      if (cmd.startsWith(CommandButton.CONSOLE_PREFIX)) {
        lore.add("&f- &a*&f/" + cmd.replace(CommandButton.CONSOLE_PREFIX, ""));
      } else {
        lore.add("&f- /" + cmd);
      }
    }

    lore.add("");
    lore.add("&eClick to add a command.");
    lore.add("&eShift-click to remove the last command.");
    lore.add("&ePress your drop key to clear all commands.");

    addButton(MenuButton.create(30, ItemBuilder.create(XMaterial.COMMAND_BLOCK.parseItem())
        .name("&cCommands")
        .lore(lore)
        .get(), event -> {

      if (event.getClick() == ClickType.DROP) {
        button.getCommands().clear();
        addCommandsButton();
        i.getButtonsManager().saveButton(button);
        return;
      }

      if (!button.getCommands().isEmpty()
          && (event.getClick() == ClickType.SHIFT_LEFT
          || event.getClick() == ClickType.SHIFT_RIGHT)) {
        button.getCommands().remove(button.getCommands().size() - 1);
        addCommandsButton();
        i.getButtonsManager().saveButton(button);
        return;
      }

      p.closeInventory();
      ChatInputRequest.create(str -> str)
          .withInitialMessage("&6Enter a command to add, without the &e/&6."
              + "\n&6Add &a* &6in front to have it executed by console."
              + "\n&6Use &e%player% &6for the player name."
              + "\n&6Type &ecancel &6to return.")
          .onExit(() -> new ButtonMenu(i, p, button).open(p))
          .onFinish(command -> {

            if (command.startsWith("*")) {
              button.getCommands().add(command.replaceFirst("\\*", CommandButton.CONSOLE_PREFIX));
            } else {
              button.getCommands().add(command);
            }

            i.getButtonsManager().saveButton(button);
          })
          .start(p);
    }));
  }

  private void addMessagesButton() {

    final List<String> lore = new ArrayList<>();
    lore.add("&7Messages sent to the user on button use.");
    lore.add("");
    lore.add("&6Current:");

    for (String msg : button.getMessages()) {
      lore.add("&f- " + msg);
    }

    lore.add("");
    lore.add("&eClick to add a message.");
    lore.add("&eShift-click to remove the last message.");
    lore.add("&ePress your drop key to clear all messages.");

    addButton(MenuButton.create(32, ItemBuilder.create(XMaterial.WRITABLE_BOOK.parseItem())
        .name("&cMessages")
        .lore(lore)
        .get(), event -> {

      if (event.getClick() == ClickType.DROP) {
        button.getMessages().clear();
        addMessagesButton();
        i.getButtonsManager().saveButton(button);
        return;
      }

      if (!button.getMessages().isEmpty()
          && (event.getClick() == ClickType.SHIFT_LEFT
          || event.getClick() == ClickType.SHIFT_RIGHT)) {
        button.getMessages().remove(button.getMessages().size() - 1);
        addMessagesButton();
        i.getButtonsManager().saveButton(button);
        return;
      }

      p.closeInventory();
      ChatInputRequest.create(str -> str)
          .withInitialMessage("&6Enter a message to add."
              + "\n&6Use &e%player% &6for the player name."
              + "\n&6Type &ecancel &6to return.")
          .onExit(() -> new ButtonMenu(i, p, button).open(p))
          .onFinish(message -> {
            button.getMessages().add(message);
            i.getButtonsManager().saveButton(button);
          })
          .start(p);
    }));
  }

  private void addDeleteButton() {
    addButton(MenuButton.create(34, ItemBuilder.create(XMaterial.RED_DYE.parseItem())
        .name("&c&lDelete")
        .addLore("")
        .addLore("&6WARNING: This cannot be reversed!")
        .addLore("")
        .addLore("&4Shift-click to permanently delete.")
        .get(), event -> {
      if (event.getClick() == ClickType.SHIFT_LEFT) {
        i.getButtonsManager().deleteButton(button.getId());
        new ButtonsListMenu(i, p).open(p);
      }
    }));
  }
}
