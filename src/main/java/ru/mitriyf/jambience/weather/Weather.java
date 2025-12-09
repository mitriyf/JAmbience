package ru.mitriyf.jambience.weather;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.JSONObject;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.values.Values;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

@Getter
public class Weather {
    private final Logger logger;
    private final Values values;
    private final JAmbience plugin;
    private final ThreadLocalRandom rnd;
    private final BukkitScheduler scheduler;
    @Setter
    private double windSpeed = 1, windVolume = 0, temp = 1, snowAmount = 0, fallSpeed = 1, offsetX = 1.45, offsetZ = -0.83;
    @Setter
    private int visibility = 2, freezeTicks = 0;
    private String status = "Clear";

    public Weather(JAmbience plugin) {
        this.plugin = plugin;
        rnd = plugin.getRnd();
        values = plugin.getValues();
        logger = plugin.getLogger();
        scheduler = plugin.getScheduler();
    }

    public void update() {
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                processUpdate();
            } catch (Exception e) {
                logger.warning("[ОТКЛОНЕНО] Подключение произошло неудачно. Данные не были обновлены.");
            }
        });
    }

    private void processUpdate() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(values.getApiUrl()).openConnection();
        connection.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        JSONObject weatherData = new JSONObject(response.toString());
        JSONObject main = weatherData.getJSONObject("main");
        temp = main.getDouble("temp");
        JSONObject wind = weatherData.getJSONObject("wind");
        windSpeed = wind.getDouble("speed");
        double windDeg = wind.getDouble("deg");
        double angle = Math.toRadians((windDeg + 180) % 360);
        offsetX = Math.cos(angle);
        offsetZ = Math.sin(angle);
        setWindVolume();
        snowAmount = 1;
        JSONObject weather = weatherData.getJSONArray("weather").getJSONObject(0);
        status = weather.getString("main");
        if (status.equalsIgnoreCase("snow")) {
            snowWeather(weatherData);
        }
        snowAmount = values.getMultiplierSnow() * snowAmount;
        if (temp >= -2) {
            fallSpeed = -0.22;
        } else if (temp > -20.9) {
            fallSpeed = -0.5 + 0.05 * (temp + 1.9) / -2.0;
        } else {
            fallSpeed = -(rnd.nextDouble(0.15));
        }
        if (fallSpeed > 0) {
            fallSpeed = -(rnd.nextDouble(0.15));
        }
        setTempPlayer();
        if ((weatherData.getInt("visibility") / 1000) < 2) {
            visibility = 2;
        } else {
            visibility = Math.round((float) weatherData.getInt("visibility") / 1000);
        }
        logger.info("[ПОЛУЧЕНО] Данные были обновлены. Громкость ветра: " + windVolume + ". Скорость ветра: " + windSpeed + "м/с. Температура: " + temp + " градусов. Видимость: " + visibility + " чанков.");
        if (snowAmount > 1) {
            logger.info("[ИВЕНТ] Сейчас идёт ивент снег.\nКоличество снега в 2 тика: " + snowAmount + "\nСкорость падения: " + fallSpeed);
        }
        connection.disconnect();
    }
    
    private void snowWeather(JSONObject weatherData) {
        if (weatherData.has("snow")) {
            JSONObject snow = weatherData.getJSONObject("snow");
            if (snow.has("1h")) {
                snowAmount = snow.getDouble("1h") * 100;
                return;
            }
        }
        snowAmount = values.getDefaultSnow();
    }

    private void setTempPlayer() {
        if (temp < -18) {
            freezeTicks = Integer.MAX_VALUE;
        } else if (temp < -12) {
            freezeTicks = 40;
        } else if (temp < -6) {
            freezeTicks = 20;
        } else {
            freezeTicks = 0;
        }
    }

    private void setWindVolume() {
        if (windSpeed <= 1.5) {
            windVolume = 0;
        } else if (windSpeed < 3.4) {
            windVolume = 0.1;
        } else if (windSpeed < 5.5) {
            windVolume = 0.2;
        } else if (windSpeed < 8) {
            windVolume = 0.3;
        } else if (windSpeed < 10.8) {
            windVolume = 0.5;
        } else if (windSpeed < 13.9) {
            windVolume = 0.7;
        } else if (windSpeed < 17.2) {
            windVolume = 0.9;
        } else if (windSpeed < 20.8) {
            windVolume = 1;
        } else {
            windVolume = 2;
        }
    }
}