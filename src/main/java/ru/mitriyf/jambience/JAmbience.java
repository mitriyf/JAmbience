package ru.mitriyf.jambience;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jambience.cmd.AmbienceCommand;
import ru.mitriyf.jambience.events.Events;
import ru.mitriyf.jambience.listeners.PlayerListener;
import ru.mitriyf.jambience.utils.Utils;
import ru.mitriyf.jambience.values.Values;
import ru.mitriyf.jambience.weather.Weather;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public final class JAmbience extends JavaPlugin {
    private final ThreadLocalRandom rnd = ThreadLocalRandom.current();
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private int version;
    private Weather weather;
    private Values values;
    private Utils utils;
    private Events events;

    @Override
    public void onEnable() {
        getLogger().info("Support: https://vk.com/jdevs");
        saveDefaultConfig();
        getVer();
        values = new Values(this);
        utils = new Utils(this);
        weather = new Weather(this);
        values.setup();
        utils.setup();
        events = new Events(this);
        events.start();
        getCommand("jambience").setExecutor(new AmbienceCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        if (values.getPlaceholders() != null) {
            values.getPlaceholders().unregister();
        }
    }

    private void getVer() {
        String ver = getServer().getBukkitVersion().split("-")[0].split("\\.")[1];
        if (ver.length() >= 2) {
            version = Integer.parseInt(ver.substring(0, 2));
        } else {
            version = Integer.parseInt(ver);
        }
    }
}