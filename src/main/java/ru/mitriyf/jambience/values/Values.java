package ru.mitriyf.jambience.values;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.filter.world.BiomesList;
import ru.mitriyf.jambience.filter.world.WorldsList;
import ru.mitriyf.jambience.filter.world.impl.AllowedWorlds;
import ru.mitriyf.jambience.filter.world.impl.BlockedWorlds;
import ru.mitriyf.jambience.listener.PlayerMoveListener;
import ru.mitriyf.jambience.mode.AutoMode;
import ru.mitriyf.jambience.mode.ManualMode;
import ru.mitriyf.jambience.model.EventData;
import ru.mitriyf.jambience.model.PotionData;
import ru.mitriyf.jambience.model.SoundData;
import ru.mitriyf.jambience.placeholders.Placeholders;
import ru.mitriyf.jambience.utils.Utils;
import ru.mitriyf.jambience.utils.actions.Action;
import ru.mitriyf.jambience.utils.actions.ActionType;
import ru.mitriyf.jambience.utils.colors.Colorizer;
import ru.mitriyf.jambience.utils.colors.impl.LegacyColorizer;
import ru.mitriyf.jambience.utils.colors.impl.MiniMessageColorizer;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuppressWarnings("DataFlowIssue")
public class Values {
    private final Logger logger;
    private final JAmbience plugin;
    private final Set<Biome> biomes = new HashSet<>();
    private final Set<World> worlds = new HashSet<>();
    private final List<SoundData> windSounds = new ArrayList<>();
    private final Map<String, String> weatherTypes = new HashMap<>();
    private final Map<Integer, EventData> seasonDataMap = new HashMap<>();
    private final Map<Integer, List<PotionData>> potionDataMap = new HashMap<>();
    private final Pattern actionPattern = Pattern.compile("\\[(\\w+)] ?(.*)");
    private int update, coldTemperature, weatherTicks, timeTicks, windSpeed, windDeg, temperatureTicks, squallTicks, freezeMaxTicks, freezeDifference, freezeMultiplier, visibilityMaxChunks, visibilityMaxDistance, visibilityTicks, precipitationTicks, precipitationRadius, errors;
    private double windMinSpeed, windMaxSpeed, precipitationMinSpeed, precipitationMaxSpeed, precipitationMultiplier, multiplierPulse, precipitationDifference, precipitationRatio, precipitationRandomMin, precipitationRandomMax;
    private boolean placeholderAPI, miniMessage, resetWeather, walkEnabled, squallEnabled, squallResetSprint, squallPulse;
    private String apiUrl, snowColor, snowEmoji, snowTemperature, sunnyColor, sunnyEmoji, sunnyTemperature, cityName;
    @Setter
    private boolean freezeEnabled, windEnabled, visibilityEnabled, precipitationEnabled, timeEnabled, potionEnabled;
    private float defaultSpeed, modifier, walkMinSpeed, walkMaxSpeed;
    private PlayerMoveListener playerMoveListener;
    private DateTimeFormatter dateTimeFormatter;
    private EventData snowEvent, rainEvent;
    private List<Action> noperm, help;
    private Placeholders placeholders;
    private ManualMode manualMode;
    private WorldsList worldType;
    private BiomesList biomeType;
    private Colorizer colorizer;
    private AutoMode autoMode;
    private ZoneId zoneId;
    private Utils utils;

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
        utils = plugin.getUtils();
        plugin.saveDefaultConfig();
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
        plugin.getWeatherEvents().setup();
    }

    private void setupSettings(ConfigurationSection settings) {
        String translate = settings.getString("translate").toLowerCase();
        if (miniMessage && translate.equalsIgnoreCase("minimessage")) {
            colorizer = new MiniMessageColorizer();
        } else {
            colorizer = new LegacyColorizer();
        }
        ConfigurationSection mode = settings.getConfigurationSection("mode");
        ConfigurationSection manual = mode.getConfigurationSection("manual");
        try {
            manualMode = new ManualMode(plugin, manual);
            ConfigurationSection auto = mode.getConfigurationSection("auto");
            boolean autoEnabled = auto.getBoolean("enabled");
            errors = auto.getInt("errorsToManual");
            if (autoEnabled) {
                autoMode = new AutoMode(plugin);
                ConfigurationSection api = auto.getConfigurationSection("api");
                apiUrl = api.getString("url");
                update = api.getInt("update");
                autoMode.auto();
            } else {
                update = manual.getInt("update");
                manualMode.auto();
            }
        } catch (Exception e) {
            logger.warning("An error occurred while retrieving the data: " + e);
        }
        ConfigurationSection supports = settings.getConfigurationSection("supports");
        ConfigurationSection plugins = supports.getConfigurationSection("plugins");
        placeholderAPI = plugins.getBoolean("placeholderAPI");
        if (placeholderAPI && plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            logger.warning("The PlaceholderAPI was not detected. This feature will be disabled.");
            placeholderAPI = false;
        }
        connectPlaceholders();
        ConfigurationSection functions = settings.getConfigurationSection("functions");
        setupSettingsWorlds(functions);
        setupSettingsBiomes(functions);
        setupSettingsWeather(functions);
    }

    private void setupSettingsBiomes(ConfigurationSection functions) {
        ConfigurationSection biomesSection = functions.getConfigurationSection("biomes");
        biomeType = biomesSection.getString("type").equals("allowed") ? new AllowedWorlds(this) : new BlockedWorlds(this);
        List<String> biomesList = biomesSection.getStringList("list");
        for (String biomeString : biomesList) {
            if (biomeString.isEmpty() || biomeString.equals("no")) {
                continue;
            }
            try {
                Biome biome = Biome.valueOf(biomeString.toUpperCase());
                biomes.add(biome);
            } catch (IllegalArgumentException e) {
                if (!biomeString.startsWith("meSa") && !biomeString.startsWith("sAvanna") && !biomeString.startsWith("badlAnds") && !biomeString.endsWith("badlAnds") && !biomeString.endsWith("savAnna") && !biomeString.startsWith("modifiEd") && !biomeString.startsWith("wOoded") && !biomeString.endsWith("hIlls") && !biomeString.endsWith("mountAins")) {
                    logger.warning("Error (IllegalArgumentException) in biomes.list " + biomeString + ": " + e);
                }
            } catch (Exception e) {
                logger.warning("Error in biomes.list " + biomeString + ": " + e);
            }
        }
    }

    private void setupSettingsWorlds(ConfigurationSection functions) {
        ConfigurationSection worldsSection = functions.getConfigurationSection("worlds");
        worldType = worldsSection.getString("type").equals("allowed") ? new AllowedWorlds(this) : new BlockedWorlds(this);
        List<String> worldsList = worldsSection.getStringList("list");
        for (String worldString : worldsList) {
            if (worldString.isEmpty() || worldString.equals("no")) {
                continue;
            }
            try {
                World world = plugin.getServer().getWorld(worldString);
                if (world != null) {
                    worlds.add(world);
                }
            } catch (Exception e) {
                logger.warning("Error in worlds.list " + worldString + ": " + e);
            }
        }
    }

    private void setupSettingsWeather(ConfigurationSection functions) {
        ConfigurationSection timeSection = functions.getConfigurationSection("time");
        timeEnabled = timeSection.getBoolean("enabled");
        cityName = timeSection.getString("name");
        try {
            dateTimeFormatter = DateTimeFormatter.ofPattern(timeSection.getString("format"));
            zoneId = ZoneId.of(timeSection.getString("idZone"));
        } catch (Exception e) {
            logger.warning("An incorrect zone has been identified: " + e);
            zoneId = ZoneId.of("Europe/Moscow");
            dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        }
        timeTicks = timeSection.getInt("ticks");
        ConfigurationSection weather = functions.getConfigurationSection("weather");
        ConfigurationSection status = weather.getConfigurationSection("status");
        ConfigurationSection weatherTypesSection = status.getConfigurationSection("types");
        for (String type : weatherTypesSection.getKeys(false)) {
            weatherTypes.put(type.toLowerCase(), weatherTypesSection.getString(type));
        }
        ConfigurationSection snowSection = status.getConfigurationSection("snow");
        snowColor = snowSection.getString("color");
        snowEmoji = snowSection.getString("emoji");
        snowTemperature = snowSection.getString("temperature");
        ConfigurationSection sunnySection = status.getConfigurationSection("sunny");
        sunnyColor = sunnySection.getString("color");
        sunnyEmoji = sunnySection.getString("emoji");
        sunnyTemperature = sunnySection.getString("temperature");
        ConfigurationSection settings = weather.getConfigurationSection("settings");
        resetWeather = settings.getBoolean("resetWeather");
        weatherTicks = settings.getInt("ticks");
        coldTemperature = settings.getInt("coldTemperature");
        setupPrecipitations(settings.getConfigurationSection("precipitations"));
        ConfigurationSection events = settings.getConfigurationSection("events");
        setupWindEvent(events.getConfigurationSection("wind"));
        setupPrecipitationEvent(events.getConfigurationSection("precipitation"));
        setupVisibilityEvent(events.getConfigurationSection("visibility"));
        setupFreezeEvent(events.getConfigurationSection("freeze"));
        setupTemperatureEvent(events.getConfigurationSection("temperature"));
        ConfigurationSection specialEvents = settings.getConfigurationSection("specialEvents");
        if (specialEvents != null) {
            setupSquallEvent(specialEvents.getConfigurationSection("squall"));
        }
    }

    private void setupPrecipitations(ConfigurationSection precipitations) {
        ConfigurationSection events = precipitations.getConfigurationSection("events");
        snowEvent = new EventData(events.getConfigurationSection("snow"), utils, logger);
        if (!snowEvent.isEnabled()) {
            snowEvent = null;
        }
        rainEvent = new EventData(events.getConfigurationSection("rain"), utils, logger);
        if (!rainEvent.isEnabled()) {
            rainEvent = null;
        }
        ConfigurationSection seasons = precipitations.getConfigurationSection("seasons");
        setupSeason(seasons.getConfigurationSection("winter"));
        setupSeason(seasons.getConfigurationSection("spring"));
        setupSeason(seasons.getConfigurationSection("summer"));
        setupSeason(seasons.getConfigurationSection("autumn"));
    }

    private void setupSeason(ConfigurationSection season) {
        EventData eventData = new EventData(season, utils, logger);
        if (eventData.isEnabled()) {
            String[] months = season.getString("months").split(";");
            for (String month : months) {
                seasonDataMap.put(utils.formatInt(month), eventData);
            }
        }
    }

    private void setupWindEvent(ConfigurationSection section) {
        windEnabled = section.getBoolean("enabled");
        ConfigurationSection speed = section.getConfigurationSection("speed");
        ConfigurationSection walkSection = speed.getConfigurationSection("walk");
        walkEnabled = walkSection.getBoolean("enabled");
        if (walkEnabled && playerMoveListener == null) {
            playerMoveListener = new PlayerMoveListener(plugin);
            plugin.getServer().getPluginManager().registerEvents(playerMoveListener, plugin);
        }
        defaultSpeed = (float) walkSection.getDouble("default");
        modifier = (float) walkSection.getDouble("modifier");
        walkMinSpeed = (float) walkSection.getDouble("minSpeed");
        walkMaxSpeed = (float) walkSection.getDouble("maxSpeed");
        windMinSpeed = speed.getDouble("minSpeed");
        windMaxSpeed = speed.getDouble("maxSpeed");
        ConfigurationSection sound = section.getConfigurationSection("sound");
        try {
            for (String soundString : sound.getStringList("types")) {
                String[] split = soundString.split(";");
                String soundName = split[0];
                if (utils.isOtherParticles()) {
                    if (soundName.equalsIgnoreCase("ITEM_ELYTRA_FLYING")) {
                        continue;
                    }
                } else if (soundName.equalsIgnoreCase("ENDERDRAGON_WINGS")) {
                    continue;
                }
                windSounds.add(new SoundData(Sound.valueOf(soundName), Float.parseFloat(split[1]), Long.parseLong(split[2])));
            }
        } catch (Exception e) {
            logger.warning("An error occurred while retrieving the sounds: " + e);
        }
        ConfigurationSection smoothlyTicks = section.getConfigurationSection("smoothlyTicks");
        windDeg = smoothlyTicks.getInt("windDeg");
        windSpeed = smoothlyTicks.getInt("windSpeed");
    }

    private void setupPrecipitationEvent(ConfigurationSection section) {
        precipitationEnabled = section.getBoolean("enabled");
        precipitationRatio = section.getDouble("ratio");
        precipitationRadius = section.getInt("radius");
        try {
            String[] randomSplit = section.getString("random").split(";");
            double one = Double.parseDouble(randomSplit[0]), two = Double.parseDouble(randomSplit[1]);
            precipitationRandomMin = Math.min(one, two);
            precipitationRandomMax = Math.max(one, two);
        } catch (Exception e) {
            logger.warning("An error occurred while retrieving the doubles: " + e);
        }
        ConfigurationSection fallSpeed = section.getConfigurationSection("fallSpeed");
        precipitationMinSpeed = fallSpeed.getDouble("minSpeed");
        precipitationMaxSpeed = fallSpeed.getDouble("maxSpeed");
        precipitationMultiplier = fallSpeed.getDouble("multiplier");
        precipitationDifference = fallSpeed.getDouble("difference");
        precipitationTicks = section.getInt("smoothlyTicks");
    }

    private void setupVisibilityEvent(ConfigurationSection section) {
        visibilityEnabled = section.getBoolean("enabled");
        visibilityMaxDistance = section.getInt("maxDistance");
        visibilityMaxChunks = section.getInt("maxChunks");
        visibilityTicks = section.getInt("smoothlyTicks");
        ConfigurationSection potionEffects = section.getConfigurationSection("potionEffects");
        potionEnabled = potionEffects.getBoolean("enabled");
        try {
            ConfigurationSection chunks = potionEffects.getConfigurationSection("chunks");
            for (String s : chunks.getKeys(false)) {
                List<PotionData> potionDataList = generatePotionDataList(chunks.getStringList(s));
                if (potionDataList == null) {
                    continue;
                }
                String[] split = s.split("-");
                int one = utils.formatInt(split[0]);
                int two = utils.formatInt(split[1]);
                int minChunks = Math.min(one, two);
                int maxChunks = Math.max(one, two);
                for (int i = minChunks; i <= maxChunks; i++) {
                    potionDataMap.put(i, potionDataList);
                }
            }
        } catch (Exception e) {
            logger.warning("An error occurred while generating the potion effects: " + e);
        }
    }

    private List<PotionData> generatePotionDataList(List<String> stringList) {
        List<PotionData> potionDataList = new ArrayList<>();
        for (String s : stringList) {
            String[] split = s.split(";");
            PotionEffectType potionEffectType = PotionEffectType.getByName(split[0]);
            if (potionEffectType == null) {
                continue;
            }
            int duration = utils.formatInt(split[1]);
            int amplifier = utils.formatInt(split[2]);
            boolean ambient = Boolean.parseBoolean(split[3]);
            boolean particles = Boolean.parseBoolean(split[4]);
            long everyTicks = Long.parseLong(split[5]);
            PotionEffect potionEffect;
            if (plugin.getVersion() < 8) {
                potionEffect = new PotionEffect(potionEffectType, duration, amplifier, ambient);
            } else {
                potionEffect = new PotionEffect(potionEffectType, duration, amplifier, ambient, particles);
            }
            potionDataList.add(new PotionData(potionEffect, everyTicks));
        }
        if (potionDataList.isEmpty()) {
            return null;
        }
        return potionDataList;
    }

    private void setupFreezeEvent(ConfigurationSection section) {
        freezeEnabled = section.getBoolean("enabled");
        freezeMaxTicks = section.getInt("maxTicks");
        freezeDifference = section.getInt("difference");
        freezeMultiplier = section.getInt("multiplier");
    }

    private void setupTemperatureEvent(ConfigurationSection section) {
        temperatureTicks = section.getInt("smoothlyTicks");
    }

    private void setupSquallEvent(ConfigurationSection squallSection) {
        squallEnabled = squallSection.getBoolean("enabled");
        squallResetSprint = squallSection.getBoolean("resetSprint");
        squallPulse = squallSection.getBoolean("pulse");
        multiplierPulse = squallSection.getDouble("multiplierPulse");
        squallTicks = squallSection.getInt("everyTicks");
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
        Matcher matcher = actionPattern.matcher(str);
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
        List<Action> actionListBuilder = new ArrayList<>();
        for (String actionString : actionStrings) {
            actionListBuilder.add(fromString(actionString));
        }
        return actionListBuilder;
    }

    private void clear() {
        if (autoMode != null) {
            apiUrl = null;
            autoMode.close();
            autoMode = null;
        }
        if (manualMode != null) {
            manualMode.close();
            manualMode = null;
        }
        windSounds.clear();
        weatherTypes.clear();
    }
}