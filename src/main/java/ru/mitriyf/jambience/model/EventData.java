package ru.mitriyf.jambience.model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import ru.mitriyf.jambience.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Getter
public class EventData {
    private final List<ParticleData> particleData = new ArrayList<>();
    private final Logger logger;
    private final Utils utils;
    @Setter
    private long currentAmount;
    private int maxStep, minimum;
    private boolean enabled, stormEnabled;
    private double radius, defaultAmount, circleNumber, multiplier, shiftX = 15, shiftY = 0, shiftZ = 15;

    public EventData(ConfigurationSection section, Utils utils, Logger logger) {
        this.utils = utils;
        this.logger = logger;
        setupSettings(section);
    }

    private void setupSettings(ConfigurationSection section) {
        enabled = section.getBoolean("enabled");
        tryGenerateParticles(section.getStringList("particles"));
        maxStep = section.getInt("maxStep");
        circleNumber = section.getDouble("circleNumber");
        radius = section.getDouble("radius");
        multiplier = section.getDouble("multiplier");
        defaultAmount = section.getDouble("defaultAmount");
        String shiftString = section.getString("shiftCenter");
        if (shiftString != null) {
            String[] shifts = shiftString.split(";");
            shiftX = Double.parseDouble(shifts[0]);
            shiftY = Double.parseDouble(shifts[1]);
            shiftZ = Double.parseDouble(shifts[2]);
        }
        ConfigurationSection weatherSection = section.getConfigurationSection("weather");
        if (weatherSection != null) {
            stormEnabled = weatherSection.getBoolean("storm");
            minimum = weatherSection.getInt("minimum");
        } else {
            currentAmount = Math.round(defaultAmount * multiplier);
        }
    }

    private void tryGenerateParticles(List<String> particles) {
        try {
            for (String string : particles) {
                String[] split = string.split(";");
                String particleName = split[0];
                if (utils.isOtherParticles()) {
                    if (particleName.equalsIgnoreCase("WATER_WAKE") || particleName.equalsIgnoreCase("REDSTONE")) {
                        continue;
                    }
                } else if (particleName.equalsIgnoreCase("SPLASH") || particleName.equalsIgnoreCase("COLOURED_DUST")) {
                    continue;
                }
                Color color = null;
                int count = 0;
                float size = 1.0f, speed = 1.0f;
                if (split.length > 1) {
                    count = utils.formatInt(split[1]);
                    if (split.length > 2) {
                        size = Float.parseFloat(split[2]);
                        if (split.length > 3) {
                            speed = Float.parseFloat(split[3]);
                            if (split.length > 4) {
                                color = Color.fromRGB(Integer.parseInt(split[4]), Integer.parseInt(split[5]), Integer.parseInt(split[6]));
                            }
                        }
                    }
                }
                particleData.add(new ParticleData(utils.getParticleType().getParticle(particleName), color, count, size, speed));
            }
        } catch (Exception e) {
            logger.warning("Error while creating particle: " + e);
        }
    }
}