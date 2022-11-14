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

package dev.demeng.commandbuttons.menus;

import dev.demeng.commandbuttons.CommandButtons;
import dev.demeng.commandbuttons.model.CommandButton;
import dev.demeng.commandbuttons.util.LocationSerializer;
import dev.demeng.commandbuttons.util.Utils;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.item.ItemBuilder;
import dev.demeng.pluginbase.lib.xseries.XMaterial;
import dev.demeng.pluginbase.menu.layout.Menu;
import dev.demeng.pluginbase.menu.layout.PagedMenu;
import dev.demeng.pluginbase.menu.model.MenuButton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The menu that lists all command buttons created on the server.
 */
public class ButtonsListMenu extends PagedMenu {

  public ButtonsListMenu(CommandButtons i, Player p) {
    super(54, "Command Buttons", new Settings() {
      @Override
      public @NotNull MenuButton getPreviousButton() {
        return new MenuButton(45, new ItemBuilder(XMaterial.GREEN_DYE.parseItem())
            .name("&c< Previous Page").get(), null);
      }

      @Override
      public @NotNull MenuButton getDummyPreviousButton() {
        return new MenuButton(45, new ItemBuilder(XMaterial.GRAY_DYE.parseItem())
            .name("&7You are on the first page.").get(), null);
      }

      @Override
      public @NotNull MenuButton getNextButton() {
        return new MenuButton(53, new ItemBuilder(XMaterial.GREEN_DYE.parseItem())
            .name("&aNext Page >").get(), null);
      }

      @Override
      public @NotNull MenuButton getDummyNextButton() {
        return new MenuButton(53, new ItemBuilder(XMaterial.GRAY_DYE.parseItem())
            .name("&7You are on the last page.").get(), null);
      }

      @Override
      public @NotNull List<Integer> getAvailableSlots() {
        return IntStream.range(0, 36).boxed().collect(Collectors.toList());
      }
    });

    final List<MenuButton> buttons = new ArrayList<>();

    for (CommandButton button : i.getButtonsManager().getButtons()) {

      // The material of the block at the first non-air location, or a barrier is not applicable.
      ItemStack stack = new ItemStack(Material.BARRIER);

      for (Location loc : button.getLocations()) {
        if (!Utils.isAir(loc.getBlock())) {
          stack = new ItemStack(loc.getBlock().getType());
          break;
        }
      }

      if (Common.getServerMajorVersion() < 13 && stack.getType().name().equals("SKULL")) {
        stack = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
      }

      final List<String> lore = new ArrayList<>();

      lore.add("&6Locations");

      for (Location loc : button.getLocations()) {
        lore.add("&f- " + LocationSerializer.serialize(loc)
            + " (" + loc.getBlock().getType().name() + ")");
      }

      lore.add("");
      lore.add("&6Commands");

      for (String cmd : button.getCommands()) {
        if (cmd.startsWith(CommandButton.CONSOLE_PREFIX)) {
          lore.add("&a*&f/" + cmd.replace(CommandButton.CONSOLE_PREFIX, ""));
        } else {
          lore.add("&f/" + cmd);
        }
      }

      lore.add("");
      lore.add("&eClick to edit.");

      buttons.add(new MenuButton(-1, new ItemBuilder(stack)
          .name("&c" + button.getId())
          .lore(lore).get(),
          event -> new ButtonMenu(i, p, button).open(p)));
    }

    fill(buttons);

    for (Menu page : getPages()) {
      page.setRow(5, XMaterial.BLACK_STAINED_GLASS_PANE.parseItem());
    }
  }
}
