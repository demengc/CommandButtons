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
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.chat.ChatUtils;
import dev.demeng.pluginbase.command.CommandBase;
import dev.demeng.pluginbase.command.annotations.Aliases;
import dev.demeng.pluginbase.command.annotations.Command;
import dev.demeng.pluginbase.command.annotations.Default;
import dev.demeng.pluginbase.command.annotations.Description;
import dev.demeng.pluginbase.command.annotations.Permission;
import dev.demeng.pluginbase.command.annotations.SubCommand;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;

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

    ChatUtils.tell(sender, i.getMessages().getString("reloaded"));
  }
}
