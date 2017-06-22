package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;

import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class closeticket implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public closeticket(SimpleHelpTickets plugin)  {
    this.plugin = plugin;
  }

  DBConnection service = DBConnection.getInstance();
  ResultSet rs;
  java.sql.Statement stmt;
  Connection con;

  Player target;
  String admin;
  Boolean TicketClose;

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

    if(args.length == 0) {        
      sender.sendMessage(ChatColor.WHITE + "/closeticket <#>");
      return true;
      // CLOSING TICKET
    } else if(args.length == 1) {
      // CHECK TICKETNUMBER IS A DIGIT
      for (char c : args[0].toCharArray()) {
        if (!Character.isDigit(c)) {
          sender.sendMessage(plugin.getMessage("InvalidTicketNumber").replace("&arg", args[0]));
          return true;
        }
      }

      int id = Integer.parseInt(args[0]);
      // OPEN CONNECTION
      try {
        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
          con = plugin.mysql.getConnection();
        } else {
          con = service.getConnection();
        }
        stmt = con.createStatement();
        // GET TICKETRFROM DB
        rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE id='" + id + "'");
        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
          rs.next(); //sets pointer to first record in result set
        }
        // CHECK TICKET STATUS IS NOT ALREADY CLOSED, IF SO END HERE.
        if (rs.getString("status").equalsIgnoreCase("CLOSED")) {
          sender.sendMessage(plugin.getMessage("TicketAlreadyClosed").replace("&arg", args[0]));
          rs.close();
          stmt.close();
          return true;
        }
        // CHECK THE OWNER OF THE TICKET, AND GET CUSTOM OUTPUT FROM CONFIG
        Player target = Bukkit.getPlayer(rs.getString("uuid"));

        // IF PLAYER IS CONSOLE, TICKETOWNER, OR ADMIN
        if (player == null || rs.getString("uuid").equals(player.getUniqueId().toString()) || player.hasPermission("sht.admin")) {
          // SET THE ADMIN VARIABLE TO REFLECT CONSOLE/ADMIN
          if (player == null) {
            admin = "CONSOLE";          
          } else if (rs.getString("uuid").contains(player.getUniqueId().toString()) || player.hasPermission("sht.admin")) {          
            admin = player.getName();          
          }

          //GET EXPIRATION DATE
          String date = rs.getString("date");
          String expiration = plugin.getExpiration(date);

          // UPDATE THE TICKET
          stmt.executeUpdate("UPDATE SHT_Tickets SET status='"+"CLOSED"+"', admin='"+admin+"', expiration='"+expiration+"' WHERE id='"+id+"'");
          sender.sendMessage(plugin.getMessage("TicketClosed").replace("&arg", ""+id));

          stmt.close();
          rs.close();
          // IF TICKETOWNER IS ONLINE, AND NOT THE USER WHO TRIGGERED THE EVENT, LET THEM KNOW OF THE CHANGE TO THEIR TICKET
          if (target != null && target != player) {
            target.sendMessage(plugin.getMessage("TicketClosedOWNER").replace("&arg", ""+id).replace("&admin", admin));
          }
          // NOTIFY ADMINS OF CHANGE TO TICKET
          Collection<? extends Player> players = Bukkit.getOnlinePlayers();
          for(Player op: players){
            if(op.hasPermission("sht.admin") && plugin.getConfig().getBoolean("NotifyAdminOnTicketClose") && op != sender) {
              op.sendMessage(plugin.getMessage("TicketClosedADMIN").replace("&arg", ""+args[0]).replace("&admin", admin));
            }
          }
          return true;
        } else {
          sender.sendMessage(plugin.getMessage("NotYourTicketToClose").replace("&arg", args[0]));
          return true;
        }
      } catch(Exception e) {
        if (e.toString().contains("ResultSet closed")) {
          sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
          return true;
        } else if (e.toString().contains("empty")) {
          sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
          return true;
        } else {
          sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));

          return true;
        }
      }
      // REPOENING A TICKET
    } else if(args.length == 2 && args[0].equalsIgnoreCase("-r")) {

      // CHECK TICKETNUMBER IS A DIGIT
      for (char c : args[1].toCharArray()) {
        if (!Character.isDigit(c)) {
          sender.sendMessage(plugin.getMessage("InvalidTicketNumber").replace("&arg", args[1]));
          return true;
        }
      }

      int id = Integer.parseInt(args[1]);
      // OPEN CONNECTION      
      try {
        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
          con = plugin.mysql.getConnection();
        } else {
          con = service.getConnection();
        }
        stmt = con.createStatement();
        // GET TICKET FROM DB
        rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE id='" + id + "'");
        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
          rs.next(); //sets pointer to first record in result set
        }
        // CHECK TICKET IS NOT ALREADY OPEN, IF SO END HERE.
        if (rs.getString("status").equalsIgnoreCase("OPEN")) {
          sender.sendMessage(plugin.getMessage("TicketNotClosed").replace("&arg", args[1]));
          rs.close();
          stmt.close();
          return true;
        }
        // CHECK THE OWNER OF THE TICKET, AND GET CUSTOM OUTPUT FROM CONFIG       
        Player target = Bukkit.getPlayer(rs.getString("uuid"));        
        // IF PLAYER IS CONSOLE OR ADMIN
        if (player == null || player.hasPermission("sht.admin")) {
          // SET THE ADMIN VARIABLE TO RELECT CONSOLE/ADMIN
          if (player == null) {
            admin = "CONSOLE";          
          } else if (rs.getString("uuid").contains(player.getUniqueId().toString()) || player.hasPermission("sht.admin")) {          
            admin = player.getName();          
          }
          // UPDATE THE TICKET
          stmt.executeUpdate("UPDATE SHT_Tickets SET status='"+"OPEN"+"', admin='"+admin+"', expiration=NULL WHERE id='"+id+"'");

          sender.sendMessage(plugin.getMessage("TicketReopened").replace("&arg", ""+id).replace("&admin", admin));
          stmt.close();
          rs.close();
          // IF TICKETOWNER IS ONLINE, TELL THEM OF CHANGES TO THEIR TICKET
          if (target != null && target != player) {             
            target.sendMessage(plugin.getMessage("TicketReopenedOWNER").replace("&arg", ""+id).replace("&admin", admin));
          }
          // INFORM ADMINS OF CHANGES TOT ICKET
          Collection<? extends Player> players = Bukkit.getOnlinePlayers();
          for(Player op: players){
            if(op.hasPermission("sht.admin") && plugin.getConfig().getBoolean("NotifyAdminOnTicketClose") && op != sender) {
              op.sendMessage(plugin.getMessage("TicketReopenedADMIN").replace("&arg", ""+id).replace("&admin", admin));
            }
          }
          return true;
        } else {
          sender.sendMessage(plugin.getMessage("NoPermission"));
          return true;
        }
      } catch(Exception e) {
        if (e.toString().contains("ResultSet closed")) {
          sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[1]));
          return true;
        } else {
          sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
          return true;
        }
      }
    }
    return true; 
  }
}
