package ru.mitriyf.jambience.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.manager.WeatherManager;
import ru.mitriyf.jambience.values.Values;

import java.time.LocalTime;

public class Placeholders extends PlaceholderExpansion {
    private final WeatherManager weatherManager;
    private final Values values;

    public Placeholders(JAmbience plugin) {
        values = plugin.getValues();
        weatherManager = plugin.getWeatherManager();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String ind) {
        if (player == null) {
            return null;
        }
        String[] args = ind.split("_");
        if (args.length >= 1) {
            boolean isCold = weatherManager.isCold();
            switch (args[0].toLowerCase()) {
                case "windspeed": {
                    return String.valueOf(Math.round((float) weatherManager.getWindSpeed()));
                }
                case "winddeg": {
                    return String.valueOf(Math.round(weatherManager.getWindDeg()));
                }
                case "temperature": {
                    String tempStr = isCold ? values.getSnowTemperature() : values.getSunnyTemperature();
                    return tempStr.replace("%temperature%", String.valueOf(weatherManager.getTemperature()));
                }
                case "color": {
                    return isCold ? values.getSnowColor() : values.getSunnyColor();
                }
                case "emoji": {
                    return isCold ? values.getSnowEmoji() : values.getSunnyEmoji();
                }
                case "visibility": {
                    return String.valueOf(weatherManager.getVisibility());
                }
                case "event": {
                    String status = weatherManager.getStatus().toLowerCase();
                    return values.getWeatherTypes().getOrDefault(status, status);
                }
                case "time": {
                    return LocalTime.now(values.getZoneId()).format(values.getDateTimeFormatter());
                }
                case "name": {
                    return values.getCityName();
                }
                case "amount": {
                    return String.valueOf(Math.round((float) weatherManager.getCurrentAmount()));
                }
            }
        }
        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return "Mitriyf";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "JAmbience";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1";
    }
}