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

package dev.demeng.commandbuttons.commands;

import dev.demeng.commandbuttons.CommandButtons;
import dev.demeng.commandbuttons.menus.ButtonMenu;
import dev.demeng.commandbuttons.menus.ButtonsListMenu;
import dev.demeng.commandbuttons.model.CommandButton;
import dev.demeng.commandbuttons.util.Utils;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.command.CommandBase;
import dev.demeng.pluginbase.command.annotations.Aliases;
import dev.demeng.pluginbase.command.annotations.Command;
import dev.demeng.pluginbase.command.annotations.Default;
import dev.demeng.pluginbase.command.annotations.Description;
import dev.demeng.pluginbase.command.annotations.Optional;
import dev.demeng.pluginbase.command.annotations.Permission;
import dev.demeng.pluginbase.command.annotations.SubCommand;
import dev.demeng.pluginbase.command.annotations.Usage;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

/**
 * The main command of CommandButtons.
 */
@RequiredArgsConstructor
@Command("commandbuttons")
@Aliases({"cb"})
public class CommandButtonsCmd extends CommandBase {

  private final CommandButtons i;

  @Default
  @Description("Displays information for CommandButtons.")
  public void runDefault(CommandSender sender) {
    ChatUtils.coloredTell(
        sender,
        "&c&lRunning CommandButtons v" + Common.getVersion() + " by Demeng.",
        "&6Link: &ehttps://spigotmc.org/resources/60991/");
  }

  @SubCommand("help")
  @Description("Displays the list of commands.")
  public void runHelp(CommandSender sender) {
    for (String line : i.getMessages().getStringList("help")) {
      ChatUtils.coloredTell(sender, line);
    }
  }

  @SubCommand("reload")
  @Description("Reloads configuration files.")
  @Aliases("rl")
  @Permission("commandbuttons.reload")
  public void runReload(CommandSender sender) {

    try {
      i.getMessagesFile().reload();
      i.getDataFile().reload();
    } catch (IOException | InvalidConfigurationException ex) {
      Common.error(ex, "Failed to reload config files.", false, sender);
      return;
    }

    i.updateBaseSettings();
    i.getButtonsManager().loadAllButtons();

    ChatUtils.tell(sender, i.getMessages().getString("reloaded"));
  }

  @SubCommand("create")
  @Description("Creates a new command button.")
  @Usage("/cb create <id>")
  @Permission("commandbuttons.create")
  public void runCreate(Player p, String id) {

    final Block targetBlock = p.getTargetBlock(null, 5);

    if (Utils.isAir(targetBlock)) {
      ChatUtils.tell(p, i.getMessages().getString("no-target-block"));
      return;
    }

    if (i.getButtonsManager().getButton(id) != null) {
      ChatUtils.tell(p, Objects.requireNonNull(
          i.getMessages().getString("id-already-exists")).replace("%id%", id));
      return;
    }

    if (i.getButtonsManager().getButtonByLocation(targetBlock.getLocation()) != null) {
      ChatUtils.tell(p, Objects.requireNonNull(
          i.getMessages().getString("location-already-exists")));
      return;
    }

    final CommandButton button = new CommandButton(
        id,
        Collections.singletonList(targetBlock.getLocation()),
        "none",
        true,
        3000L,
        0.0,
        Collections.singletonList("{CONSOLE}minecraft:say Hello world!"),
        Collections.emptyList());

    i.getButtonsManager().saveButton(button);
    ChatUtils.tell(p, Objects.requireNonNull(i.getMessages().getString("created"))
        .replace("%id%", id));

    new ButtonMenu(i, p, button).open(p);
  }

  @SubCommand("editor")
  @Description("Opens the GUI editor.")
  @Aliases({"gui", "edit"})
  @Usage("/cb editor [id]")
  @Permission("commandbuttons.editor")
  public void runEditor(Player p, @Optional String id) {

    if (id == null) {
      new ButtonsListMenu(i, p).open(p);
      return;
    }

    final CommandButton button = i.getButtonsManager().getButton(id);

    if (button == null) {
      tellIncorrectUsage("/cb editor <id>");
      return;
    }

    new ButtonMenu(i, p, button).open(p);
  }
}
