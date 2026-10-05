package ru.mitriyf.jambience.utils.particle.impl;

import org.bukkit.Effect;
import ru.mitriyf.jambience.utils.particle.ParticleType;

public class EffectObject implements ParticleType {
    @Override
    public Object getParticle(String string) {
        return Effect.valueOf(string);
    }
}
