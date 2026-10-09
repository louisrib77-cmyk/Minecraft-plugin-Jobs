package de.jobs;

import de.jobs.listener.BlockListener;
import de.jobs.listener.EntityListener;
import de.jobs.listener.JoinQuitListener;
import de.jobs.listener.MoveListener;
import de.jobs.listener.ShopListener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class JobsPlugin extends JavaPlugin {

    /** Wandelt Texte mit Farb-Tags wie <gold>Text</gold> in Chat-Komponenten um. */
    public static final MiniMessage MM = MiniMessage.miniMessage();

    private RewardConfig rewards;
    private JobManager jobManager;
    private DataManager dataManager;
    private PlacedBlockTracker placedBlocks;
    private ShopManager shopManager;
    private ShopGui shopGui;
    private NamespacedKey spawnerKey;
    private MoneyScoreboard moneyScoreboard;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        rewards = new RewardConfig(this);
        jobManager = new JobManager(this);
        dataManager = new DataManager(this);
        placedBlocks = new PlacedBlockTracker(this);
        shopManager = new ShopManager(this);
        shopGui = new ShopGui(this);
        moneyScoreboard = new MoneyScoreboard(this);
        spawnerKey = new NamespacedKey(this, "spawner_spawned");

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JoinQuitListener(this), this);
        pm.registerEvents(new BlockListener(this), this);
        pm.registerEvents(new EntityListener(this), this);
        pm.registerEvents(new MoveListener(this), this);
        pm.registerEvents(new ShopListener(this), this);

        JobsCommand jobsCommand = new JobsCommand(this);
        PluginCommand command = getCommand("jobs");
        if (command != null) {
            command.setExecutor(jobsCommand);
            command.setTabCompleter(jobsCommand);
        }

        // Spieler, die schon online sind (z. B. nach einem Neuladen des Plugins), laden
        for (Player player : getServer().getOnlinePlayers()) {
            dataManager.load(player.getUniqueId());
            moneyScoreboard.show(player);
        }

        // Autosave alle 5 Minuten (6000 Ticks)
        getServer().getScheduler().runTaskTimer(this, dataManager::saveAll, 6000L, 6000L);

        // Geld-Rangliste jede Sekunde aktualisieren (20 Ticks)
        getServer().getScheduler().runTaskTimer(this, moneyScoreboard::updateAll, 20L, 20L);

        getLogger().info("JobsPlugin aktiviert.");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
    }

    public RewardConfig getRewards() { return rewards; }
    public JobManager getJobManager() { return jobManager; }
    public DataManager getDataManager() { return dataManager; }
    public PlacedBlockTracker getPlacedBlocks() { return placedBlocks; }
    public ShopManager getShopManager() { return shopManager; }
    public ShopGui getShopGui() { return shopGui; }
    public NamespacedKey getSpawnerKey() { return spawnerKey; }
    public MoneyScoreboard getMoneyScoreboard() { return moneyScoreboard; }

    /** Geldbetrag mit zwei Nachkommastellen, z. B. 12,50 */
    public static String money(double value) {
        return String.format(Locale.GERMAN, "%.2f", value);
    }

    /** Materialname lesbar machen: RAW_IRON -> raw iron */
    public static String prettyName(Material material) {
        return material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    /** Belohnungen gibt es nur in Survival und Adventure, nicht im Kreativmodus. */
    public static boolean isSurvival(Player player) {
        GameMode mode = player.getGameMode();
        return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }
}
