package de.jobs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class JobsCommand implements CommandExecutor, TabCompleter {

    private final JobsPlugin plugin;

    public JobsCommand(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl ist nur für Spieler.");
            return true;
        }

        if (args.length == 0) {
            showStatus(player);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "setshop" -> setShop(player, args);
            case "removeshop" -> removeShop(player);
            case "money" -> giveMoney(player, args);
            default -> sendUsage(player);
        }
        return true;
    }

    /** Zeigt Geld sowie Level, XP und Bonus aller Jobs. */
    private void showStatus(Player player) {
        PlayerData data = plugin.getDataManager().get(player.getUniqueId());
        JobManager jobs = plugin.getJobManager();

        player.sendMessage(JobsPlugin.MM.deserialize("<gold><bold>=== Deine Jobs ===</bold></gold>"));
        player.sendMessage(JobsPlugin.MM.deserialize(
                "<gray>Geld: <gold>" + JobsPlugin.money(data.getMoney()) + " $</gold></gray>"));

        for (Job job : Job.values()) {
            PlayerData.JobProgress progress = data.getProgress(job);
            double bonus = (jobs.multiplier(progress.getLevel()) - 1) * 100;
            player.sendMessage(JobsPlugin.MM.deserialize(String.format(Locale.GERMAN,
                    "<white>%s</white> <gray>Level %d | XP %.0f/%.0f | Bonus +%.0f %%</gray>",
                    job.getDisplayName(),
                    progress.getLevel(),
                    progress.getXp(),
                    jobs.xpNeeded(progress.getLevel()),
                    bonus)));
        }
    }

    /** Bindet die Truhe, auf die der Admin schaut, an eine Shop-Kategorie. */
    private void setShop(Player player, String[] args) {
        if (!requireAdmin(player)) return;

        if (args.length < 2) {
            player.sendMessage(JobsPlugin.MM.deserialize("<red>Benutzung: /jobs setshop [erz|holz|essen]</red>"));
            return;
        }

        String category = args[1].toLowerCase(Locale.ROOT);
        if (!plugin.getConfig().isConfigurationSection("shop.categories." + category)) {
            player.sendMessage(JobsPlugin.MM.deserialize(
                    "<red>Unbekannte Kategorie. Verfügbar: " + String.join(", ", categories()) + "</red>"));
            return;
        }

        Block chest = getTargetChest(player);
        if (chest == null) return;

        plugin.getShopManager().set(chest, category);
        player.sendMessage(JobsPlugin.MM.deserialize(
                "<green>Diese Truhe ist jetzt der Shop '" + category + "'.</green>"));
    }

    private void removeShop(Player player) {
        if (!requireAdmin(player)) return;

        Block chest = getTargetChest(player);
        if (chest == null) return;

        if (plugin.getShopManager().categoryAt(chest) == null) {
            player.sendMessage(JobsPlugin.MM.deserialize("<red>Diese Truhe ist kein Shop.</red>"));
            return;
        }

        plugin.getShopManager().remove(chest);
        player.sendMessage(JobsPlugin.MM.deserialize("<green>Shop entfernt. Die Truhe funktioniert wieder normal.</green>"));
    }

    private void giveMoney(Player player, String[] args) {
        if (!requireAdmin(player)) return;

        if (args.length < 3) {
            player.sendMessage(JobsPlugin.MM.deserialize("<red>Benutzung: /jobs money [Spieler] [Betrag]</red>"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(JobsPlugin.MM.deserialize("<red>Dieser Spieler ist nicht online.</red>"));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2].replace(',', '.'));
        } catch (NumberFormatException e) {
            player.sendMessage(JobsPlugin.MM.deserialize("<red>Ungültiger Betrag.</red>"));
            return;
        }

        plugin.getDataManager().get(target.getUniqueId()).addMoney(amount);
        player.sendMessage(JobsPlugin.MM.deserialize("<green>" + JobsPlugin.money(amount) + " $ an "
                + target.getName() + " gegeben.</green>"));
    }

    private void sendUsage(Player player) {
        player.sendMessage(JobsPlugin.MM.deserialize(
                "<gray>Befehle: /jobs | /jobs setshop [erz|holz|essen] | /jobs removeshop | /jobs money [Spieler] [Betrag]</gray>"));
    }

    private boolean requireAdmin(Player player) {
        if (player.hasPermission("jobs.admin")) return true;
        player.sendMessage(JobsPlugin.MM.deserialize("<red>Dafür hast du keine Rechte.</red>"));
        return false;
    }

    /** Gibt die Truhe zurück, auf die der Spieler schaut, oder null mit Fehlermeldung. */
    private Block getTargetChest(Player player) {
        Block block = player.getTargetBlockExact(5);
        if (block == null || (block.getType() != Material.CHEST && block.getType() != Material.BARREL)) {
            player.sendMessage(JobsPlugin.MM.deserialize("<red>Schau auf eine Truhe (max. 5 Blöcke entfernt).</red>"));
            return null;
        }
        return block;
    }

    private List<String> categories() {
        List<String> result = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("shop.categories");
        if (section != null) {
            result.addAll(section.getKeys(false));
        }
        return result;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (!(sender instanceof Player player) || !player.hasPermission("jobs.admin") || args.length == 0) {
            return result;
        }

        if (args.length == 1) {
            result.addAll(List.of("setshop", "removeshop", "money"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setshop")) {
            result.addAll(categories());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("money")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                result.add(online.getName());
            }
        }
        return filter(result, args[args.length - 1]);
    }

    private static List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(lower))
                .toList();
    }
}
