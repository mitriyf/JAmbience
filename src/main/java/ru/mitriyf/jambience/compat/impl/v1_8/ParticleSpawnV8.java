package ru.mitriyf.jambience.compat.impl.v1_8;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.mitriyf.jambience.compat.abstraction.ParticleSpawn;

import java.lang.reflect.Method;

public class ParticleSpawnV8 implements ParticleSpawn {
    private final Method method;

    public ParticleSpawnV8(Method method) {
        this.method = method;
    }

    @Override
    public void create(Player player, Object objectParticle, Location location, int count, double offsetX, double offsetY, double offsetZ, Color color, float size, float speed, int radius) throws Exception {
        Effect effect = (Effect) objectParticle;
        if (color == null) {
            method.invoke(player.spigot(), location, effect, 0, 0, (float) offsetX, (float) offsetY, (float) offsetZ, speed, count, radius);
        } else {
            method.invoke(player.spigot(), location, effect, 0, 0, getColor(color.getRed()), getColor(color.getGreen()), getColor(color.getBlue()), speed, count, radius);
        }
    }

    private float getColor(int color) {
        return (float) color / 255;
    }
}
