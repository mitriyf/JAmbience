package ru.mitriyf.jambience.events;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.utils.Utils;
import ru.mitriyf.jambience.values.Values;
import ru.mitriyf.jambience.weather.Weather;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Events {
    private final Utils utils;
    private final Values values;
    private final Weather weather;
    private final JAmbience plugin;
    private final ThreadLocalRandom rnd;
    private final BukkitScheduler scheduler;
    private final Map<UUID, BukkitTask> sound = new HashMap<>();
    private final Map<UUID, BukkitTask> temperature = new HashMap<>();
    private BukkitTask weatherTask, fullTime, update;

    public Events(JAmbience plugin) {
        this.plugin = plugin;
        rnd = plugin.getRnd();
        utils = plugin.getUtils();
        values = plugin.getValues();
        weather = plugin.getWeather();
        scheduler = plugin.getServer().getScheduler();
    }

    public void start() {
        if (values.isWeatherEnabled() && update == null) {
            update = scheduler.runTaskTimerAsynchronously(plugin, weather::update, 0, values.getApiUpdate() * 20L);
            weatherTask = scheduler.runTaskTimerAsynchronously(plugin, this::weather, 2, 2);
            fullTime = scheduler.runTaskTimerAsynchronously(plugin, this::setFullTime, 0, 30 * 20);
        } else if (update != null) {
            update.cancel();
            weatherTask.cancel();
            fullTime.cancel();
            update = null;
            weatherTask = null;
            fullTime = null;
            start();
        }
    }

    private void weather() {
        double[] tps = plugin.getServer().getTPS();
        Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
        if (tps[0] <= 19.5 || onlinePlayers.isEmpty()) {
            return;
        }
        for (Player p : onlinePlayers) {
            if (values.isViewDistance() && p.getViewDistance() != weather.getVisibility() && p.getLocation().getWorld() != null) {
                p.setViewDistance(weather.getVisibility());
            }
            UUID uuid = p.getUniqueId();
            if (!utils.isTemperature() && values.isFreeze() && weather.getFreezeTicks() != 0 && temperature.get(uuid) == null) {
                startTemp(p);
            }
            if (sound.get(uuid) == null) {
                startSound(p, uuid);
            }
            startSnow(p);
        }
    }

    private void startSound(Player p, UUID uuid) {
        p.playSound(p.getLocation(), Sound.ITEM_ELYTRA_FLYING, (float) weather.getWindVolume(), 1F);
        sound.put(uuid, scheduler.runTaskLater(plugin, () -> sound.remove(uuid), 200));
    }

    private void startTemp(Player p) {
        temperature.put(p.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                int freezeTicks = weather.getFreezeTicks();
                if (freezeTicks < 50) {
                    p.setFreezeTicks(freezeTicks);
                    if (freezeTicks == 0) {
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
        DateFormat df = new SimpleDateFormat("HH:mm");
        String[] date = df.format(new Date()).split(":");
        int h = utils.formatInt(date[0]);
        int m = utils.formatInt(date[1]);
        long fullTime = getTime(h) + Math.round(m * 16.7);
        for (World w : plugin.getServer().getWorlds()){
            scheduler.runTask(plugin, () -> w.setFullTime(fullTime));
        }
    }

    private int getTime(int h) {
        if (h > 6) {
            return h * 1000 - 6000;
        } else {
            return h * 1000 + 18000;
        }
    }
}
