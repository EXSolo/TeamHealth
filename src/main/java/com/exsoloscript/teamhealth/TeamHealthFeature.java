package com.exsoloscript.teamhealth;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import uk.co.eluinhost.UltraHardcore.UltraHardcore;
import uk.co.eluinhost.UltraHardcore.config.ConfigHandler;
import uk.co.eluinhost.UltraHardcore.exceptions.FeatureIDNotFoundException;
import uk.co.eluinhost.UltraHardcore.exceptions.FeatureStateNotChangedException;
import uk.co.eluinhost.UltraHardcore.features.FeatureManager;
import uk.co.eluinhost.UltraHardcore.features.UHCFeature;

import java.util.WeakHashMap;
import java.util.logging.Level;

public class TeamHealthFeature extends UHCFeature {

    private static int task_id = -1;
    private static WeakHashMap<Team, Double> players = new WeakHashMap<>();
    private static final int health_scaling = ConfigHandler.getConfig(0).getInt("features.playerListHealth.scaling");
    private static final boolean round_health = ConfigHandler.getConfig(0).getBoolean("features.playerListHealth.roundHealth");
    private static Scoreboard board = null;
    private static Objective obj_player_list = null;
    private static Objective obj_player_name = null;

    public TeamHealthFeature(boolean b) {
        super(b);
        setFeatureID("TeamHealth");
        setDescription("The health of all players in a team shown in player list");
    }

    public static void updatePlayerListHealth(Player player, double health) {
        String str = ChatColor.stripColor(player.getDisplayName());
        int players = 1;
        Team t = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
        if (t != null) {
            players = t.getPlayers().size();
        }

        if (ConfigHandler.getConfig(0).getBoolean("features.playerListHealth.colours")) {
            str = str.substring(0, Math.min(str.length(), 14));
            if (!player.hasPermission("UHC.playerListHealth")) {
                str = ChatColor.BLUE + str;
                health = 0;
            } else if (health <= (players * 6)) {
                str = ChatColor.RED + str;
            } else if (health <= (players * 12)) {
                str = ChatColor.YELLOW + str;
            } else {
                str = ChatColor.GREEN + str;
            }
        } else {
            str = str.substring(0, Math.min(str.length(), 16));
        }
        player.setPlayerListName(str);
        if (round_health) {
            health = Math.ceil(health);
        }

        // Fallback for older versions
        obj_player_list.getScore(Bukkit.getOfflinePlayer(str)).setScore((int) (health * health_scaling));
        obj_player_name.getScore(Bukkit.getOfflinePlayer(ChatColor.stripColor(player.getDisplayName()))).setScore((int) (health * health_scaling));
    }

    public static void updatePlayers(Player[] players) {
        for (Player p : players) {
            Team t = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(p);
            Double health = TeamHealthFeature.players.get(t);
            double newHealth = 0;

            if (t != null) {
                if (health == null) {
                    TeamHealthFeature.players.put(t, newHealth);
                }

                for (OfflinePlayer op : t.getPlayers()) {
                    if (op.isOnline()) {
                        newHealth += op.getPlayer().getHealth();
                    }
                }
            } else {
                newHealth = p.getHealth();
            }

            if (health == null || newHealth != health) {
                updatePlayerListHealth(p, newHealth);
            }
        }
    }

    public void enableFeature() {
        ConfigHandler.getConfig(0).set("features.teamHealth.enabled", true);
        ConfigHandler.saveConfig(0);

        try {
            FeatureManager.getFeature("PlayerList").setEnabled(false);
        } catch (FeatureIDNotFoundException e) {
            Bukkit.getLogger().log(Level.SEVERE, "PlayerList feature was not found. Did you install the latest version?");
        } catch (FeatureStateNotChangedException e) {
            Bukkit.getLogger().log(Level.SEVERE, "PlayerList feature was not toggled. Please contact a developer");
        }

        task_id = Bukkit.getScheduler().scheduleSyncRepeatingTask(UltraHardcore.getInstance(), new Runnable() {
            public void run() {
                TeamHealthFeature.updatePlayers(Bukkit.getOnlinePlayers());
            }
        }, 1L, ConfigHandler.getConfig(0).getInt("features.playerListHealth.delay"));

        initializeScoreboard();
    }

    public void disableFeature() {
        ConfigHandler.getConfig(0).set("features.teamHealth.enabled", false);
        ConfigHandler.saveConfig(0);

        if (task_id >= 0) {
            Bukkit.getScheduler().cancelTask(task_id);
            task_id = -1;
        }
        if (board != null) {
            board.clearSlot(DisplaySlot.PLAYER_LIST);
            board.clearSlot(DisplaySlot.BELOW_NAME);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setPlayerListName(p.getDisplayName());
            }
        }
    }

    private void initializeScoreboard() {
        try {
            board.registerNewObjective("UHCHealth", "dummy");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            board.registerNewObjective("UHCHealthName", "dummy");
        } catch (IllegalArgumentException ignored) {
        }

        obj_player_list = board.getObjective("UHCHealth");
        obj_player_name = board.getObjective("UHCHealthName");
        obj_player_name.setDisplayName(ChatColor.translateAlternateColorCodes('&', ConfigHandler.getConfig(0).getString("features.playerListHealth.belowNameUnit")).replaceAll("&h", "\u2665"));
        obj_player_list.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        if (ConfigHandler.getConfig(0).getBoolean("features.playerListHealth.belowName")) {
            obj_player_name.setDisplaySlot(DisplaySlot.BELOW_NAME);
        } else {
            Objective localObjective = board.getObjective(DisplaySlot.BELOW_NAME);
            if ((localObjective != null) && (localObjective.getName().equals("UHCHealthName"))) {
                board.clearSlot(DisplaySlot.BELOW_NAME);
            }
        }
    }

    static {
        board = Bukkit.getScoreboardManager().getMainScoreboard();
    }
}
