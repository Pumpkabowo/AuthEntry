package me.pumpkabowo.authentry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AuthEntry extends JavaPlugin implements Listener, CommandExecutor {

    private final Set<UUID> frozenPlayers = new HashSet<>();
    private final Set<UUID> verifiedPlayers = new HashSet<>();
    private List<String> acceptedAnswers;
    private String question;
    private String prefix;
    private FileConfiguration config;
    private FileConfiguration verifiedConfig;
    private File verifiedFile;
    private final Map<UUID, Long> lastMoveMessageTime = new HashMap<>();
    private boolean pluginEnabled = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        prefix = colorize(config.getString("messages.prefix", "&7[&bAuthEntry&7] "));
        question = colorize(config.getString("auth.question", "&bWhat is 1+1?"));
        acceptedAnswers = config.getStringList("auth.answers");

        setupVerifiedFile();
        List<String> stored = verifiedConfig.getStringList("verified");
        for (String uuidStr : stored) {
            try {
                verifiedPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {}
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("auth").setExecutor(this);

        getLogger().info("AuthEntry enabled with " + verifiedPlayers.size() + " verified player(s).");
    }

    @Override
    public void onDisable() {
        saveVerifiedPlayers();
    }

    private void setupVerifiedFile() {
        verifiedFile = new File(getDataFolder(), "verified.yml");
        if (!verifiedFile.exists()) {
            try {
                verifiedFile.createNewFile();
                verifiedConfig = YamlConfiguration.loadConfiguration(verifiedFile);
                verifiedConfig.set("verified", new ArrayList<>());
                verifiedConfig.save(verifiedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            verifiedConfig = YamlConfiguration.loadConfiguration(verifiedFile);
        }
    }

    private void saveVerifiedPlayers() {
        List<String> toSave = verifiedPlayers.stream().map(UUID::toString).collect(Collectors.toList());
        verifiedConfig.set("verified", toSave);
        try {
            verifiedConfig.save(verifiedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!pluginEnabled) return;
        Bukkit.getScheduler().runTaskLater(this, () -> {
            Player player = event.getPlayer();
            if (!verifiedPlayers.contains(player.getUniqueId())) {
                frozenPlayers.add(player.getUniqueId());
                player.sendMessage(prefix + colorize(config.getString("messages.welcome", "&eWelcome! Please answer the security question:")));
                player.sendMessage(prefix + question);
            }
        }, 20L); // 1 second delay
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!pluginEnabled) return;
        Player player = event.getPlayer();
        if (frozenPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            String message = event.getMessage().trim();
            boolean correct = acceptedAnswers.stream().anyMatch(ans -> ans.equalsIgnoreCase(message));
            if (correct) {
                Bukkit.getScheduler().runTask(this, () -> {
                    frozenPlayers.remove(player.getUniqueId());
                    verifiedPlayers.add(player.getUniqueId());
                    player.sendMessage(prefix + colorize(config.getString("messages.correct", "&aCorrect! You are now verified.")));
                });
            } else {
                player.sendMessage(prefix + colorize(config.getString("messages.incorrect", "&cIncorrect. Try again:")));
                player.sendMessage(prefix + colorize(config.getString("messages.reask", "&7What is 1+1?")));
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!pluginEnabled) return;
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (frozenPlayers.contains(uuid)) {
            if (event.getFrom().distance(event.getTo()) > 0) {
                event.setTo(event.getFrom());

                long now = System.currentTimeMillis();
                long last = lastMoveMessageTime.getOrDefault(uuid, 0L);

                if (now - last > 2000) {
                    lastMoveMessageTime.put(uuid, now);
                    player.sendMessage(prefix + colorize(config.getString("messages.reminder", "&eYou must answer the question:")));
                    player.sendMessage(prefix + question);
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!pluginEnabled) return;
        if (event.getEntity() instanceof Player p && frozenPlayers.contains(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!pluginEnabled) return;
        if (frozenPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            if (!hasPermission(sender, "authentry.list")) return true;

            sender.sendMessage(prefix + ChatColor.YELLOW + "Verified players:");
            verifiedPlayers.forEach(uuid -> sender.sendMessage("- " + Bukkit.getOfflinePlayer(uuid).getName()));
            sender.sendMessage(ChatColor.GRAY + "Pending players:");
            frozenPlayers.forEach(uuid -> sender.sendMessage("- " + Bukkit.getOfflinePlayer(uuid).getName()));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("enable")) {
            if (!hasPermission(sender, "authentry.enable")) return true;
            pluginEnabled = true;
            sender.sendMessage(prefix + ChatColor.GREEN + "AuthEntry is now enabled.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("disable")) {
            if (!hasPermission(sender, "authentry.disable")) return true;
            pluginEnabled = false;
            sender.sendMessage(prefix + ChatColor.RED + "AuthEntry is now disabled.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            if (!hasPermission(sender, "authentry.status")) return true;
            sender.sendMessage(prefix + "AuthEntry is currently " + (pluginEnabled ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ".");
            return true;
        }

        if (args.length == 1) {
            if (!hasPermission(sender, "authentry.player")) return true;
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                UUID id = target.getUniqueId();
                if (frozenPlayers.contains(id)) {
                    frozenPlayers.remove(id);
                    verifiedPlayers.add(id);
                    sender.sendMessage(prefix + ChatColor.GREEN + target.getName() + " has been authorized.");
                    target.sendMessage(prefix + colorize(config.getString("messages.manual_auth", "&aYou have been authorized by an admin.")));
                } else if (verifiedPlayers.contains(id)) {
                    verifiedPlayers.remove(id);
                    frozenPlayers.add(id);
                    sender.sendMessage(prefix + ChatColor.RED + target.getName() + " has been unauthorized.");
                    target.sendMessage(prefix + colorize(config.getString("messages.unauthorized", "&eYou have been re-locked. What is 1+1?")));
                } else {
                    sender.sendMessage(prefix + ChatColor.RED + "Player is neither verified nor frozen.");
                }
            } else {
                sender.sendMessage(prefix + ChatColor.RED + "Player not found.");
            }
            return true;
        }

        sender.sendMessage(prefix + ChatColor.GOLD + "AuthEntry Command Help:");
        sender.sendMessage(ChatColor.YELLOW + "/auth <player>" + ChatColor.WHITE + " - Manually verify or freeze a player.");
        sender.sendMessage(ChatColor.YELLOW + "/auth enable/disable/status" + ChatColor.WHITE + " - Manage plugin state.");
        sender.sendMessage(ChatColor.YELLOW + "/auth list" + ChatColor.WHITE + " - Show verified and pending players.");
        return true;
    }

    private boolean hasPermission(CommandSender sender, String node) {
        if (!(sender instanceof Player)) return true;
        if (sender.hasPermission("authentry.all") || sender.hasPermission(node)) {
            return true;
        } else {
            sender.sendMessage(prefix + ChatColor.RED + "You do not have access to that command.");
            return false;
        }
    }

    private String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
