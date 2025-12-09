package ru.mitriyf.jambience.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.events.Events;

public class PlayerListener implements Listener {
    private final Events events;

    public PlayerListener(JAmbience plugin) {
        this.events = plugin.getEvents();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!events.isOn()) {
            events.setOn(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        events.getTemperature().remove(e.getPlayer().getUniqueId());
    }
}