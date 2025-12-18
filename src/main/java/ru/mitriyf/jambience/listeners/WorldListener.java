package ru.mitriyf.jambience.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.utils.Utils;
import ru.mitriyf.jambience.values.Values;

public class WorldListener implements Listener {
    private final Values values;
    private final Utils utils;

    public WorldListener(JAmbience plugin) {
        utils = plugin.getUtils();
        values = plugin.getValues();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!values.isTorchesEnabled()) {
            return;
        }
        Block block = e.getBlock();
        utils.getTorchUtils().saveTorch(block, false);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        if (!values.isTorchesEnabled()) {
            return;
        }
        utils.getTorchUtils().updateChunks(e.getWorld());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!values.isTorchesEnabled()) {
            return;
        }
        Block block = e.getBlock();
        utils.getTorchUtils().saveTorch(block, true);
    }
}