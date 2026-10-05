package ru.mitriyf.jambience.utils;

import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.compat.abstraction.ParticleSpawn;
import ru.mitriyf.jambience.compat.abstraction.SetVisibility;
import ru.mitriyf.jambience.compat.impl.v1_12.ParticleSpawnV12;
import ru.mitriyf.jambience.compat.impl.v1_13.ParticleSpawnV13;
import ru.mitriyf.jambience.compat.impl.v1_8.ParticleSpawnV8;
import ru.mitriyf.jambience.compat.impl.v1_8.SetVisibilityV8;
import ru.mitriyf.jambience.compat.impl.v1_9.SetVisibilityV9;
import ru.mitriyf.jambience.utils.actions.Action;
import ru.mitriyf.jambience.utils.actions.ActionType;
import ru.mitriyf.jambience.utils.common.CommonUtils;
import ru.mitriyf.jambience.utils.json.Json;
import ru.mitriyf.jambience.utils.particle.ParticleType;
import ru.mitriyf.jambience.utils.particle.impl.EffectObject;
import ru.mitriyf.jambience.utils.particle.impl.ParticleObject;
import ru.mitriyf.jambience.values.Values;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Getter
public class Utils {
    private final Values values;
    private final Logger logger;
    private final JAmbience plugin;
    private final CommonUtils commonUtils;
    private final BukkitScheduler scheduler;
    private boolean temperatureDisabled, actionBar, otherParticles;
    private SetVisibility setVisibility;
    private ParticleSpawn particleSpawn;
    private ParticleType particleType;

    public Utils(JAmbience plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        logger = plugin.getLogger();
        scheduler = plugin.getServer().getScheduler();
        commonUtils = new CommonUtils(this, plugin);
    }

    @SuppressWarnings("all")
    public void setup() {
        int version = plugin.getVersion();
        if (version < 17) {
            temperatureDisabled = true;
            if (version < 11) {
                actionBar = true;
                if (version < 9) {
                    otherParticles = true;
                }
            }
        }
        if (version < 13) {
            if (version < 9) {
                try {
                    particleSpawn = new ParticleSpawnV8(Player.Spigot.class.getMethod("playEffect", Location.class, Effect.class, int.class, int.class, float.class, float.class, float.class, float.class, int.class, int.class));
                } catch (Exception e) {
                    logger.info("Effects is not supported on your core: " + e);
                }
            } else {
                particleSpawn = new ParticleSpawnV12();
            }
        } else {
            particleSpawn = new ParticleSpawnV13();
        }
        try {
            if (version < 9) {
                particleType = new EffectObject();
                setVisibility = new SetVisibilityV8(Player.Spigot.class.getMethod("setViewDistance", int.class));
            } else {
                particleType = new ParticleObject();
                Player.class.getMethod("setViewDistance", int.class);
                setVisibility = new SetVisibilityV9();
            }
        } catch (Exception e) {
            logger.info("Visibility/Effects is not supported on your core: " + e);
        }
    }

    public void sendMessage(CommandSender sender, List<Action> actions) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int delayTicks = 0;
                for (Action action : actions) {
                    if (action.getType() == ActionType.DELAY) {
                        try {
                            delayTicks += formatInt(action.getContext());
                        } catch (Exception ignored) {
                        }
                        continue;
                    }
                    if (delayTicks > 0) {
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> sendSender(sender, action), delayTicks);
                    } else {
                        sendSender(sender, action);
                    }
                }
            }
        }.runTask(plugin);
    }

    private void sendSender(CommandSender sender, Action action) {
        ActionType type = action.getType();
        String context = action.getContext();
        switch (type) {
            case CONSOLE:
                commonUtils.dispatchConsole(context);
                break;
            case BROADCAST:
                commonUtils.broadcast(context);
                break;
            case LOG:
                log(context);
                break;
            default:
                sendMessage(sender, context);
                break;
        }
    }

    public Object parse(String s) {
        Json j = new Json(s);
        j.skipWs();
        return j.parseValue();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> asObject(Object o) {
        return (Map<String, Object>) o;
    }

    @SuppressWarnings("unchecked")
    public List<Object> asArray(Object o) {
        return (List<Object>) o;
    }

    public double asDouble(Object o) {
        return ((Number) o).doubleValue();
    }

    public int asInt(Object o) {
        return ((Number) o).intValue();
    }

    private void sendMessage(CommandSender sender, String text) {
        sender.sendMessage(formatString(text));
    }

    public String formatString(String s) {
        return values.getColorizer().colorize(s);
    }

    public int formatInt(String text) {
        return Integer.parseInt(text);
    }

    private void log(String log) {
        logger.info(log);
    }
}