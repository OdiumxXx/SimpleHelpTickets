package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.UUID;

import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class tickets implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public tickets(SimpleHelpTickets plugin)  {
    this.plugin = plugin;
  }

  DBConnection service = DBConnection.getInstance();
  ResultSet rs;
  java.sql.Statement stmt;
  Connection con;

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }


    if (player == null || player.hasPermission("sht.admin")) {



      try {
        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
          con = plugin.mysql.getConnection();
        } else {
          con = service.getConnection();
        }
        stmt = con.createStatement();

        if (args.length == 0) {    
          // DISPLAY OPEN TICKETS
          rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE status='"+"OPEN"+"' ORDER BY id ASC");
          int iterations = 0;
          sender.sendMessage(plugin.GOLD+"[ "+plugin.WHITE+ChatColor.BOLD+"Open Tickets"+ChatColor.RESET+plugin.GOLD+" ]");
          while(rs.next()){
            iterations++;
            String desc = rs.getString("description");
            if (desc.length() > 42) {
              desc = desc.substring(0, 42)+"...";              
            }            
            String ownerName = Bukkit.getOfflinePlayer(rs.getString("uuid")).getName();            
            
            if (!rs.getString("adminreply").equalsIgnoreCase("NONE") && rs.getString("userreply").equalsIgnoreCase("NONE")) { // if only admin has replied
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.WHITE+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GREEN+ownerName+": "+ChatColor.YELLOW+desc);
            } else if (!rs.getString("userreply").equalsIgnoreCase("NONE") && rs.getString("adminreply").equalsIgnoreCase("NONE")) { // if only user has replied
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.WHITE+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GREEN+ownerName+": "+ChatColor.YELLOW+desc);
            } else if (!rs.getString("adminreply").equalsIgnoreCase("NONE") && !rs.getString("userreply").equalsIgnoreCase("NONE")) { // if both have replied
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.WHITE+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GREEN+ownerName+": "+ChatColor.GOLD+desc);
            } else if (rs.getString("adminreply").equalsIgnoreCase("NONE") && rs.getString("userreply").equalsIgnoreCase("NONE")) { // if neither have replied
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.WHITE+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GREEN+ownerName+": "+ChatColor.WHITE+desc);
            }
          }
          if (iterations == 0) {            
            sender.sendMessage(plugin.getMessage("NoTickets"));           
            rs.close();
            stmt.close();
            return true;
          }

          rs.close();
          stmt.close();
          return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("-a")) {     
          // DISPLAY ALL TICKETS
          rs = stmt.executeQuery("SELECT * FROM SHT_Tickets ORDER BY id ASC");      
          int iterations = 0;
          sender.sendMessage(plugin.GOLD+"[ "+plugin.WHITE+ChatColor.BOLD+"All Tickets"+ChatColor.RESET+plugin.GOLD+" ]");
          while(rs.next()){
            iterations++;
            String desc = rs.getString("description");
            if (desc.length() > 42) {
              desc = desc.substring(0, 42)+"...";
            }
            String ownerName = Bukkit.getOfflinePlayer(rs.getString("uuid")).getName();
            if (!rs.getString("adminreply").equalsIgnoreCase("NONE") && rs.getString("userreply").equalsIgnoreCase("NONE")) { // IF ONLY ADMIN HAS REPLIED     
              if (rs.getString("status").equalsIgnoreCase("OPEN")) { // IF TICKET IS OPEN AND ADMIN HAS REPLIED
                sender.sendMessage(ChatColor.GOLD+"("+ChatColor.WHITE+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GREEN+ownerName+": "+ChatColor.YELLOW+desc);
              } else { // IF TICKET IS CLOSED AND ADMIN HAS REPLIED
                sender.sendMessage(ChatColor.GOLD+"("+ChatColor.GRAY+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GRAY+ownerName+": "+ChatColor.YELLOW+desc);
              }

            } else if (!rs.getString("userreply").equalsIgnoreCase("NONE") && rs.getString("adminreply").equalsIgnoreCase("NONE")) { // IF ONLY USER HAS REPLIED
              if (rs.getString("status").equalsIgnoreCase("OPEN")) { // IF TICKET IS OPEN AND ONLY USER HAS REPLIED
                sender.sendMessage(ChatColor.GOLD+"("+ChatColor.WHITE+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GREEN+ownerName+": "+ChatColor.YELLOW+desc);
              } else { // IF TICKET IS CLOSED AND ONLY USER HAS REPLIED
                sender.sendMessage(ChatColor.GOLD+"("+ChatColor.GRAY+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GRAY+ownerName+": "+ChatColor.YELLOW+desc);
              }

            } else if (!rs.getString("adminreply").equalsIgnoreCase("NONE") && !rs.getString("userreply").equalsIgnoreCase("NONE")) { // IF BOTH ADMIN AND USER HAVE REPLIED
              if (rs.getString("status").equalsIgnoreCase("OPEN")) { // IF TICKET IS OPEN AND BOTH HAVE REPLIED
                sender.sendMessage(ChatColor.GOLD+"("+ChatColor.WHITE+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GREEN+ownerName+": "+ChatColor.GOLD+desc);
              } else { // IF TICKET IS CLOSED AND BOTH HAVE REPLIED
                sender.sendMessage(ChatColor.GOLD+"("+ChatColor.GRAY+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GRAY+ownerName+": "+ChatColor.GOLD+desc);
              }

            } else if (rs.getString("adminreply").equalsIgnoreCase("NONE") && rs.getString("userreply").equalsIgnoreCase("NONE")) { // IF NEITHER HAVE REPLIED
              if (rs.getString("status").equalsIgnoreCase("OPEN")) { // IF TICKET IS OPEN AND NEITHER HAVE REPLIED 
                sender.sendMessage(ChatColor.GOLD+"("+ChatColor.WHITE+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GREEN+ownerName+": "+ChatColor.WHITE+desc);
              } else { // IF TICKET IS CLOSED AND NEITHER HAVE REPLIED
                sender.sendMessage(ChatColor.GOLD+"("+ChatColor.GRAY+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GRAY+ownerName+": "+ChatColor.GRAY+desc);
              }
            }
          }
          if (iterations == 0) {            
            sender.sendMessage(plugin.getMessage("NoTickets"));            
            rs.close();
            stmt.close();
            return true;
          }

          rs.close();
          stmt.close();
          return true;
          // DISPLAY CLOSED TICKETS
        } else if (args.length == 1 && args[0].equalsIgnoreCase("-c")) {     
          rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE status='"+"CLOSED"+"' ORDER BY id ASC");          
          int iterations = 0;
          sender.sendMessage(plugin.GOLD+"[ "+plugin.WHITE+ChatColor.BOLD+"Closed Tickets"+ChatColor.RESET+plugin.GOLD+" ]");
          while(rs.next()){
            iterations++;
            String desc = rs.getString("description");
            if (desc.length() > 42) {
              desc = desc.substring(0, 42)+"...";
            }
            String ownerName = Bukkit.getOfflinePlayer(rs.getString("uuid")).getName();
            if (!rs.getString("adminreply").equalsIgnoreCase("NONE") && rs.getString("userreply").equalsIgnoreCase("NONE")) { // IF ONLY ADMIN HAS REPLIED
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.GRAY+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GRAY+ownerName+": "+ChatColor.YELLOW+desc);
            } else if (rs.getString("adminreply").equalsIgnoreCase("NONE") && !rs.getString("userreply").equalsIgnoreCase("NONE")) { // IF ONLY USER HAS REPLIED
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.GRAY+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GRAY+ownerName+": "+ChatColor.YELLOW+desc);
            } else if (!rs.getString("adminreply").equalsIgnoreCase("NONE") && !rs.getString("userreply").equalsIgnoreCase("NONE")) {   // IF BOTH HAVE REPLIED
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.GRAY+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GRAY+ownerName+": "+ChatColor.GOLD+desc);
            } else if (rs.getString("adminreply").equalsIgnoreCase("NONE") && rs.getString("userreply").equalsIgnoreCase("NONE")) {   // IF NEITHER HAVE REPLIED
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.GRAY+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GRAY+ownerName+": "+ChatColor.GRAY+desc);
            }
          }
          if (iterations == 0) {            
            sender.sendMessage(plugin.getMessage("NoTickets"));            
            rs.close();
            stmt.close();
            return true;
          }

          rs.close();
          stmt.close();
          return true;
        } else if (args.length == 1 && !args[0].equalsIgnoreCase("-c") && !args[0].equalsIgnoreCase("-a") ) {
          sender.sendMessage("/tickets [-a/-c]");
          return true;
        }
      } catch(Exception e) {
        sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
        return true;
      }
    } else {
      // DISPLAY USER TICKETS
      try {
        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
          con = plugin.mysql.getConnection();
        } else {
          con = service.getConnection();
        }
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE uuid='"+player.getUniqueId().toString()+"' ORDER BY id ASC");   
        int iterations = 0;
        sender.sendMessage(plugin.GOLD+"[ "+plugin.WHITE+ChatColor.BOLD+"Your Tickets"+ChatColor.RESET+plugin.GOLD+" ]");
        while(rs.next()){
          iterations++;
          String desc = rs.getString("description");
          if (desc.length() > 42) {
            desc = desc.substring(0, 42)+"...";
          }
          String ownerName = Bukkit.getOfflinePlayer(rs.getString("uuid")).getName();
          if (!rs.getString("adminreply").equalsIgnoreCase("NONE") && rs.getString("userreply").equalsIgnoreCase("NONE")) { // IF THERE HAS BEEN AN ADMIN REPLY              
            if (rs.getString("status").equalsIgnoreCase("OPEN")) { // IF TICKET IS OPEN WITH ADMIN REPLY
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.WHITE+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GREEN+ownerName+": "+ChatColor.YELLOW+desc);
            } else { // IF TICKET IS CLOSED WITH ADMIN REPLY
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.GRAY+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GRAY+ownerName+": "+ChatColor.YELLOW+desc);
            }
          } else if (!rs.getString("userreply").equalsIgnoreCase("NONE") && rs.getString("adminreply").equalsIgnoreCase("NONE")) { // IF THERE HAS BEEN A USER REPLY              
            if (rs.getString("status").equalsIgnoreCase("OPEN")) { // IF TICKET IS OPEN WITH USER REPLY
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.WHITE+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GREEN+ownerName+": "+ChatColor.YELLOW+desc);
            } else { // IF TICKET IS CLOSED WITH USER REPLY
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.GRAY+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GRAY+ownerName+": "+ChatColor.YELLOW+desc);
            }
          } else if (!rs.getString("adminreply").equalsIgnoreCase("NONE") && !rs.getString("userreply").equalsIgnoreCase("NONE")) { //IF THERE HAS BEEN BOTH AN ADMIN AND USER REPLY
            if (rs.getString("status").equalsIgnoreCase("OPEN")) { // IF TICKET IS OPEN WITH BOTH ADMIN AND USER REPLY
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.WHITE+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GREEN+ownerName+": "+ChatColor.GOLD+desc);
            } else { // IF TICKET IS CLOSED WITH BOTH ADMIN AND USER REPLY
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.GRAY+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GRAY+ownerName+": "+ChatColor.GOLD+desc);
            }
          } else { // IF THERE HAS BEEN NO REPLIES
            if (rs.getString("status").equalsIgnoreCase("OPEN")) { // IF TICKET IS OPEN
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.WHITE+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GREEN+ownerName+": "+ChatColor.WHITE+desc);
            } else { // IF TICKET IS CLOSED
              sender.sendMessage(ChatColor.GOLD+"("+ChatColor.GRAY+rs.getInt("id")+ChatColor.GOLD+") "+ChatColor.DARK_GRAY+ownerName+": "+ChatColor.GRAY+desc);
            }
          }
        }
        if (iterations == 0) {            
          sender.sendMessage(plugin.getMessage("NoTickets"));          
          rs.close();
          stmt.close();
          return true;
        }
        rs.close();
        stmt.close();
        return true;
      } catch(Exception e) {
        sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
        return true;
      }

    }
    return true;  
  }
}
