package ru.mitriyf.jambience.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.manager.WeatherManager;
import ru.mitriyf.jambience.values.Values;

public class PlayerMoveListener implements Listener {
    private final WeatherManager weatherManager;
    private final Values values;

    public PlayerMoveListener(JAmbience plugin) {
        values = plugin.getValues();
        weatherManager = plugin.getWeatherManager();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!values.isWalkEnabled()) {
            return;
        }
        Player player = event.getPlayer();
        Location location = event.getTo();
        Location fromLocation = event.getFrom();
        if (checkCoords(fromLocation, location) || values.getWorldType().notContainsWorld(location.getWorld()) || values.getBiomeType().notContainsBiome(location.getBlock().getBiome()) || player.isFlying() || player.isInsideVehicle()) {
            return;
        }
        Vector windVector = new Vector(weatherManager.getOffsetX(), 0, weatherManager.getOffsetZ());
        Vector playerVector = location.toVector().subtract(fromLocation.toVector()).setY(0);
        float defaultSpeed = values.getDefaultSpeed();
        if (windVector.lengthSquared() < 0.0001 || playerVector.lengthSquared() < 0.0001) {
            resetSpeed(player, defaultSpeed);
            return;
        }
        windVector.normalize();
        playerVector.normalize();
        double dot = playerVector.dot(windVector);
        float windSpeed = Math.min(1f, (float) Math.max(0f, (float) weatherManager.getWindSpeed() / values.getWindMaxSpeed()));
        float modifier = values.getModifier() * windSpeed;
        float walkSpeed = defaultSpeed + (float) (dot * modifier);
        walkSpeed = Math.max(values.getWalkMinSpeed(), Math.min(values.getWalkMaxSpeed(), walkSpeed));
        if (Math.abs(player.getWalkSpeed() - walkSpeed) > 0.005) {
            player.setWalkSpeed(walkSpeed);
        }
    }

    private void resetSpeed(Player player, float defaultSpeed) {
        if (player.getWalkSpeed() != defaultSpeed) {
            player.setWalkSpeed(defaultSpeed);
        }
    }

    private boolean checkCoords(Location fromLocation, Location location) {
        return fromLocation.getX() == location.getX() && fromLocation.getY() == location.getY() && fromLocation.getZ() == location.getZ();
    }
}
