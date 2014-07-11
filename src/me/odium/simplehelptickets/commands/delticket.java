package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;

import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class delticket implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public delticket(SimpleHelpTickets plugin)  {
    this.plugin = plugin;
  }

  DBConnection service = DBConnection.getInstance();
  ResultSet rs;
  java.sql.Statement stmt;
  java.sql.Statement stmt2;
  Connection con;

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

    if(args.length == 0) {        
      sender.sendMessage(ChatColor.WHITE + "/delticket <#>");
      return true;
    } else if(args.length == 1) {

      for (char c : args[0].toCharArray()) {
        if (!Character.isDigit(c)) {
          sender.sendMessage(plugin.getMessage("InvalidTicketNumber").replace("&arg", args[0]));
          return true;
        }
      }

      //CONSOLE COMMANDS
      if (player == null) {
        try {
          if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
            con = plugin.mysql.getConnection();
          } else {
            con = service.getConnection();
          }
          stmt = con.createStatement();
          //CHECK IF TICKET EXISTS
          rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM SHT_Tickets WHERE id='"+args[0]+"'");
          if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
            rs.next(); //sets pointer to first record in result set
          }
          if (rs.getInt("ticketTotal") == 0) {
            sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
            rs.close();
            stmt.close();
            return true;
          }
          stmt.executeUpdate("DELETE FROM SHT_Tickets WHERE id='"+args[0]+"'");
          sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.WHITE+"Ticket "+ChatColor.GOLD+args[0]+ChatColor.WHITE+" Deleted");

          stmt.close();
          return true;

        } catch(Exception e) {
          if (e.toString().contains("ResultSet closed")) {
            sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
            return true;
          } else {
          }
          sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
          return true;
        }
        // PLAYER COMMANDS
      } else {
        try {
          if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
            con = plugin.mysql.getConnection();
          } else {
            con = service.getConnection();
          }
          stmt = con.createStatement();
          //CHECK IF TICKET EXISTS
          rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM SHT_Tickets WHERE id='"+args[0]+"'");
          if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
            rs.next(); //sets pointer to first record in result set
          }
          if (rs.getInt("ticketTotal") == 0) {
            sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
            rs.close();
            stmt.close();
            return true;
          }
          rs.close();
          rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE id='" + args[0] + "'");
          if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
            rs.next(); //sets pointer to first record in result set
          }
          String playerUUID = player.getUniqueId().toString();
          if (!rs.getString("uuid").equals(playerUUID) && !player.hasPermission("sht.admin")) {
            sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.RED+"Ticket "+rs.getString("id")+" is not your ticket to delete.");
            return true;
          } else {
            stmt.executeUpdate("DELETE FROM SHT_Tickets WHERE id='"+args[0]+"'");
            sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.WHITE+"Ticket "+ChatColor.GOLD+args[0]+ChatColor.WHITE+" Deleted");
            rs.close();
            stmt.close();            
            return true;
          }
        } catch(Exception e) {
          sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
          return true;
        }     
      }
    }
    return true;
  }
}
