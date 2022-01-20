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

package dev.demeng.commandbuttons.listeners;

import dev.demeng.commandbuttons.CommandButtons;
import dev.demeng.commandbuttons.model.CommandButton;
import dev.demeng.pluginbase.Common;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * The listener responsible for listening for command button interactions.
 */
@RequiredArgsConstructor
public class ButtonListener implements Listener {

  private final CommandButtons i;

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerInteract(PlayerInteractEvent e) {

    if ((Common.isServerVersionAtLeast(9) && e.getHand() != EquipmentSlot.HAND)
        || (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.PHYSICAL)
        || e.getClickedBlock() == null) {
      return;
    }

    final CommandButton button = i.getButtonsManager()
        .getButtonByLocation(e.getClickedBlock().getLocation());

    if (button != null) {
      // Cancel event if button use is unsuccessful.
      e.setCancelled(!button.use(e.getPlayer()));
    }
  }
}
