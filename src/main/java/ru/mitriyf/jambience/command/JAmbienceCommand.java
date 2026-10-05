package ru.mitriyf.jambience.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.manager.WeatherManager;
import ru.mitriyf.jambience.mode.AutoMode;
import ru.mitriyf.jambience.utils.Utils;
import ru.mitriyf.jambience.values.Values;

@SuppressWarnings("NullableProblems")
public class JAmbienceCommand implements CommandExecutor {
    private final WeatherManager weatherManager;
    private final JAmbience plugin;
    private final Values values;
    private final Utils utils;

    public JAmbienceCommand(JAmbience plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        values = plugin.getValues();
        weatherManager = plugin.getWeatherManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        boolean permission = sender.hasPermission("jambience.use");
        if (!permission || (args.length != 1 && args.length != 3)) {
            if (permission) {
                utils.sendMessage(sender, values.getHelp());
            } else {
                utils.sendMessage(sender, values.getNoperm());
            }
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "status": {
                weatherManager.getInformation(sender);
                return false;
            }
            case "reload": {
                weatherManager.setUpdated(false);
                reloadSmoothly(sender);
                return false;
            }
            case "reloadsmoothly": {
                reloadSmoothly(sender);
                return false;
            }
            case "update": {
                weatherManager.setUpdated(false);
                updateSmoothly(sender);
                return false;
            }
            case "updatesmoothly": {
                updateSmoothly(sender);
                return false;
            }
            case "set": {
                setWeatherValue(sender, args);
                return false;
            }
            default: {
                utils.sendMessage(sender, values.getHelp());
                return false;
            }
        }
    }

    private void reloadSmoothly(CommandSender sender) {
        plugin.getValues().setup();
        sender.sendMessage("§aSuccessfully!");
    }

    private void updateSmoothly(CommandSender sender) {
        AutoMode autoMode = values.getAutoMode();
        if (autoMode != null) {
            autoMode.processUpdate();
        } else {
            values.getManualMode().processUpdate();
        }
        sender.sendMessage("§aSuccessfully. §eWait for it...");
    }

    private void setWeatherValue(CommandSender sender, String[] args) {
        if (args.length == 1) {
            utils.sendMessage(sender, values.getHelp());
            return;
        }
        double i;
        try {
            i = Double.parseDouble(args[2]);
        } catch (Exception e) {
            sender.sendMessage("§cInsert a number, not text.");
            return;
        }
        setValue(sender, args[1].toLowerCase(), i);
    }

    private void setValue(CommandSender sender, String key, double i) {
        switch (key) {
            case "windspeed": {
                weatherManager.setWindSpeed((int) i);
                break;
            }
            case "temperature": {
                weatherManager.setTemperature((int) i);
                break;
            }
            case "currentamount": {
                weatherManager.setCurrentAmount((int) i);
                break;
            }
            case "windvolume": {
                weatherManager.setWindVolume(i);
                break;
            }
            case "fallspeed": {
                weatherManager.setFallSpeed(i);
                break;
            }
            case "freezeticks": {
                weatherManager.setFreezeTicks((int) i);
                break;
            }
            case "visibility": {
                weatherManager.setVisibility((int) i);
                break;
            }
            case "event": {
                processEvent((int) i);
                break;
            }
            case "squall": {
                processSquall((int) i);
                break;
            }
            default: {
                sender.sendMessage("§eUse the following types:\n§a[windSpeed, temperature, currentAmount, windVolume, fallSpeed, freezeTicks, visibility, event (0, 1, 2), squall (0, 1)]");
                return;
            }
        }
        sender.sendMessage("§aSuccessfully!");
    }

    private void processEvent(int i) {
        if (i == 1) {
            weatherManager.setEventData(values.getRainEvent());
        } else if (i == 2) {
            weatherManager.setEventData(values.getSnowEvent());
        } else {
            weatherManager.setEventData(null);
        }
    }

    private void processSquall(int i) {
        weatherManager.setSquall(i == 1);
    }
}