package ru.mitriyf.jambience.compat.abstraction;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ParticleSpawn {
    void create(Player player, Object objectParticle, Location location, int count, double offsetX, double offsetY, double offsetZ, Color color, float size, float speed, int radius) throws Exception;
}
