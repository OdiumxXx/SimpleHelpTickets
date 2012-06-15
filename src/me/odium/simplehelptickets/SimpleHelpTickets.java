package me.odium.simplehelptickets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;


import me.odium.simplehelptickets.commands.checkticket;
import me.odium.simplehelptickets.commands.closeticket;
import me.odium.simplehelptickets.commands.delticket;
import me.odium.simplehelptickets.commands.replyticket;
import me.odium.simplehelptickets.commands.sht;
import me.odium.simplehelptickets.commands.taketicket;
import me.odium.simplehelptickets.commands.ticket;
import me.odium.simplehelptickets.commands.tickets;
import me.odium.simplehelptickets.listeners.PListener;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleHelpTickets extends JavaPlugin {
  Logger log = Logger.getLogger("Minecraft");

  public ChatColor GREEN = ChatColor.GREEN;
  public  ChatColor RED = ChatColor.RED;
  public ChatColor GOLD = ChatColor.GOLD;
  public ChatColor GRAY = ChatColor.GRAY;
  public ChatColor WHITE = ChatColor.WHITE; 
  public ChatColor AQUA = ChatColor.AQUA;
  
  // Custom Config  
  private FileConfiguration StorageConfig = null;
  private File StorageConfigFile = null;

  public void reloadStorageConfig() {
    if (StorageConfigFile == null) {
      StorageConfigFile = new File(getDataFolder(), "StorageConfig.yml");
    }
    StorageConfig = YamlConfiguration.loadConfiguration(StorageConfigFile);

    // Look for defaults in the jar
    InputStream defConfigStream = getResource("StorageConfig.yml");
    if (defConfigStream != null) {
      YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
      StorageConfig.setDefaults(defConfig);
    }
  }
  public FileConfiguration getStorageConfig() {
    if (StorageConfig == null) {
      reloadStorageConfig();
    }
    return StorageConfig;
  }
  public void saveStorageConfig() {
    if (StorageConfig == null || StorageConfigFile == null) {
      return;
    }
    try {
      StorageConfig.save(StorageConfigFile);
    } catch (IOException ex) {
      Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + StorageConfigFile, ex);
    }
  }
  // End Custom Config

  public String getCurrentDTG (String string) {
    Calendar currentDate = Calendar.getInstance();
    SimpleDateFormat dtgFormat = new SimpleDateFormat ("dd/MMM/yy HH:mm");    
    return dtgFormat.format (currentDate.getTime());
  }  

  public void onEnable(){    
    log.info("[" + getDescription().getName() + "] " + getDescription().getVersion() + " enabled.");
    FileConfiguration cfg = getConfig();
    FileConfigurationOptions cfgOptions = cfg.options();
    cfgOptions.copyDefaults(true);
    cfgOptions.copyHeader(true);
    saveConfig();
    // Load Custom Config
    FileConfiguration ccfg = getStorageConfig();
    FileConfigurationOptions ccfgOptions = ccfg.options();
    ccfgOptions.copyDefaults(true);
    ccfgOptions.copyHeader(true);
    saveStorageConfig();
    // declare new listener
    new PListener(this);
    // declare executors
    this.getCommand("sht").setExecutor(new sht(this));
    this.getCommand("ticket").setExecutor(new ticket(this));
    this.getCommand("tickets").setExecutor(new tickets(this));
    this.getCommand("checkticket").setExecutor(new checkticket(this));
    this.getCommand("replyticket").setExecutor(new replyticket(this));
    this.getCommand("taketicket").setExecutor(new taketicket(this));
    this.getCommand("delticket").setExecutor(new delticket(this));
    this.getCommand("closeticket").setExecutor(new closeticket(this));
  }

  public void onDisable(){ 
    log.info("[" + getDescription().getName() + "] " + getDescription().getVersion() + " disabled."); 
  }

  public String myGetPlayerName(String name) { 
    Player caddPlayer = getServer().getPlayerExact(name);
    String pName;
    if(caddPlayer == null) {
      caddPlayer = getServer().getPlayer(name);
      if(caddPlayer == null) {
        pName = name;
      } else {
        pName = caddPlayer.getName();
      }
    } else {
      pName = caddPlayer.getName();
    }
    return pName;
  }
  
  public void displayHelp(Player player) {
    player.sendMessage(ChatColor.GOLD + "[ " +"SimpleHelpTickets" + " v" + getDescription().getVersion() + ChatColor.GOLD + " ]");
    player.sendMessage(ChatColor.DARK_GREEN + " /sht" + ChatColor.WHITE + " - Display the commands menu.");
    player.sendMessage(ChatColor.DARK_GREEN + " /helpme" + ChatColor.WHITE + " - Explain how to open a ticket.");
    player.sendMessage(ChatColor.DARK_GREEN + " /ticket <Description>" + ChatColor.WHITE + " - Open a Help ticket. [Stand @ Location]");
    player.sendMessage(ChatColor.DARK_GREEN + " /tickets" + ChatColor.WHITE + " - View Your Tickets.");
    player.sendMessage(ChatColor.DARK_GREEN + " /checkticket <#>" + ChatColor.WHITE + " - Check one of your ticket's info.");
    player.sendMessage(ChatColor.DARK_GREEN + " /closeticket <#>" + ChatColor.WHITE + " - Close one of your tickets.");
    if(player == null || player.hasPermission("sht.admin")) {
      player.sendMessage(ChatColor.GOLD + "[ " + "Admin Commands" + ChatColor.GOLD + " ]");
      player.sendMessage(ChatColor.AQUA + " /tickets" + ChatColor.WHITE + " - List all tickets");
      player.sendMessage(ChatColor.AQUA + " /checkticket <#>" + ChatColor.WHITE + " - Check a ticket's info.");
      player.sendMessage(ChatColor.AQUA + " /taketicket <#>" + ChatColor.WHITE + " - Assign yourself to a ticket.");
      player.sendMessage(ChatColor.AQUA + " /replyticket <#> <Reply>" + ChatColor.WHITE + " - Reply to a Ticket.");
      player.sendMessage(ChatColor.AQUA + " /closeticket <#>" + ChatColor.WHITE + " - Close a ticket.");
      player.sendMessage(ChatColor.AQUA + " /sht reload" + ChatColor.WHITE + " - Reload the config.");
    }
  }

}
