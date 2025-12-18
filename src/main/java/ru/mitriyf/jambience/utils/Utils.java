package ru.mitriyf.jambience.utils;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.utils.common.CommonUtils;
import ru.mitriyf.jambience.utils.common.TorchUtils;
import ru.mitriyf.jambience.values.Values;
import ru.mitriyf.jambience.utils.actions.Action;
import ru.mitriyf.jambience.utils.actions.ActionType;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Getter
public class Utils {
    private final Values values;
    private final Logger logger;
    private final JAmbience plugin;
    private final CountDownLatch latch;
    private final TorchUtils torchUtils;
    private final CommonUtils commonUtils;
    private final BukkitScheduler scheduler;
    private boolean temperature = false, actionBar = false, bar = false;

    public Utils(JAmbience plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        logger = plugin.getLogger();
        latch = new CountDownLatch(1);
        scheduler = plugin.getServer().getScheduler();
        torchUtils = new TorchUtils(this, plugin);
        commonUtils = new CommonUtils(this, plugin);
    }

    public void setup() {
        int version = plugin.getVersion();
        if (version < 17) {
            temperature = true;
        }
        if (version < 13) {
            if (version < 9) {
                bar = true;
            }
        }
        if (version < 11) {
            actionBar = true;
        }
    }

    public void sendMessage(CommandSender sender, List<Action> actions) {
        sendMessage(sender, actions, null, null);
    }

    public void sendMessage(CommandSender sender, List<Action> actions, String[] search, String[] replace) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            for (Action action : actions) {
                sendSender(sender, action, search, replace);
            }
        });
    }

    private void sendSender(CommandSender sender, Action action, String[] search, String[] replace) {
        ActionType type = action.getType();
        String context = replaceEach(action.getContext(), search, replace);
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
            case DELAY:
                try {
                    if (latch.await(formatInt(context) * 50L, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                } catch (Exception ignored) {
                }
                break;
            default:
                sendMessage(sender, context);
                break;
        }
    }

    private String replaceEach(String text, String[] searchList, String[] replacementList) {
        if (text.isEmpty() || searchList == null || replacementList == null) {
            return text;
        }
        final StringBuilder result = new StringBuilder(text);
        for (int i = 0; i < searchList.length; i++) {
            final String search = searchList[i];
            final String replacement = replacementList[i];
            int start = 0;
            while ((start = result.indexOf(search, start)) != -1) {
                result.replace(start, start + search.length(), replacement);
                start += replacement.length();
            }
        }

        return result.toString();
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
