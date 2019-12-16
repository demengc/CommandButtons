package com.demeng7215.commandbuttons.listeners;

import com.demeng7215.commandbuttons.CommandButtons;
import com.demeng7215.commandbuttons.buttonfunctions.CommandButtonFunction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CommandButtonUseListeners implements Listener {

    private CommandButtons i;

    public CommandButtonUseListeners(CommandButtons i) {
        this.i = i;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandButtonPush(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null &&
                e.getAction() == Action.RIGHT_CLICK_BLOCK &&
                e.getClickedBlock().getType().name().endsWith("_BUTTON")) {
            new CommandButtonFunction(i, e.getClickedBlock().getLocation(), e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandButtonClick(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null &&
                e.getClickedBlock().getType().name().contains("SIGN")) {
            new CommandButtonFunction(i, e.getClickedBlock().getLocation(), e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandButtonStep(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null &&
                e.getAction() == Action.PHYSICAL &&
                e.getClickedBlock().getType().name().endsWith("_PLATE")) {
            new CommandButtonFunction(i, e.getClickedBlock().getLocation(), e.getPlayer());
        }
    }
}
