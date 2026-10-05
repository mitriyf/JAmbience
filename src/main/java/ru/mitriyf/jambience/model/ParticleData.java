package ru.mitriyf.jambience.model;

import lombok.Getter;
import org.bukkit.Color;

@Getter
public class ParticleData {
    private final int count;
    private final Color color;
    private final Object particle;
    private final float size, speed;

    public ParticleData(Object particle, Color color, int count, float size, float speed) {
        this.particle = particle;
        this.color = color;
        this.count = count;
        this.size = size;
        this.speed = speed;
    }
}
