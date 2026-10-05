package ru.mitriyf.jambience.mode;

import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.manager.WeatherManager;
import ru.mitriyf.jambience.model.EventData;
import ru.mitriyf.jambience.utils.Utils;
import ru.mitriyf.jambience.values.Values;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

public class AutoMode {
    private final Utils utils;
    private final Logger logger;
    private final Values values;
    private final JAmbience plugin;
    private final ManualMode manualMode;
    private final BukkitScheduler scheduler;
    private final WeatherManager weatherManager;
    private BukkitTask task;
    private int errors;

    public AutoMode(JAmbience plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        values = plugin.getValues();
        logger = plugin.getLogger();
        manualMode = values.getManualMode();
        weatherManager = plugin.getWeatherManager();
        scheduler = plugin.getServer().getScheduler();
    }

    public void auto() {
        task = scheduler.runTaskTimerAsynchronously(plugin, this::processUpdate, 0, values.getUpdate() * 20L);
    }

    public void processUpdate() {
        try {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(values.getApiUrl()).openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder body = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }
                    Map<String, Object> weatherData = utils.asObject(utils.parse(body.toString()));
                    Map<String, Object> main = utils.asObject(weatherData.get("main"));
                    long temperature = Math.round(utils.asDouble(main.get("temp")));
                    Map<String, Object> wind = utils.asObject(weatherData.get("wind"));
                    double windSpeed = utils.asDouble(wind.get("speed"));
                    double windDeg = utils.asDouble(wind.get("deg"));
                    Map<String, Object> weather = utils.asObject(utils.asArray(weatherData.get("weather")).get(0));
                    Object status = weather.get("main");
                    EventData eventData = generateEventData(weatherData);
                    Object visibility = weatherData.get("visibility");
                    weatherManager.updateSmoothly(eventData, status == null ? "Clear" : (String) status, temperature, windSpeed, windDeg, visibility == null ? 10000 : utils.asInt(visibility));
                }
                errors = 0;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (Exception e) {
            errors++;
            if (errors >= values.getErrors()) {
                logger.info("[DENIED] The connection was unsuccessful: " + e + ". I'll use manual control until the next cycle.");
                manualMode.processUpdate();
            }
        }
    }

    private EventData generateEventData(Map<String, Object> weatherData) {
        EventData eventData = null;
        for (String eventType : weatherData.keySet()) {
            switch (eventType.toLowerCase()) {
                case "snow": {
                    eventData = values.getSnowEvent();
                    if (eventData != null) {
                        eventData.setCurrentAmount(countAmount(eventData, weatherData, eventType));
                    }
                    break;
                }
                case "rain": {
                    eventData = values.getRainEvent();
                    if (eventData != null) {
                        eventData.setCurrentAmount(countAmount(eventData, weatherData, eventType));
                    }
                    break;
                }
            }
        }
        return eventData;
    }

    private long countAmount(EventData eventData, Map<String, Object> weatherData, String eventType) {
        double amount = eventData.getMultiplier();
        if (weatherData.containsKey(eventType)) {
            Map<String, Object> object = utils.asObject(weatherData.get(eventType));
            if (object.containsKey("1h")) {
                return Math.round(amount * utils.asDouble(object.get("1h")));
            }
        }
        return Math.round(amount * eventData.getDefaultAmount());
    }

    public void close() {
        if (task != null) {
            task.cancel();
        }
    }
}
