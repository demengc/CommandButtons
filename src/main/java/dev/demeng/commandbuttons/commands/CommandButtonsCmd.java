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

package dev.demeng.commandbuttons.commands;

import dev.demeng.commandbuttons.CommandButtons;
import dev.demeng.commandbuttons.menus.ButtonMenu;
import dev.demeng.commandbuttons.menus.ButtonsListMenu;
import dev.demeng.commandbuttons.model.CommandButton;
import dev.demeng.commandbuttons.util.Utils;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.text.Text;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bukkit.annotation.CommandPermission;

/**
 * The main command of CommandButtons.
 */
@RequiredArgsConstructor
@Command({"commandbuttons", "cb"})
public class CommandButtonsCmd {

  private final CommandButtons i;

  @DefaultFor({"commandbuttons", "cb"})
  @Description("Displays information for CommandButtons.")
  public void runDefault(CommandSender sender) {
    Text.coloredTell(sender, "&c&lRunning CommandButtons v" + Common.getVersion() + " by Demeng.");
    Text.coloredTell(sender, "&6Link: &ehttps://spigotmc.org/resources/60991/");
  }

  @Subcommand("help")
  @Description("Displays the list of commands.")
  public void runHelp(CommandSender sender) {
    for (String line : i.getMessages().getStringList("help")) {
      Text.coloredTell(sender, line);
    }
  }

  @Subcommand({"reload", "rl"})
  @Description("Reloads configuration files.")
  @CommandPermission("commandbuttons.reload")
  public void runReload(CommandSender sender) {

    try {
      i.getSettingsFile().reload();
      i.getMessagesFile().reload();
      i.getDataFile().reload();
    } catch (IOException | InvalidConfigurationException ex) {
      Common.error(ex, "Failed to reload config files.", false, sender);
      return;
    }

    i.updateBaseSettings();
    i.getButtonsManager().loadAllButtons();

    Text.tell(sender, i.getMessages().getString("reloaded"));
  }

  @Subcommand("create")
  @Description("Creates a new command button.")
  @Usage("/cb create <id>")
  @CommandPermission("commandbuttons.create")
  public void runCreate(Player p, String id) {

    final Block targetBlock = p.getTargetBlock(null, 5);

    if (Utils.isAir(targetBlock)) {
      Text.tell(p, i.getMessages().getString("no-target-block"));
      return;
    }

    if (i.getButtonsManager().getButton(id) != null) {
      Text.tell(p, Objects.requireNonNull(
          i.getMessages().getString("id-already-exists")).replace("%id%", id));
      return;
    }

    if (i.getButtonsManager().getButtonByLocation(targetBlock.getLocation()) != null) {
      Text.tell(p, Objects.requireNonNull(
          i.getMessages().getString("location-already-exists")));
      return;
    }

    // Test for item frame
    Location finalLocation = targetBlock.getLocation();
    final Location blockFaceLocation = Utils.getBlockFaceLocation(p);

    if (blockFaceLocation != null && Utils.isItemFrame(blockFaceLocation)) {
      finalLocation = blockFaceLocation;
    }

    final CommandButton button = new CommandButton(
        id,
        new ArrayList<>(Collections.singletonList(finalLocation)),
        "none",
        true,
        3000L,
        0.0,
        new ArrayList<>(Collections.singletonList("{CONSOLE}minecraft:say Hello world!")),
        new ArrayList<>(Collections.emptyList()));

    i.getButtonsManager().saveButton(button);
    Text.tell(p, Objects.requireNonNull(i.getMessages().getString("created"))
        .replace("%id%", id));

    new ButtonMenu(i, p, button).open(p);
  }

  @Subcommand({"editor", "gui", "edit"})
  @Description("Opens the GUI editor.")
  @Usage("/cb editor [id]")
  @CommandPermission("commandbuttons.editor")
  public void runEditor(Player p, @Optional String id) {

    if (id == null) {
      new ButtonsListMenu(i, p).open(p);
      return;
    }

    final CommandButton button = i.getButtonsManager().getButton(id);

    if (button == null) {
      Text.tell(p, i.getMessages().getString("invalid-button"));
      return;
    }

    new ButtonMenu(i, p, button).open(p);
  }

  @Subcommand("addlocation")
  @Description("Adds a new location to the command button.")
  @Usage("/cb addLocation <id>")
  @CommandPermission("commandbuttons.editor")
  public void runAddLocation(Player p, String id) {

    final Block targetBlock = p.getTargetBlock(null, 5);

    if (Utils.isAir(targetBlock)) {
      Text.tell(p, i.getMessages().getString("no-target-block"));
      return;
    }

    final CommandButton button = i.getButtonsManager().getButton(id);

    if (button == null) {
      Text.tell(p, i.getMessages().getString("invalid-button"));
      return;
    }

    if (i.getButtonsManager().getButtonByLocation(targetBlock.getLocation()) != null) {
      Text.tell(p, Objects.requireNonNull(
          i.getMessages().getString("location-already-exists")));
      return;
    }

    button.getLocations().add(targetBlock.getLocation());
    i.getButtonsManager().saveButton(button);

    Text.tell(p, Objects.requireNonNull(i.getMessages().getString("location-added"))
        .replace("%id%", id));
  }

  @Subcommand("removelocation")
  @Description("Removes a location from the command button.")
  @Usage("/cb removeLocation <id>")
  @CommandPermission("commandbuttons.editor")
  public void runRemoveLocation(Player p, String id) {

    final Block targetBlock = p.getTargetBlock(null, 5);

    if (Utils.isAir(targetBlock)) {
      Text.tell(p, i.getMessages().getString("no-target-block"));
      return;
    }

    final CommandButton button = i.getButtonsManager().getButton(id);

    if (button == null) {
      Text.tell(p, i.getMessages().getString("invalid-button"));
      return;
    }

    boolean removed = false;

    final Iterator<Location> iterator = button.getLocations().iterator();

    while (iterator.hasNext()) {
      final Location loc = iterator.next();
      if (Objects.equals(loc.getWorld(), targetBlock.getLocation().getWorld())
          && loc.getBlockX() == targetBlock.getLocation().getBlockX()
          && loc.getBlockY() == targetBlock.getLocation().getBlockY()
          && loc.getBlockZ() == targetBlock.getLocation().getBlockZ()) {
        removed = true;
        iterator.remove();
      }
    }

    if (removed) {
      i.getButtonsManager().saveButton(button);
      Text.tell(p, Objects.requireNonNull(i.getMessages().getString("location-removed"))
          .replace("%id%", id));
      return;
    }

    Text.tell(p, Objects.requireNonNull(i.getMessages().getString("location-not-exists"))
        .replace("%id%", id));
  }
}
