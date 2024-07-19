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

package dev.demeng.commandbuttons.listeners;

import dev.demeng.commandbuttons.CommandButtons;
import dev.demeng.commandbuttons.model.CommandButton;
import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.Events;
import dev.demeng.pluginbase.terminable.TerminableConsumer;
import dev.demeng.pluginbase.terminable.module.TerminableModule;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

/**
 * The listener responsible for listening for command button interactions.
 */
@RequiredArgsConstructor
public class ButtonListener implements TerminableModule {

  // The button interaction timeout, in milliseconds.
  private static final int TIMEOUT = 500;

  private final CommandButtons i;
  private final Map<Player, Long> lastInteracted = new HashMap<>();

  @Override
  public void setup(@NotNull TerminableConsumer consumer) {

    Events.subscribe(PlayerInteractEntityEvent.class, EventPriority.HIGH)
        .handler(e -> {
          if (e.getRightClicked() == null
              || (Common.isServerVersionAtLeast(9) && e.getHand() == EquipmentSlot.OFF_HAND)) {
            return;
          }

          final CommandButton button = i.getButtonsManager()
              .getButtonByLocation(e.getRightClicked().getLocation());

          if (button == null) {
            return;
          }

          if (lastInteracted.getOrDefault(e.getPlayer(), 0L) + TIMEOUT
              >= System.currentTimeMillis()) {
            e.setCancelled(true);
            return;
          }

          lastInteracted.put(e.getPlayer(), System.currentTimeMillis());

          final boolean success = button.use(e.getPlayer());

          // If the command button use was successful.
          if (success) {

            // Cancel interaction if material is on the "disable interaction" list.
            if (i.getSettings().getStringList("disable-interaction").stream()
                .anyMatch(str -> str.equalsIgnoreCase(e.getRightClicked().getType().name()))) {
              e.setCancelled(true);
            }

            return;
          }

          // Cancel interaction if unsuccessful.
          e.setCancelled(true);
        })
        .bindWith(consumer);


    Events.subscribe(PlayerInteractEvent.class, EventPriority.HIGH)
        .handler(e -> {
          if (e.getClickedBlock() == null
              || (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.PHYSICAL)
              || (Common.isServerVersionAtLeast(9) && e.getHand() == EquipmentSlot.OFF_HAND)) {
            return;
          }

          final CommandButton button = i.getButtonsManager()
              .getButtonByLocation(e.getClickedBlock().getLocation());

          if (button == null) {
            return;
          }

          if (lastInteracted.getOrDefault(e.getPlayer(), 0L) + TIMEOUT
              >= System.currentTimeMillis()) {
            e.setCancelled(true);
            return;
          }

          lastInteracted.put(e.getPlayer(), System.currentTimeMillis());

          final boolean success = button.use(e.getPlayer());

          // If the command button use was successful.
          if (success) {

            // Cancel interaction if material is on the "disable interaction" list.
            if (i.getSettings().getStringList("disable-interaction").stream()
                .anyMatch(str -> str.equalsIgnoreCase(e.getClickedBlock().getType().name()))) {
              e.setCancelled(true);
            }

            return;
          }

          // Cancel interaction if unsuccessful.
          e.setCancelled(true);
        })
        .bindWith(consumer);

    Events.subscribe(PlayerQuitEvent.class, EventPriority.MONITOR)
        .handler(e -> lastInteracted.remove(e.getPlayer()))
        .bindWith(consumer);
  }
}
