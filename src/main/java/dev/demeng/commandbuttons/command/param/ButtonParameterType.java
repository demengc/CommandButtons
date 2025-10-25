/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
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

package dev.demeng.commandbuttons.command.param;

import dev.demeng.commandbuttons.CommandButtons;
import dev.demeng.commandbuttons.model.CommandButton;
import dev.demeng.pluginbase.lib.lamp.autocomplete.SuggestionProvider;
import dev.demeng.pluginbase.lib.lamp.bukkit.actor.BukkitCommandActor;
import dev.demeng.pluginbase.lib.lamp.exception.ValueNotAllowedException;
import dev.demeng.pluginbase.lib.lamp.node.ExecutionContext;
import dev.demeng.pluginbase.lib.lamp.parameter.ParameterType;
import dev.demeng.pluginbase.lib.lamp.stream.MutableStringStream;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class ButtonParameterType implements ParameterType<BukkitCommandActor, CommandButton> {

  private final CommandButtons i;

  @Override
  public CommandButton parse(@NotNull MutableStringStream input,
      @NotNull ExecutionContext<BukkitCommandActor> context) {
    final String id = input.readString();
    final Optional<CommandButton> button = i.getButtonsManager().getButton(id);

    if (button.isEmpty()) {
      throw new ValueNotAllowedException(
          id, List.copyOf(i.getButtonsManager().getButtons().keySet()), true);
    }

    return button.get();
  }

  @Override public @NotNull SuggestionProvider<@NotNull BukkitCommandActor> defaultSuggestions() {
    return context -> List.copyOf(i.getButtonsManager().getButtons().keySet());
  }
}
