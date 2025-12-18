package ru.mitriyf.jambience.utils.common;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jambience.JAmbience;
import ru.mitriyf.jambience.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

@SuppressWarnings("DataFlowIssue")
public class TorchUtils {
    private final Map<String, Object> chunkList = new HashMap<>();
    private final BukkitScheduler scheduler;
    private final ThreadLocalRandom rnd;
    private final JAmbience plugin;
    private final ItemStack stick;
    private final Logger logger;
    private final Utils utils;
    private final File worlds;

    public TorchUtils(Utils utils, JAmbience plugin) {
        this.utils = utils;
        this.plugin = plugin;
        rnd = plugin.getRnd();
        logger = plugin.getLogger();
        stick = new ItemStack(Material.STICK);
        scheduler = plugin.getServer().getScheduler();
        worlds = new File(plugin.getDataFolder(), "worlds");
        for (World world : plugin.getServer().getWorlds()) {
            updateChunks(world);
        }
    }

    public void saveTorch(Block block, boolean remove) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            World world = block.getWorld();
            File chunks = new File(worlds, world.getName());
            Material material = block.getType();
            boolean isTorch = material == Material.TORCH || material == Material.WALL_TORCH;
            if (!worlds.exists() && !chunks.exists() && !chunks.mkdirs()) {
                logger.warning("Error create mkdirs!");
            } else if (!remove && !isTorch && !block.isSolid()) {
                return;
            }
            Chunk chunk = block.getChunk();
            String fileName = chunk.getX() + "_" + chunk.getZ() + ".yml";
            File file = new File(chunks, fileName);
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        logger.warning("Error create file torch.");
                    }
                } catch (Exception e) {
                    logger.warning("Error save torch: " + e);
                }
            }
            Object lock = chunkList.computeIfAbsent(fileName, k -> new Object());
            synchronized (lock) {
                YamlConfiguration dataFile = YamlConfiguration.loadConfiguration(file);
                try {
                    ConfigurationSection zSection = getZSection(dataFile, block);
                    ConfigurationSection blocksSection = zSection.getConfigurationSection("blocks");
                    if (blocksSection == null) {
                        blocksSection = zSection.createSection("blocks");
                    }
                    ConfigurationSection torchesSection = zSection.getConfigurationSection("torches");
                    if (torchesSection == null) {
                        torchesSection = zSection.createSection("torches");
                    }
                    String yBlock = String.valueOf(block.getY());
                    if (remove) {
                        torchesSection.set(yBlock, null);
                        blocksSection.set(yBlock, null);
                    } else {
                        ConfigurationSection blockSection = blocksSection;
                        if (isTorch) {
                            blockSection = torchesSection;
                            CountDownLatch latch = new CountDownLatch(1);
                            scheduler.runTask(plugin, () -> {
                                for (int i = block.getY() + 1; i < world.getMaxHeight(); i++) {
                                    Block blockNew = block.getWorld().getBlockAt(block.getX(), i, block.getZ());
                                    check(dataFile, blockNew);
                                }
                                latch.countDown();
                            });
                            try {
                                latch.await();
                            } catch (Exception ignored) {
                            }
                        }
                        if (blockSection.getConfigurationSection(yBlock) == null) {
                            blockSection.createSection(yBlock);
                        }
                    }
                } finally {
                    updateValues(dataFile, file, fileName, world);
                }
            }
        });
    }

    private void check(YamlConfiguration dataFile, Block block) {
        if (!block.isSolid()) {
            return;
        }
        ConfigurationSection zSection = getZSection(dataFile, block);
        ConfigurationSection blocksSection = zSection.getConfigurationSection("blocks");
        if (blocksSection == null) {
            blocksSection = zSection.createSection("blocks");
        }
        String yBlock = String.valueOf(block.getY());
        if (blocksSection.getConfigurationSection(yBlock) == null) {
            blocksSection.createSection(yBlock);
        }
    }

    private void updateValues(YamlConfiguration chunk, File file, String fileName, World world) {
        ConfigurationSection locations = chunk.getConfigurationSection("locations");
        for (String x : locations.getKeys(false)) {
            ConfigurationSection xSection = locations.getConfigurationSection(x);
            for (String z : xSection.getKeys(false)) {
                ConfigurationSection zCoords = xSection.getConfigurationSection(z);
                ConfigurationSection torches = zCoords.getConfigurationSection("torches");
                for (String torch : torches.getKeys(false)) {
                    int yTorch = Integer.parseInt(torch);
                    boolean hasBlock = false;
                    for (String block : zCoords.getConfigurationSection("blocks").getKeys(false)) {
                        int yBlock = Integer.parseInt(block);
                        if (yBlock > yTorch) {
                            hasBlock = true;
                            break;
                        }
                    }
                    ConfigurationSection torchSection = torches.getConfigurationSection(torch);
                    if (hasBlock) {
                        torchSection.set("noBreak", true);
                    } else {
                        torchSection.set("noBreak", null);
                    }
                    if (!world.isClearWeather()) {
                        if (!torchSection.getBoolean("noBreak")) {
                            torches.set(torch, null);
                            scheduler.runTask(plugin, () -> {
                                Location location = new Location(world, utils.formatInt(x), yTorch, utils.formatInt(z));
                                Block block = location.getBlock();
                                Material material = block.getType();
                                if (material != Material.TORCH && material != Material.WALL_TORCH) {
                                    return;
                                }
                                stick.setAmount(rnd.nextInt(2) + 1);
                                location.getBlock().setType(Material.AIR);
                                world.dropItem(location, stick);
                            });
                        }
                    }
                }
            }
        }
        try {
            chunk.save(file);
        } catch (Exception e) {
            logger.warning("Error saving file: " + e);
        }
        chunkList.remove(fileName);
    }

    private void updateChunk(Chunk chunk) {
        World world = chunk.getWorld();
        File chunks = new File(worlds, world.getName());
        if (!worlds.exists() || !chunks.exists()) {
            return;
        }
        String fileName = chunk.getX() + "_" + chunk.getZ() + ".yml";
        File file = new File(chunks, fileName);
        if (!file.exists()) {
            return;
        }
        Object lock = chunkList.computeIfAbsent(fileName, k -> new Object());
        synchronized (lock) {
            YamlConfiguration dataFile = YamlConfiguration.loadConfiguration(file);
            updateValues(dataFile, file, fileName, world);
        }
    }

    public void updateChunks(World world) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            for (Chunk chunk : world.getLoadedChunks()) {
                updateChunk(chunk);
            }
        });
    }

    private ConfigurationSection getZSection(YamlConfiguration dataFile, Block block) {
        ConfigurationSection locations = dataFile.getConfigurationSection("locations");
        if (locations == null) {
            locations = dataFile.createSection("locations");
        }
        String xValue = String.valueOf(block.getX());
        ConfigurationSection xSection = locations.getConfigurationSection(xValue);
        if (xSection == null) {
            xSection = locations.createSection(xValue);
        }
        String zValue = String.valueOf(block.getZ());
        ConfigurationSection zSection = xSection.getConfigurationSection(zValue);
        if (zSection == null) {
            zSection = xSection.createSection(zValue);
        }
        return zSection;
    }
}
