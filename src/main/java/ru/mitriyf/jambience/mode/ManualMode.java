package ru.mitriyf.jambience.mode;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.manager.WeatherManager;
import ru.mitriyf.jambience.model.EventData;
import ru.mitriyf.jambience.model.ManualData;
import ru.mitriyf.jambience.model.MonthData;
import ru.mitriyf.jambience.values.Values;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ManualMode {
    private final Values values;
    private final Logger logger;
    private final JAmbience plugin;
    private final BukkitScheduler scheduler;
    private final WeatherManager weatherManager;
    private final Map<Integer, MonthData> monthDataMap = new HashMap<>();
    private BukkitTask task;

    public ManualMode(JAmbience plugin, ConfigurationSection manual) {
        this.plugin = plugin;
        values = plugin.getValues();
        logger = plugin.getLogger();
        weatherManager = plugin.getWeatherManager();
        scheduler = plugin.getServer().getScheduler();
        ConfigurationSection monthsSection = manual.getConfigurationSection("months");
        if (monthsSection != null) {
            for (String monthName : monthsSection.getKeys(false)) {
                monthDataMap.put(Integer.parseInt(monthName), new MonthData(monthsSection.getConfigurationSection(monthName), plugin.getRandom()));
            }
        }
    }

    public void auto() {
        task = scheduler.runTaskTimerAsynchronously(plugin, this::processUpdate, 0, values.getUpdate() * 20L);
    }

    public void processUpdate() {
        LocalDate localDate = LocalDate.now();
        ManualData manualData = null;
        MonthData monthData = monthDataMap.get(localDate.getMonthValue());
        if (monthData != null) {
            manualData = monthData.getDayDataMap().get(localDate.getDayOfMonth());
            if (manualData == null) {
                manualData = monthData.getManualData();
            }
        } else {
            logger.warning("An error occurred while retrieving the data — it is missing.");
        }
        if (manualData != null) {
            String status = manualData.getStatus();
            EventData eventData = generateEventData(status, manualData.getCurrentAmount());
            weatherManager.updateSmoothly(eventData, status, Math.round(manualData.getTemperature()), manualData.getWindSpeed(), manualData.getWindDeg(), manualData.getVisibility());
        }
    }

    private EventData generateEventData(String status, double currentAmount) {
        switch (status.toLowerCase()) {
            case "snow": {
                EventData eventData = values.getSnowEvent();
                if (eventData != null) {
                    eventData.setCurrentAmount(Math.round(currentAmount));
                }
                return eventData;
            }
            case "rain": {
                EventData eventData = values.getRainEvent();
                if (eventData != null) {
                    eventData.setCurrentAmount(Math.round(currentAmount));
                }
                return eventData;
            }
            default: {
                return null;
            }
        }
    }

    public void close() {
        if (task != null) {
            task.cancel();
        }
    }
}