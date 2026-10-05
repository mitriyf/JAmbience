package ru.mitriyf.jambience.utils.particle.impl;

import org.bukkit.Particle;
import ru.mitriyf.jambience.utils.particle.ParticleType;

public class ParticleObject implements ParticleType {
    @Override
    public Object getParticle(String string) {
        return Particle.valueOf(string);
    }
}
