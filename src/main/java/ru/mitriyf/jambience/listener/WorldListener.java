package ru.mitriyf.jambience.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import ru.mitriyf.jambience.JAmbience;

public class WorldListener implements Listener {
    private final JAmbience plugin;

    public WorldListener(JAmbience plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent e) {
        updateValues();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent e) {
        updateValues();
    }

    private void updateValues() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getValues().setup(), 5L);
    }
}
