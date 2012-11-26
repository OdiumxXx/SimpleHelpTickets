package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class purgetickets implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public purgetickets(SimpleHelpTickets plugin)  {
    this.plugin = plugin;
  }

  DBConnection service = DBConnection.getInstance();
  ResultSet rs;
  java.sql.Statement stmt;
  Connection con;

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    if (args.length == 0) {
      sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GOLD+plugin.expireTickets()+ChatColor.WHITE+" Expired tickets purged");
      return true;
    } else if (args.length == 1 && args[0].equalsIgnoreCase("-c")) { 
      java.sql.Statement stmt;
      Connection con;
      try {
        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
          con = plugin.mysql.getConnection();
        } else {
        con = service.getConnection();
        }
        stmt = con.createStatement();
        stmt.executeUpdate("DELETE FROM SHT_Tickets WHERE status='"+"CLOSED"+"'");
//        sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GREEN+"All Closed Tickets Successfully Purged");
        sender.sendMessage(plugin.getMessage("AllClosedTicketsPurged"));
      } catch(Exception e) {
        sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
      }  

    } else if (args.length == 1 && args[0].equalsIgnoreCase("-a")) { 
      java.sql.Statement stmt;
      Connection con;
      try {
        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
          con = plugin.mysql.getConnection();
        } else {
        con = service.getConnection();
        }
        stmt = con.createStatement();
        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
          stmt.executeUpdate("TRUNCATE SHT_Tickets");  
        } else {
          stmt.executeUpdate("DELETE FROM SHT_Tickets");
        }
        
        
//        sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GREEN+"All Tickets Successfully Purged");
        sender.sendMessage(plugin.getMessage("AllTicketsPurged"));
      } catch(Exception e) {
        plugin.log.info("[SimpleHelpTickets] "+"Error: "+e);
      }  

    }
    return true;
  }
}

