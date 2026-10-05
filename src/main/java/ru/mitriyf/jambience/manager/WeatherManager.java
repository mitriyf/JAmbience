package ru.mitriyf.jambience.manager;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.model.EventData;
import ru.mitriyf.jambience.values.Values;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Setter
@Getter
public class WeatherManager {
    private final Values values;
    private final JAmbience plugin;
    private final ThreadLocalRandom random;
    private final BukkitScheduler scheduler;
    private double windVolume = 0, fallSpeed = -0.1, windDeg = 0, offsetX = 1.45, offsetZ = -0.83;
    private int temperature = 1, visibility = 8, currentAmount = 0, windSpeed = 1, monthValue = 1;
    private BukkitTask amountTask, temperatureTask, windDegTask, windSpeedTask, visibilityTask;
    private boolean cold, updated, noVisibility, squall;
    private String status = "Clear";
    private int freezeTicks = 0;
    private EventData eventData;

    public WeatherManager(JAmbience plugin) {
        this.plugin = plugin;
        random = plugin.getRandom();
        values = plugin.getValues();
        scheduler = plugin.getServer().getScheduler();
    }

    public void updateSmoothly(EventData eventData, String status, long temperature, double windSpeed, double windDeg, long visibility) {
        scheduler.runTask(plugin, () -> {
            this.status = status;
            this.eventData = eventData;
            boolean isEventData = eventData != null;
            if (!isEventData) {
                currentAmount = 0;
            }
            int windSpeedInt = (int) Math.round(windSpeed);
            int visibilityInt = Math.max(0, Math.round(((float) visibility / values.getVisibilityMaxDistance()) * values.getVisibilityMaxChunks()));
            int windDegInt = (int) Math.round((windDeg % 360 + 360) % 360);
            if (!updated) {
                updated = true;
                if (isEventData) {
                    currentAmount = Math.round(eventData.getCurrentAmount());
                }
                this.temperature = (int) temperature;
                cold = temperature <= values.getColdTemperature();
                this.windSpeed = windSpeedInt;
                setWindDeg(windDegInt);
                this.visibility = visibilityInt;
            } else {
                if (isEventData) {
                    startAmountTask();
                }
                startTemperatureTask(temperature);
                startWindSpeedTask(windSpeedInt);
                startWindDegTask(windDegInt);
                startVisibilityTask(visibilityInt);
            }
            updateFallSpeed();
            updateWindVolume();
            updateFreezeTicks();
            monthValue = LocalDate.now().getMonthValue();
            squall = status.equalsIgnoreCase("squall");
        });
    }

    private void startAmountTask() {
        if (amountTask != null) {
            amountTask.cancel();
        }
        amountTask = new BukkitRunnable() {
            @Override
            public void run() {
                long eventCurrentAmount = eventData.getCurrentAmount();
                if (currentAmount < eventCurrentAmount) {
                    currentAmount++;
                } else if (currentAmount > eventCurrentAmount) {
                    currentAmount--;
                }
                if (currentAmount == eventCurrentAmount) {
                    amountTask = null;
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, values.getPrecipitationTicks());
    }

    private void startTemperatureTask(long newTemperature) {
        if (temperatureTask != null) {
            temperatureTask.cancel();
        }
        temperatureTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (temperature < newTemperature) {
                    temperature++;
                } else if (temperature > newTemperature) {
                    temperature--;
                }
                cold = temperature <= values.getColdTemperature();
                if (temperature == newTemperature) {
                    temperatureTask = null;
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, values.getTemperatureTicks());
    }

    private void startWindSpeedTask(int newWindSpeed) {
        if (windSpeedTask != null) {
            windSpeedTask.cancel();
        }
        windSpeedTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (windSpeed < newWindSpeed) {
                    windSpeed++;
                } else if (windSpeed > newWindSpeed) {
                    windSpeed--;
                }
                if (windSpeed == newWindSpeed) {
                    windSpeedTask = null;
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, values.getWindSpeed());
    }

    private void startWindDegTask(double newWindDeg) {
        if (windDegTask != null) {
            windDegTask.cancel();
        }
        windDegTask = new BukkitRunnable() {
            @Override
            public void run() {
                double difference = newWindDeg - windDeg;
                if (difference > 180) {
                    difference -= 360;
                } else if (difference < -180) {
                    difference += 360;
                }
                if (Math.abs(difference) > 0.5) {
                    windDeg += difference > 0 ? 1 : -1;
                } else {
                    windDeg = newWindDeg;
                }
                if (windDeg >= 360) {
                    windDeg -= 360;
                } else if (windDeg < 0) {
                    windDeg += 360;
                }
                setWindDeg(windDeg);
                if (windDeg == newWindDeg && windSpeedTask == null) {
                    windDegTask = null;
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, values.getWindDeg());
    }

    private void startVisibilityTask(int newVisibility) {
        if (visibilityTask != null) {
            visibilityTask.cancel();
        }
        visibilityTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (visibility < newVisibility) {
                    visibility++;
                } else if (visibility > newVisibility) {
                    visibility--;
                }
                noVisibility = visibility < 2;
                if (visibility == newVisibility) {
                    visibilityTask = null;
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, values.getVisibilityTicks());
    }

    private void setWindDeg(double windDeg) {
        this.windDeg = windDeg;
        double angle = Math.toRadians((windDeg + 180) % 360);
        double speed = values.getPrecipitationRatio() * windSpeed;
        offsetX = Math.cos(angle) * speed;
        offsetZ = Math.sin(angle) * speed;
    }

    private void updateFallSpeed() {
        fallSpeed = Math.max(values.getPrecipitationMinSpeed(), Math.min(values.getPrecipitationMaxSpeed(), values.getPrecipitationDifference() - (temperature * values.getPrecipitationMultiplier())));
    }

    private void updateFreezeTicks() {
        freezeTicks = temperature < 0 && cold ? Math.min(values.getFreezeMaxTicks(), (-temperature / values.getFreezeDifference()) * values.getFreezeMultiplier()) : 0;
    }

    private void updateWindVolume() {
        double minSpeed = values.getWindMinSpeed();
        double volume = Math.max(0, Math.min(1, (windSpeed - minSpeed) / (values.getWindMaxSpeed() - minSpeed)));
        windVolume = volume * (1 + Math.abs(fallSpeed));
    }

    public void getInformation(CommandSender sender) {
        sender.sendMessage("[RECEIVED] Type: " + status + ". Wind volume: " + windVolume + ". Wind speed: " + windSpeed + "m/s. Temperature: " + temperature + " degrees. Visibility: " + visibility + " chunks.");
        if (currentAmount > 0) {
            sender.sendMessage("[EVENT] There is a event going on now. The amount in " + values.getWeatherTicks() + " | " + values.getTimeTicks() + " ticks: " + currentAmount + ". The rate of fall: " + fallSpeed);
        }
    }
}
