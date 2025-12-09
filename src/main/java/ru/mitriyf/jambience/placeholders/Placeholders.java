package ru.mitriyf.jambience.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.values.Values;
import ru.mitriyf.jambience.weather.Weather;

public class Placeholders extends PlaceholderExpansion {
    private final Weather weather;
    private final Values values;

    public Placeholders(JAmbience plugin) {
        weather = plugin.getWeather();
        values = plugin.getValues();
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String ind) {
        if (p == null) {
            return null;
        }
        String[] args = ind.split("_");
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "windspeed": {
                    return String.valueOf(Math.round((float) weather.getWindSpeed()));
                }
                case "temp": {
                    int temp = Math.round((float) weather.getTemp());
                    if (temp <= 12) {
                        return values.getSnow().replace("%temperature%", String.valueOf(temp));
                    } else {
                        return values.getSunny().replace("%temperature%", String.valueOf(temp));
                    }
                }
                case "visibility": {
                    return String.valueOf(weather.getVisibility());
                }
                case "event": {
                    String status = weather.getStatus().toLowerCase();
                    return values.getWeatherTypes().getOrDefault(status, status);
                }
                case "snow": {
                    return String.valueOf(Math.round((float) weather.getSnowAmount()));
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
        return "1.0.0";
    }
}