package ru.mitriyf.jambience.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.values.Values;
import ru.mitriyf.jambience.weather.Weather;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Events {
    private final Values values;
    private final Weather weather;
    private final JAmbience plugin;
    private final ThreadLocalRandom rnd;
    private final BukkitScheduler scheduler;
    private final Map<UUID, BukkitTask> sound = new HashMap<>();
    private final Map<UUID, BukkitTask> temperature = new HashMap<>();
    private BukkitTask weatherTask, fullTime, update;
    @Setter
    private boolean on = true;

    public Events(JAmbience plugin) {
        this.plugin = plugin;
        rnd = plugin.getRnd();
        values = plugin.getValues();
        weather = plugin.getWeather();
        scheduler = plugin.getScheduler();
    }

    public void start() {
        if (values.isWeatherEnabled() && update == null) {
            update = scheduler.runTaskTimer(plugin, weather::update, 0, 600 * 20);
            weatherTask = scheduler.runTaskTimer(plugin, this::weather, 2, 2);
            fullTime = scheduler.runTaskTimer(plugin, this::setFullTime, 0, 30 * 20);
        } else if (update != null) {
            update.cancel();
            weatherTask.cancel();
            fullTime.cancel();
            update = null;
            weatherTask = null;
            fullTime = null;
        }
    }

    private void weather() {
        scheduler.runTaskAsynchronously(plugin, () -> {
            if (!on) {
                return;
            } else if (Bukkit.getOnlinePlayers().isEmpty()) {
                on = false;
                return;
            }
            double[] tps = Bukkit.getTPS();
            if (tps[0] <= 19.5) {
                return;
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getViewDistance() != weather.getVisibility() && p.getLocation().getWorld() != null) {
                    p.setViewDistance(weather.getVisibility());
                }
                if (values.isFreeze() && weather.getFreezeTicks() != 0 && temperature.get(p.getUniqueId()) == null) {
                    startTemp(p);
                }
                if (sound.get(p.getUniqueId()) == null) {
                    startSound(p);
                }
                startSnow(p);
            }
        });
    }

    private void startSound(Player p) {
        p.playSound(p.getLocation(), Sound.ITEM_ELYTRA_FLYING, (float) weather.getWindVolume(), 1F);
        sound.put(p.getUniqueId(), scheduler.runTaskLater(plugin, () -> sound.remove(p.getUniqueId()), 200));
    }

    private void startTemp(Player p) {
        temperature.put(p.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                if (weather.getFreezeTicks() < 50) {
                    p.setFreezeTicks(weather.getFreezeTicks());
                    if (weather.getFreezeTicks() == 0) {
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 1, 1));
    }

    private void startSnow(Player p) {
        double snowAmount = weather.getSnowAmount();
        double radius = values.getRadiusSnow();
        for (int count = 0; count < snowAmount; count++) {
            double x = rnd.nextDouble(-radius, radius);
            double y = rnd.nextDouble(-radius, radius);
            double z = rnd.nextDouble(-radius, radius);
            spawnParticle(p, p.getLocation().clone().add(x, y, z));
        }
    }

    private void spawnParticle(Player p, Location loc) {
        double speed = (weather.getWindSpeed() / 3) * rnd.nextDouble(0.90, 1.10);
        p.spawnParticle(Particle.CLOUD, loc, 0, weather.getOffsetX() * speed, weather.getFallSpeed(), weather.getOffsetZ() * speed);
    }

    private void setFullTime() {
        scheduler.runTaskAsynchronously(plugin, () -> {
            DateFormat df = new SimpleDateFormat("HH:mm");
            String[] date = df.format(new Date()).split(":");
            int h = Integer.parseInt(date[0]);
            int m = Integer.parseInt(date[1]);
            long fullTime = getTime(h) + Math.round(m * 16.7);
            for (World w : plugin.getServer().getWorlds()){
                scheduler.runTask(plugin, () -> w.setFullTime(fullTime));
            }
        });
    }

    private int getTime(int h) {
        if (h > 6) {
            return h * 1000 - 6000;
        } else {
            return h * 1000 + 18000;
        }
    }
}
