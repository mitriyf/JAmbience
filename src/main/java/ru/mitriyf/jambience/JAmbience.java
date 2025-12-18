package ru.mitriyf.jambience;

import lombok.Getter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mitriyf.jambience.cmd.AmbienceCommand;
import ru.mitriyf.jambience.events.Events;
import ru.mitriyf.jambience.listeners.WorldListener;
import ru.mitriyf.jambience.utils.Utils;
import ru.mitriyf.jambience.values.Values;
import ru.mitriyf.jambience.weather.Weather;

import java.util.concurrent.ThreadLocalRandom;

@Getter
@SuppressWarnings("DataFlowIssue")
public final class JAmbience extends JavaPlugin {
    private final ThreadLocalRandom rnd = ThreadLocalRandom.current();
    private Weather weather;
    private Values values;
    private Events events;
    private Utils utils;
    private int version;

    @Override
    public void onEnable() {
        getLogger().info("Support: https://vk.com/jdevs");
        saveDefaultConfig();
        getVer();
        values = new Values(this);
        utils = new Utils(this);
        weather = new Weather(this);
        events = new Events(this);
        values.setup();
        utils.setup();
        getCommand("jambience").setExecutor(new AmbienceCommand(this));
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new WorldListener(this), this);
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