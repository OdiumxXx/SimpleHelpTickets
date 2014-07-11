package me.odium.simplehelptickets.commands;

import java.io.File;

import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.entity.Player;

public class sht implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public sht(SimpleHelpTickets plugin)  {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

    if(args.length == 0) {      
      plugin.displayHelp(sender);
      return true;
    } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
      if(player == null || player.hasPermission("sht.reload") ) {
        // Reload Config
        File file=new File(plugin.getDataFolder()+File.separator+"config.yml");
        if(!file.exists()) {
          sender.sendMessage(plugin.getMessage("NewConfig"));         
          FileConfiguration cfg = plugin.getConfig();
          FileConfigurationOptions cfgOptions = cfg.options();
          cfgOptions.copyDefaults(true);
          cfgOptions.copyHeader(true);
          plugin.saveConfig();
        }        
        plugin.reloadConfig();


        // Reload Output Config
        File file2=new File(plugin.getDataFolder()+File.separator+"output.yml");
        if(!file2.exists()) {
          sender.sendMessage(plugin.getMessage("NewOutput"));
          FileConfiguration cfg = plugin.getOutputConfig();
          FileConfigurationOptions cfgOptions = cfg.options();
          cfgOptions.copyDefaults(true);
          cfgOptions.copyHeader(true);
          plugin.saveOutputConfig();

        }        
        plugin.reloadOutputConfig();

        sender.sendMessage(plugin.getMessage("ConfigReloaded"));
        return true;
      } else {
        sender.sendMessage(plugin.getMessage("NoPermission"));
        return true;
      }
    } else if (args.length == 1 && args[0].equalsIgnoreCase("check")) {
      String uuid = player.getUniqueId().toString();
      player.sendMessage(ChatColor.BLUE+"Your UUID: "+ChatColor.RESET+uuid);
      String uuidName = Bukkit.getOfflinePlayer(uuid).getName();
      player.sendMessage(ChatColor.BLUE+"Your Name "+ChatColor.RESET+uuidName);
    }

    return true;    
  }

}