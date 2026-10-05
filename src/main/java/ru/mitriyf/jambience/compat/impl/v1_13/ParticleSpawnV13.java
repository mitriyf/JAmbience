package ru.mitriyf.jambience.compat.impl.v1_13;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import ru.mitriyf.jambience.compat.abstraction.ParticleSpawn;

public class ParticleSpawnV13 implements ParticleSpawn {
    @Override
    public void create(Player player, Object objectParticle, Location location, int count, double offsetX, double offsetY, double offsetZ, Color color, float size, float speed, int radius) {
        Particle particle = (Particle) objectParticle;
        if (color == null) {
            player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ);
        } else {
            if (particle == Particle.REDSTONE) {
                player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, new Particle.DustOptions(color, size));
            } else {
                player.spawnParticle(particle, location, count, getColor(color.getRed()), getColor(color.getGreen()), getColor(color.getBlue()), 1);
            }
        }
    }

    private double getColor(int color) {
        return (double) color / 255;
    }
}
