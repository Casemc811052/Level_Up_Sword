package com.besence.LevelUpSword;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

    private Map<UUID, Integer> blockBreakCounts = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("levelupaxe").setExecutor(this);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // レベルアップアックスでない場合は何もしないやつ
        if (!isLevelUpAxe(item)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        int blockBreakCount = blockBreakCounts.getOrDefault(playerId, 0) + 1;
        blockBreakCounts.put(playerId, blockBreakCount);

        if (blockBreakCount >= 300000) {
            
            ItemMeta meta = item.getItemMeta();
            int currentAttackDamage = meta.getAttackDamage();
            meta.setAttackDamage(currentAttackDamage + 1);
            item.setItemMeta(meta);

            // ブロック破壊数をリセット
            blockBreakCounts.put(playerId, 0);
        }

        // プレイヤーのデータを保存
        savePlayerData(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("levelupaxe")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                // レベルアップアックスを与える
                ItemStack levelUpAxe = new ItemStack(Material.DIAMOND_AXE);
                ItemMeta meta = levelUpAxe.getItemMeta();
                meta.setDisplayName("レベルアップアックス");
                // 他の設定（攻撃力など）もここで行う
                levelUpAxe.setItemMeta(meta);

                player.getInventory().addItem(levelUpAxe);
                return true;
            } else {
                sender.sendMessage("このコマンドはプレイヤーのみ実行できます。");
                return true;
            }
        }
        return false;
    }

    private boolean isLevelUpAxe(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_AXE || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        return meta.getDisplayName().equals("レベルアップアックス");
    }

    private FileConfiguration getPlayerDataConfig(Player player) {
        File dataFolder = getDataFolder();
        File playerDataFolder = new File(dataFolder, "playerdata");
        playerDataFolder.mkdirs();

        String playerName = player.getName();
        File playerDataFile = new File(playerDataFolder, playerName + ".yml");

        return YamlConfiguration.loadConfiguration(playerDataFile);
    }

    private void savePlayerData(Player player) {
        FileConfiguration config = getPlayerDataConfig(player);

        UUID playerId = player.getUniqueId();
        int blockBreakCount = blockBreakCounts.getOrDefault(playerId, 0);
        config.set("blockBreakCount", blockBreakCount);

        try {
            config.save(new File(getDataFolder(), "playerdata/" + player.getName() + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
