package ru.mitriyf.jambience;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mitriyf.jambience.command.JAmbienceCommand;
import ru.mitriyf.jambience.events.WeatherEvents;
import ru.mitriyf.jambience.manager.WeatherManager;
import ru.mitriyf.jambience.utils.Utils;
import ru.mitriyf.jambience.values.Values;

import java.util.concurrent.ThreadLocalRandom;

@Getter
@SuppressWarnings("DataFlowIssue")
public final class JAmbience extends JavaPlugin {
    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private WeatherManager weatherManager;
    private WeatherEvents weatherEvents;
    private Values values;
    private Utils utils;
    private int version;

    @Override
    public void onEnable() {
        getLogger().info("Support: https://vk.com/jdevs");
        tryGetServerVersion();
        saveDefaultConfig();
        values = new Values(this);
        utils = new Utils(this);
        utils.setup();
        weatherManager = new WeatherManager(this);
        weatherEvents = new WeatherEvents(this);
        values.setup();
        getCommand("jambience").setExecutor(new JAmbienceCommand(this));
    }

    @Override
    public void onDisable() {
        if (values.getPlaceholders() != null) {
            values.getPlaceholders().unregister();
        }
    }

    private void tryGetServerVersion() {
        try {
            String[] serverVersion = getServer().getBukkitVersion().split("-")[0].split("\\.");
            String subVersion = serverVersion[1];
            if (Integer.parseInt(serverVersion[0]) > 1) {
                version = 26;
            } else if (subVersion.length() >= 2) {
                version = Integer.parseInt(subVersion.substring(0, 2));
            } else {
                version = Integer.parseInt(subVersion);
            }
        } catch (Exception e) {
            getLogger().info("Version check failed. Default set version 26. Error: " + e);
            version = 26;
        }
    }
}