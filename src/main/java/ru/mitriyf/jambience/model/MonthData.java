package ru.mitriyf.jambience.model;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class MonthData {
    private final Map<Integer, ManualData> dayDataMap = new HashMap<>();
    private final ManualData manualData;

    public MonthData(ConfigurationSection monthSection, ThreadLocalRandom random) {
        if (monthSection != null) {
            manualData = new ManualData(monthSection, random);
            ConfigurationSection daysSection = monthSection.getConfigurationSection("days");
            if (daysSection != null) {
                for (String dayName : daysSection.getKeys(false)) {
                    ConfigurationSection daySection = daysSection.getConfigurationSection(dayName);
                    if (daySection != null) {
                        dayDataMap.put(Integer.parseInt(dayName), new ManualData(daySection, random));
                    }
                }
            }
        } else {
            manualData = null;
        }
    }
}
