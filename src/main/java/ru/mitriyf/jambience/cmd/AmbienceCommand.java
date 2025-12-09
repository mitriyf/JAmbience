package ru.mitriyf.jambience.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.utils.Utils;
import ru.mitriyf.jambience.values.Values;
import ru.mitriyf.jambience.weather.Weather;

public class AmbienceCommand implements CommandExecutor {
    private final JAmbience plugin;
    private final Weather weather;
    private final Values values;
    private final Utils utils;

    public AmbienceCommand(JAmbience plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        weather = plugin.getWeather();
        values = plugin.getValues();
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
            case "reload": {
                plugin.getValues().setup();
                sender.sendMessage("Успешно!");
                return false;
            }
            case "update": {
                weather.update();
                sender.sendMessage("Успешно. Ожидайте...");
                return false;
            }
            case "status": {
                sender.sendMessage("[ПОЛУЧЕНО] Громкость ветра: " + weather.getWindVolume() + ". Скорость ветра: " + weather.getWindSpeed() + "м/с. Температура: " + weather.getTemp() + " градусов. Видимость: " + weather.getVisibility() + " чанков.");
                double snowAmount = weather.getSnowAmount();
                if (snowAmount > 1) {
                    sender.sendMessage("[ИВЕНТ] Сейчас идёт ивент снег.\nКоличество снега в 2 тика: " + snowAmount + "\nСкорость падения: " + weather.getFallSpeed());
                }
                return false;
            }
            case "set": {
                if (args.length == 1) {
                    utils.sendMessage(sender, values.getHelp());
                    return false;
                }
                double i;
                try {
                    i = Double.parseDouble(args[2]);
                } catch (Exception e) {
                    sender.sendMessage("Вставьте число, а не текст.");
                    return false;
                }
                switch (args[1].toLowerCase()) {
                    case "windspeed": {
                        weather.setWindSpeed(i);
                        break;
                    }
                    case "temperature": {
                        weather.setTemp(i);
                        break;
                    }
                    case "snowamount": {
                        weather.setSnowAmount(i);
                        break;
                    }
                    case "windvolume": {
                        weather.setWindVolume(i);
                        break;
                    }
                    case "fallspeed": {
                        weather.setFallSpeed(i);
                        break;
                    }
                    case "freezeticks": {
                        weather.setFreezeTicks((int) i);
                        break;
                    }
                    case "visibility": {
                        weather.setVisibility((int) i);
                        break;
                    }
                    default: {
                        sender.sendMessage("Используйте следующие типы:\n[windSpeed, temperature, snowAmount, windVolume, fallSpeed, freezeTicks, visibility]");
                        return false;
                    }
                }
                sender.sendMessage("Успешно!");
                return false;
            }
            default: {
                utils.sendMessage(sender, values.getHelp());
                return false;
            }
        }
    }
}