package ru.mitriyf.jambience.events;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.manager.WeatherManager;
import ru.mitriyf.jambience.model.EventData;
import ru.mitriyf.jambience.model.ParticleData;
import ru.mitriyf.jambience.model.PotionData;
import ru.mitriyf.jambience.model.SoundData;
import ru.mitriyf.jambience.utils.Utils;
import ru.mitriyf.jambience.values.Values;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

@Getter
public class WeatherEvents {
    private final Utils utils;
    private final Values values;
    private final Logger logger;
    private final JAmbience plugin;
    private final ThreadLocalRandom random;
    private final BukkitScheduler scheduler;
    private final WeatherManager weatherManager;
    private final Map<UUID, BukkitTask> soundTasks = new HashMap<>(), potionTasks = new HashMap<>(), squallTasks = new HashMap<>();
    private BukkitTask weatherTask, timeTask;

    public WeatherEvents(JAmbience plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        random = plugin.getRandom();
        values = plugin.getValues();
        logger = plugin.getLogger();
        weatherManager = plugin.getWeatherManager();
        scheduler = plugin.getServer().getScheduler();
    }

    public void setup() {
        clear();
        weatherTask = scheduler.runTaskTimer(plugin, this::processWeather, 0, values.getWeatherTicks());
        if (values.isTimeEnabled()) {
            timeTask = scheduler.runTaskTimer(plugin, this::processTime, 0, values.getTimeTicks());
        }
    }

    private void processWeather() {
        for (World world : plugin.getServer().getWorlds()) {
            if (values.getWorldType().notContainsWorld(world)) {
                continue;
            }
            processWorld(world);
            for (Player player : world.getPlayers()) {
                Location location = player.getLocation();
                if (player.isDead() || values.getBiomeType().notContainsBiome(location.getBlock().getBiome())) {
                    return;
                }
                processPlayer(player, location);
            }
        }
    }

    private void processWorld(World world) {
        EventData eventData = weatherManager.getEventData();
        if (eventData == null) {
            resetWeather(world);
            return;
        }
        if (eventData.isStormEnabled()) {
            boolean storm = weatherManager.getCurrentAmount() > eventData.getMinimum();
            if (storm != world.hasStorm()) {
                world.setStorm(storm);
            }
        } else {
            resetWeather(world);
        }
    }

    private void resetWeather(World world) {
        if (values.isResetWeather()) {
            world.setStorm(false);
            world.setThundering(false);
        }
    }

    private void processPlayer(Player player, Location location) {
        processVisibility(player);
        processFreeze(player);
        UUID uuid = player.getUniqueId();
        processSound(player, uuid, location);
        if (weatherManager.isSquall()) {
            processSquall(player, uuid);
        }
        processPotionEffect(player, uuid);
        int weatherAmount = weatherManager.getCurrentAmount();
        if (values.isPrecipitationEnabled()) {
            if (weatherAmount > 0) {
                EventData eventData = weatherManager.getEventData();
                if (eventData != null) {
                    processPrecipitation(player, eventData, weatherAmount);
                }
            }
            EventData eventData = values.getSeasonDataMap().get(weatherManager.getMonthValue());
            if (eventData != null) {
                processPrecipitation(player, eventData, (int) eventData.getCurrentAmount());
            }
        }
    }

    private void processVisibility(Player player) {
        if (values.isVisibilityEnabled()) {
            try {
                int visibility = Math.max(weatherManager.getVisibility(), 2);
                utils.getSetVisibility().setVisibility(player, visibility);
            } catch (Exception e) {
                logger.warning("An error occurred while setting the visibility for the player: " + e + ". Disabling the function.");
                values.setVisibilityEnabled(false);
            }
        }
    }

    private void processSquall(Player player, UUID uuid) {
        if (!values.isSquallEnabled() || squallTasks.get(uuid) != null || player.isFlying() || player.isInsideVehicle()) {
            return;
        }
        if (values.isSquallResetSprint() && player.isSprinting()) {
            player.setSprinting(false);
        }
        if (values.isSquallPulse()) {
            double windOffsetX = weatherManager.getOffsetX();
            double windOffsetY = weatherManager.getFallSpeed();
            double windOffsetZ = weatherManager.getOffsetZ();
            float windSpeed = Math.min(1f, (float) Math.max(0f, (float) weatherManager.getWindSpeed() / values.getWindMaxSpeed()));
            Vector windVector = new Vector(windOffsetX, windOffsetY, windOffsetZ).normalize().multiply(values.getMultiplierPulse() * windSpeed);
            player.setVelocity(player.getVelocity().add(windVector));
        }
        squallTasks.put(uuid, scheduler.runTaskLater(plugin, () -> squallTasks.remove(uuid), values.getSquallTicks()));
    }

    private void processPotionEffect(Player player, UUID uuid) {
        if (values.isPotionEnabled() && potionTasks.get(uuid) == null) {
            try {
                List<PotionData> potionDataList = values.getPotionDataMap().get(weatherManager.getVisibility());
                if (potionDataList == null) {
                    return;
                }
                PotionData potionData = potionDataList.get(random.nextInt(potionDataList.size()));
                player.addPotionEffect(potionData.getPotionEffect());
                potionTasks.put(uuid, scheduler.runTaskLater(plugin, () -> potionTasks.remove(uuid), potionData.getEveryTicks()));
            } catch (Exception e) {
                logger.warning("An error occurred while setting the potion for the player: " + e + ". Disabling the function.");
                values.setPotionEnabled(false);
            }
        }
    }

    private void processFreeze(Player player) {
        int freezeTicks = weatherManager.getFreezeTicks();
        if (!utils.isTemperatureDisabled() && values.isFreezeEnabled() && freezeTicks != 0) {
            try {
                player.setFreezeTicks(freezeTicks);
            } catch (Exception e) {
                logger.warning("An error occurred during the freezing process: " + e + ". Disabling the function.");
                values.setFreezeEnabled(false);
            }
        }
    }

    private void processSound(Player player, UUID uuid, Location location) {
        if (values.isWindEnabled() && soundTasks.get(uuid) == null) {
            try {
                List<SoundData> windSounds = values.getWindSounds();
                int size = windSounds.size();
                if (size == 0) {
                    values.setWindEnabled(false);
                    return;
                }
                SoundData soundData = windSounds.get(random.nextInt(size));
                player.playSound(location, soundData.getSound(), (float) weatherManager.getWindVolume(), soundData.getPitch());
                soundTasks.put(uuid, scheduler.runTaskLater(plugin, () -> soundTasks.remove(uuid), soundData.getEveryTicks()));
            } catch (Exception e) {
                logger.warning("An error occurred while playing the sound: " + e + ". Disabling the function.");
                values.setWindEnabled(false);
            }
        }
    }

    private void processPrecipitation(Player player, EventData eventData, int amount) {
        try {
            List<ParticleData> particleDataSet = eventData.getParticleData();
            if (particleDataSet.isEmpty()) {
                values.setPrecipitationEnabled(false);
                return;
            }
            int maxStep = eventData.getMaxStep();
            double windOffsetX = weatherManager.getOffsetX();
            double windOffsetY = weatherManager.getFallSpeed();
            double windOffsetZ = weatherManager.getOffsetZ();
            int precipitationRadius = values.getPrecipitationRadius();
            double radius = eventData.getRadius(), circleNumber = eventData.getCircleNumber();
            ParticleData particleData = particleDataSet.get(random.nextInt(particleDataSet.size()));
            Location spawnLocation = player.getLocation().clone().subtract(windOffsetX * eventData.getShiftX(), windOffsetY * eventData.getShiftY(), windOffsetZ * eventData.getShiftZ());
            for (int count = 0; count < amount; count++) {
                Location location = spawnLocation.clone().add(getCoord(maxStep, radius, circleNumber), getCoord(maxStep, radius, circleNumber), getCoord(maxStep, radius, circleNumber));
                double min = values.getPrecipitationRandomMin(), max = values.getPrecipitationRandomMax();
                double ratio;
                if (min == max) {
                    ratio = min;
                } else {
                    ratio = random.nextDouble(min, max);
                }
                double offsetX = windOffsetX * ratio, offsetY = windOffsetY * ratio, offsetZ = windOffsetZ * ratio;
                Object particle = particleData.getParticle();
                utils.getParticleSpawn().create(player, particle, location, particleData.getCount(), offsetX, offsetY, offsetZ, particleData.getColor(), particleData.getSize(), particleData.getSpeed(), precipitationRadius);
            }
        } catch (Exception e) {
            logger.warning("An error occurred while calling the particles: " + e + ". Disabling the function.");
            values.setPrecipitationEnabled(false);
        }
    }

    private void processTime() {
        try {
            LocalTime localTime = LocalTime.now(values.getZoneId());
            int hours = localTime.getHour();
            int minutes = localTime.getMinute();
            long time = ((hours - 6 + 24) % 24) * 1000 + (minutes * 1000L / 60);
            for (World world : plugin.getServer().getWorlds()) {
                if (values.getWorldType().notContainsWorld(world)) {
                    continue;
                }
                world.setTime(time);
            }
        } catch (Exception e) {
            logger.warning("An error occurred while setting the time: " + e + ". Disabling the function.");
            values.setTimeEnabled(false);
            if (timeTask != null) {
                timeTask.cancel();
                timeTask = null;
            }
        }
    }

    private void clear() {
        if (weatherTask != null) {
            weatherTask.cancel();
            weatherTask = null;
        }
        if (timeTask != null) {
            timeTask.cancel();
            timeTask = null;
        }
    }

    private double getCoord(int maxStep, double radius, double circleNumber) {
        int step = random.nextInt(-maxStep, maxStep);
        double defaultCircle = Math.pow(circleNumber, Math.abs(step));
        double circle = step < 0 ? -defaultCircle : defaultCircle;
        return circle + random.nextDouble(-radius, radius);
    }
}
