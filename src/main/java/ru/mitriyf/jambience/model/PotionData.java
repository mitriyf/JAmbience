package ru.mitriyf.jambience.model;

import lombok.Getter;
import org.bukkit.potion.PotionEffect;

@Getter
public class PotionData {
    private final PotionEffect potionEffect;
    private final long everyTicks;

    public PotionData(PotionEffect potionEffect, long everyTicks) {
        this.potionEffect = potionEffect;
        this.everyTicks = everyTicks;
    }
}
