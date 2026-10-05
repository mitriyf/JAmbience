package ru.mitriyf.jambience.model;

import lombok.Getter;
import org.bukkit.Sound;

@Getter
public class SoundData {
    private final Sound sound;
    private final float pitch;
    private final long everyTicks;

    public SoundData(Sound sound, float pitch, long everyTicks) {
        this.sound = sound;
        this.pitch = pitch;
        this.everyTicks = everyTicks;
    }
}
