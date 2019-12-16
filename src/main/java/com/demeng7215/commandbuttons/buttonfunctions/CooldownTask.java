package com.demeng7215.commandbuttons.buttonfunctions;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;

public class CooldownTask extends BukkitRunnable {

	@Override
	public void run() {

		for (Iterator<Map.Entry<String, Double>> it = CommandButtonFunction.timeouts.entrySet().iterator();

			 it.hasNext(); ) {
			Map.Entry<String, Double> entry = it.next();

			entry.setValue(entry.getValue() - 1);

			if (entry.getValue() <= 0) it.remove();
		}
	}
}
