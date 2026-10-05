package ru.mitriyf.jambience.model;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ManualData {
    private final double[] temperature, currentAmount, visibility, windSpeed, windDeg;
    private final List<String> weatherTypes = new ArrayList<>();
    private final ThreadLocalRandom random;

    public ManualData(ConfigurationSection section, ThreadLocalRandom random) {
        this.random = random;
        temperature = getSplit(section.getString("temperature"));
        currentAmount = getSplit(section.getString("currentAmount"));
        visibility = getSplit(section.getString("visibility"));
        windSpeed = getSplit(section.getString("windSpeed"));
        windDeg = getSplit(section.getString("windDeg"));
        weatherTypes.addAll(section.getStringList("statuses"));
    }

    public String getStatus() {
        if (weatherTypes.isEmpty()) {
            return "Clear";
        }
        return weatherTypes.get(random.nextInt(weatherTypes.size()));
    }

    public double getTemperature() {
        return getFromSplit(temperature);
    }

    public double getCurrentAmount() {
        return getFromSplit(currentAmount);
    }

    public double getWindSpeed() {
        return getFromSplit(windSpeed);
    }

    public double getWindDeg() {
        return getFromSplit(windDeg);
    }

    public long getVisibility() {
        return Math.round(getFromSplit(visibility));
    }

    private double getFromSplit(double[] split) {
        double one = split[0], two = split[1];
        if (one != two) {
            double min = Math.min(one, two);
            double max = Math.max(one, two);
            return random.nextDouble(min, max);
        } else {
            return one;
        }
    }

    private double[] getSplit(String string) {
        if (string == null) {
            return new double[]{-1, -1};
        }
        String[] split = string.split(";");
        return new double[]{Double.parseDouble(split[0]), Double.parseDouble(split[1])};
    }
}
