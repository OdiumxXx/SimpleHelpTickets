package me.odium.simplehelptickets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleHelpTickets extends JavaPlugin {
  Logger log = Logger.getLogger("Minecraft");

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

  public static String getCurrentDTG (String string) {
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
  }

  public void onDisable(){ 
    log.info("[" + getDescription().getName() + "] " + getDescription().getVersion() + " disabled."); 
  }

  public class PListener implements Listener {

    public PListener(SimpleHelpTickets instance) {
      Plugin plugin = instance;
      Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {      
      boolean ShowTicketsOnJoin = getConfig().getBoolean("ShowTicketsOnJoin");
      if (ShowTicketsOnJoin == true) {
        Player player = event.getPlayer();
        if (player.hasPermission("sht.admin")) {
          int ticklength = getStorageConfig().getStringList("tickets").size();
          if (ticklength == 0) {
            // DO NOTHING
          } else if(ticklength > 0) {
            player.sendMessage(ChatColor.GOLD + "* " + ChatColor.GRAY + "There are currently " + ChatColor.GOLD + ticklength + ChatColor.GRAY + " open Help Tickets");
          }
        } 
      } else {
        // Do Some Nothing
      }
    }
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

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

    if(cmd.getName().equalsIgnoreCase("helptickets")){ 
      sender.sendMessage(ChatColor.GOLD + "-- SimpleHelpTickets" + " v" + getDescription().getVersion() + " --");
      sender.sendMessage(ChatColor.BLUE + "/ticket <Description>" + ChatColor.WHITE + " - Open a Help ticket");
      if(player.hasPermission("sht.admin")) {
        sender.sendMessage(ChatColor.BLUE + "/tickets" + ChatColor.WHITE + " - Check Tickets");        
        sender.sendMessage(ChatColor.BLUE + "/delticket <#>" + ChatColor.WHITE + " - Silently Delete a Ticket");
        sender.sendMessage(ChatColor.BLUE + "/closeticket <#>" + ChatColor.WHITE + " - Close a dealt with ticket");
      }
    }

    if(cmd.getName().equalsIgnoreCase("sht")){ 
      if(args.length == 0) {      
        sender.sendMessage(ChatColor.GOLD + "-- SimpleHelpTickets" + " v" + getDescription().getVersion() + " --");
        sender.sendMessage(ChatColor.BLUE + "/ticket <Description>" + ChatColor.WHITE + " - Open a Help ticket");
        if(player.hasPermission("sht.admin")) {
          sender.sendMessage(ChatColor.BLUE + "/tickets" + ChatColor.WHITE + " - Check Tickets");        
          sender.sendMessage(ChatColor.BLUE + "/delticket <#>" + ChatColor.WHITE + " - Silently Delete a Ticket");
          sender.sendMessage(ChatColor.BLUE + "/closeticket <#>" + ChatColor.WHITE + " - Close a dealt with ticket");
        }
        return true;
      } else if (args.length == 1 && args[0].equalsIgnoreCase("version")) {         
        sender.sendMessage(ChatColor.GOLD + "[" + getDescription().getName() + "] " + " Version: " + ChatColor.RED + getDescription().getVersion());
        return true;     
      } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
        if(player.hasPermission("sht.reload")) {
          reloadConfig();
          sender.sendMessage(ChatColor.GREEN + "Config Reloaded!");
          return true;
        } else {
          sender.sendMessage(ChatColor.RED + "You do not have permission");
        }
      }
    }


    // BEGIN /WARN COMMAND
    if(cmd.getName().equalsIgnoreCase("ticket")){     
      if(args.length == 0) {
        sender.sendMessage(ChatColor.GOLD + "-- SimpleHelpTickets" + " v" + getDescription().getVersion() + " --");
        sender.sendMessage(ChatColor.BLUE + "/ticket <Description>" + ChatColor.WHITE + " - Open a Help ticket");
//        sender.sendMessage(ChatColor.GRAY + "If your help ticket requires an admin to see something, stand there when you set the ticket");
        if(player.hasPermission("sht.admin")) {
          sender.sendMessage(ChatColor.BLUE + "/tickets" + ChatColor.WHITE + " - Check Tickets");        
          sender.sendMessage(ChatColor.BLUE + "/delticket <#>" + ChatColor.WHITE + " - Silently Delete a Ticket");
          sender.sendMessage(ChatColor.BLUE + "/closeticket <#>" + ChatColor.WHITE + " - Close a dealt with ticket");
        }
      } else {
        StringBuilder sb = new StringBuilder();
        for (String arg : args)
          sb.append(arg + " ");
            //            String name = myGetPlayerName(args[0]);
            String[] temp = sb.toString().split(" ");
            String[] temp2 = Arrays.copyOfRange(temp, 0, temp.length);
            sb.delete(0, sb.length());
            for (String details : temp2)
            {
              sb.append(details);
              sb.append(" ");
            }
            String details = sb.toString();
            //            String loc = player.getLocation().toString();
            World PlayerWorld = player.getWorld();
            String PlayerWorldName = PlayerWorld.getName();
            double locX = player.getLocation().getX();
            double locY = player.getLocation().getY();
            double locZ = player.getLocation().getZ();

            StringBuilder sb1 = new StringBuilder();
            sb1.append(PlayerWorldName+",");
            sb1.append(locX+",");
            sb1.append(locY+",");
            sb1.append(locZ);
            String sb2 = sb1.toString();

            List<String> ticket = getStorageConfig().getStringList("tickets");
            List<String> ticketD = getStorageConfig().getStringList("dates");
            List<String> ticketPB = getStorageConfig().getStringList("placedby");
            List<String> ticketLOC = getStorageConfig().getStringList("location");
            List<String> admin = getStorageConfig().getStringList("admin");
            ticket.add(details); 
            ticketD.add(getCurrentDTG("date"));
            ticketPB.add(player.getDisplayName());
            ticketLOC.add(sb2);
            admin.add("noone"); 
            getStorageConfig().set("tickets", ticket);  // insert the ticket
            getStorageConfig().set("dates", ticketD); // insert the date
            getStorageConfig().set("placedby", ticketPB); // insert who placed the ticket
            getStorageConfig().set("location", ticketLOC); // insert who placed the ticket
            getStorageConfig().set("admin", admin); // insert who placed the ticket
            saveStorageConfig();
            sender.sendMessage(ChatColor.GREEN + "Your ticket has been logged, and will be addressed by an admin shortly.");
            Player[] players = Bukkit.getOnlinePlayers();
            for(Player op: players){
              if(op.hasPermission("sht.admin")) {
                op.sendMessage(ChatColor.GOLD + "* " + sender.getName() + ChatColor.WHITE + " has opened a " + ChatColor.GOLD + "Help Ticket");
              }
            }
      }
    }

    if(cmd.getName().equalsIgnoreCase("tickets")){
      java.util.List<String> userstickets = getStorageConfig().getStringList("tickets");            
      java.util.List<String> usersdates = getStorageConfig().getStringList("dates");
      java.util.List<String> userstickPB = getStorageConfig().getStringList("placedby");
      java.util.List<String> admin = getStorageConfig().getStringList("admin");
      sender.sendMessage(ChatColor.GOLD + "--- Current Help Tickets ---");
      int usersticklength = getStorageConfig().getStringList("tickets").size();
      if (usersticklength == 0) {
        sender.sendMessage(ChatColor.WHITE + "There are currently no help tickets to display.");  
      } else {
        for(int i = 0; i < userstickets.size(); ++i) {
          String date = usersdates.get(i);
          String ticket = userstickets.get(i);
          String tickPB = userstickPB.get(i);
          String tickadmin = admin.get(i);
          if (tickadmin.equals("noone")) {       
            sender.sendMessage(" " + ChatColor.GOLD + "(" + ChatColor.WHITE + i + ChatColor.GOLD + ") " + ChatColor.GRAY + date + " " + ChatColor.BLUE + tickPB + ChatColor.WHITE + " - " + ticket);
          } else {
            sender.sendMessage(" " + ChatColor.GOLD + "(" + ChatColor.WHITE + i + ChatColor.GOLD + ") "  + ChatColor.GREEN + " [" + tickadmin + "]" + " " + ChatColor.BLUE + tickPB + ChatColor.WHITE + " - " + ticket);
          }
        }
        return true;
      }
    }
    
    if(cmd.getName().equalsIgnoreCase("taketicket")){
      int ticketno = Integer.parseInt( args[0] );
      java.util.List<String> userstickets = getStorageConfig().getStringList("tickets");            
      java.util.List<String> usersdates = getStorageConfig().getStringList("dates");
      java.util.List<String> userstickPB = getStorageConfig().getStringList("placedby");
      java.util.List<String> userstickLOC = getStorageConfig().getStringList("location");
      java.util.List<String> admin = getStorageConfig().getStringList("admin");
      String date = usersdates.get(ticketno);
      String ticket = userstickets.get(ticketno);
      String tickPB = userstickPB.get(ticketno);
      String tickLOC = userstickLOC.get(ticketno);

      String[] vals = tickLOC.split(",");
      World world = Bukkit.getWorld(vals[0]);
      double x = Double.parseDouble(vals[1]);        
      double y = Double.parseDouble(vals[2]);
      double z = Double.parseDouble(vals[3]);
      Location loc = new Location(world, x, y, z);

      admin.set(ticketno, player.getDisplayName());
      getStorageConfig().set("admin", admin); // insert who placed the ticket
      player.teleport(loc);
      sender.sendMessage(ChatColor.GOLD + "--- " + ChatColor.WHITE + "Ticket " + ticketno + ChatColor.GOLD + " ---");
      sender.sendMessage(" " + ChatColor.BLUE + "Placed By: " + ChatColor.WHITE + tickPB);
      sender.sendMessage(" " + ChatColor.BLUE + "Date: " + ChatColor.WHITE + date);
      sender.sendMessage(" " + ChatColor.BLUE + "Ticket: " + ChatColor.GREEN + ticket);
      //      sender.sendMessage(" " + ChatColor.BLUE + "Location: " + ChatColor.GREEN + tickLOC);
      String tickuser = myGetPlayerName(tickPB);
      if(this.getServer().getPlayer(tickuser) == null) {
        return true;  
      } else {
        String admin1 = player.getDisplayName();
        Player target = this.getServer().getPlayer(tickuser);
        target.sendMessage(ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin1 + ChatColor.GRAY + " is reviewing your help ticket");
      }
    }


    if(cmd.getName().equalsIgnoreCase("delticket")){
      if(args.length == 0) {        
        sender.sendMessage(ChatColor.WHITE + "/delticket <#>");
      } else if(args.length == 1) {
        int ticketno = Integer.parseInt( args[0] );

        java.util.List<String> userstickets = getStorageConfig().getStringList("tickets");            
        java.util.List<String> usersdates = getStorageConfig().getStringList("dates");
        java.util.List<String> userstickPB = getStorageConfig().getStringList("placedby");
        java.util.List<String> userstickLOC = getStorageConfig().getStringList("location");
        java.util.List<String> admin = getStorageConfig().getStringList("admin");
        userstickets.remove(ticketno);
        usersdates.remove(ticketno);
        userstickPB.remove(ticketno);
        userstickLOC.remove(ticketno);
        admin.remove(ticketno);
        getStorageConfig().set("tickets", userstickets);  // insert the ticket
        getStorageConfig().set("dates", usersdates); // insert the date
        getStorageConfig().set("placedby", userstickPB); // insert who placed the ticket
        getStorageConfig().set("location", userstickLOC); // insert who placed the ticket
        getStorageConfig().set("admin", admin); // insert who placed the ticket
        saveStorageConfig();          
        sender.sendMessage(ChatColor.GREEN + " Ticket " + ticketno + " deleted");
      }
    }      

    if(cmd.getName().equalsIgnoreCase("closeticket")){
      if(args.length == 0) {        
        sender.sendMessage(ChatColor.WHITE + "/closeticket <#>");
      } else if(args.length == 1) {
        int ticketno = Integer.parseInt( args[0] );

        java.util.List<String> userstickets = getStorageConfig().getStringList("tickets");            
        java.util.List<String> usersdates = getStorageConfig().getStringList("dates");
        java.util.List<String> userstickPB = getStorageConfig().getStringList("placedby");
        java.util.List<String> userstickLOC = getStorageConfig().getStringList("location");
        java.util.List<String> admin = getStorageConfig().getStringList("admin");
        String target = userstickPB.get(ticketno);
        userstickets.remove(ticketno);
        usersdates.remove(ticketno);
        userstickPB.remove(ticketno);
        userstickLOC.remove(ticketno);
        admin.remove(ticketno);
        getStorageConfig().set("tickets", userstickets);  // insert the ticket
        getStorageConfig().set("dates", usersdates); // insert the date
        getStorageConfig().set("placedby", userstickPB); // insert who placed the ticket
        getStorageConfig().set("location", userstickLOC); // insert who placed the ticket
        getStorageConfig().set("admin", admin); // insert who placed the ticket
        saveStorageConfig();          
        sender.sendMessage(ChatColor.GREEN + " Ticket " + ticketno + " deleted");
        String tickuser = myGetPlayerName(target);
        if(this.getServer().getPlayer(tickuser) == null) {
          return true;  
        } else {
          String admin1 = player.getDisplayName();
          Player target1 = this.getServer().getPlayer(tickuser);
          target1.sendMessage(ChatColor.GRAY + "Administrator " + ChatColor.GOLD + admin1 + ChatColor.GRAY + " has closed your help ticket");
        }
      }    
    }


    if(cmd.getName().equalsIgnoreCase("cwarns")){
      if(args.length != 1) {        
        sender.sendMessage(ChatColor.WHITE + "/cwarn <playername>");
      } else if(args.length == 1) {
        String name = myGetPlayerName(args[0]);
        if(!getStorageConfig().contains(name)) {  
          sender.sendMessage(ChatColor.BLUE + name + ChatColor.GRAY + " has no warnings");
          return true;
        } else {
          getStorageConfig().set(name, null);
          saveStorageConfig();
          sender.sendMessage(ChatColor.GRAY + "Warnings for " + ChatColor.BLUE + name + ChatColor.GRAY + " cleared");
          if(this.getServer().getPlayer(args[0]) == null) {
            return true;  
          } else {
            Player target = this.getServer().getPlayer(args[0]);
            String admin = player.getDisplayName();
            target.sendMessage(ChatColor.BLUE + admin + ChatColor.GRAY +  " has removed one of your warnings");
            return true;
          }
        }
      }
    }
    return true;
    // END
  }
}
