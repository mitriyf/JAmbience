package ru.mitriyf.jambience.values;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.placeholders.Placeholders;
import ru.mitriyf.jambience.utils.actions.Action;
import ru.mitriyf.jambience.utils.actions.ActionType;
import ru.mitriyf.jambience.utils.colors.Colorizer;
import ru.mitriyf.jambience.utils.colors.impl.LegacyColorizer;
import ru.mitriyf.jambience.utils.colors.impl.MiniMessageColorizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuppressWarnings("all")
public class Values {
    private final Logger logger;
    private final JAmbience plugin;
    private final Map<String, String> weatherTypes = new HashMap<>();
    private final Pattern action_pattern = Pattern.compile("\\[(\\w+)] ?(.*)");
    private boolean placeholderAPI, viewDistance, weatherEnabled, miniMessage, freeze;
    private List<Action> noperm = new ArrayList<>();
    private List<Action> help = new ArrayList<>();
    private double radiusSnow, multiplierSnow;
    private String apiUrl, snow, sunny;
    private int apiUpdate, defaultSnow;
    private Placeholders placeholders;
    private Colorizer colorizer;

    public Values(JAmbience plugin) {
        this.plugin = plugin;
        logger = plugin.getLogger();
        try {
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            miniMessage = true;
        } catch (Exception e) {
            miniMessage = false;
        }
    }

    public void setup() {
        clear();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection settings = config.getConfigurationSection("settings");
        if (settings == null) {
            logger.warning("В конфигурации отсутствует секция: settings");
            return;
        }
        setupSettings(settings);
        ConfigurationSection messages = config.getConfigurationSection("messages");
        if (messages == null) {
            logger.warning("В конфигурации отсутствует секция: messages");
            return;
        }
        setupMessages(messages);
    }

    private void setupSettings(ConfigurationSection settings) {
        String translate = settings.getString("translate").toLowerCase();
        if (miniMessage && translate.equalsIgnoreCase("minimessage")) {
            colorizer = new MiniMessageColorizer();
        } else {
            colorizer = new LegacyColorizer();
        }
        ConfigurationSection api = settings.getConfigurationSection("api");
        apiUrl = api.getString("url");
        apiUpdate = api.getInt("update");
        ConfigurationSection supports = settings.getConfigurationSection("supports");
        ConfigurationSection plugins = supports.getConfigurationSection("plugins");
        placeholderAPI = plugins.getBoolean("placeholderAPI");
        if (placeholderAPI && plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            logger.warning("The PlaceholderAPI was not detected. This feature will be disabled.");
            placeholderAPI = false;
        }
        connectPlaceholders();
        ConfigurationSection functions = supports.getConfigurationSection("functions");
        ConfigurationSection weather = functions.getConfigurationSection("weather");
        weatherEnabled = weather.getBoolean("enabled");
        ConfigurationSection weatherTypesSection = weather.getConfigurationSection("types");
        for (String type : weatherTypesSection.getKeys(false)) {
            weatherTypes.put(type.toLowerCase(), weatherTypesSection.getString(type));
        }
        ConfigurationSection emojis = weather.getConfigurationSection("emojis");
        snow = emojis.getString("snow");
        sunny = emojis.getString("sunny");
        viewDistance = weather.getBoolean("viewDistance");
        freeze = weather.getBoolean("freeze");
        ConfigurationSection snow = weather.getConfigurationSection("snow");
        radiusSnow = snow.getDouble("radius");
        defaultSnow = snow.getInt("defaultSnow");
        multiplierSnow = snow.getDouble("multiplier");
    }

    private void setupMessages(ConfigurationSection messages) {
        noperm = getActionList(messages.getStringList("noperm"));
        help = getActionList(messages.getStringList("help"));
    }

    private void connectPlaceholders() {
        if (placeholderAPI) {
            if (placeholders == null) {
                placeholders = new Placeholders(plugin);
                placeholders.register();
                logger.info("[JAmbience] Connection to PlaceholderAPI was successful!");
            }
        } else {
            if (placeholders != null) {
                placeholders.unregister();
                placeholders = null;
            }
        }
    }

    private Action fromString(String str) {
        Matcher matcher = action_pattern.matcher(str);
        if (!matcher.matches()) {
            return new Action(ActionType.MESSAGE, str);
        }
        ActionType type;
        try {
            type = ActionType.valueOf(matcher.group(1).toUpperCase());
        } catch (IllegalArgumentException e) {
            type = ActionType.MESSAGE;
            return new Action(type, str);
        }
        return new Action(type, matcher.group(2).trim());
    }

    public List<Action> getActionList(List<String> actionStrings) {
        ImmutableList.Builder<Action> actionListBuilder = ImmutableList.builder();
        for (String actionString : actionStrings) {
            actionListBuilder.add(fromString(actionString));
        }
        return actionListBuilder.build();
    }

    private void clear() {
        weatherTypes.clear();
    }
}