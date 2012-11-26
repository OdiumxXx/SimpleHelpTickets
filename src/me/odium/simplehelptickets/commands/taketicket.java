package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class taketicket implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public taketicket(SimpleHelpTickets plugin)  {
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

    if (player == null) {
      sender.sendMessage(plugin.RED+"This command can only be run by a player, use /checkticket instead.");
      return true;
    }
    if(args.length == 0) {        
    	sender.sendMessage(plugin.replaceColorMacros(plugin.getOutputConfig().getString("UserCommandsDescription-taketicket") + plugin.getOutputConfig().getString("UserCommandsMenu-taketicket")));
		return true;
    }
    
    for (char c : args[0].toCharArray()) {
      if (!Character.isDigit(c)) {
        sender.sendMessage(plugin.getMessage("InvalidTicketNumber").replace("&arg", args[0]));
        return true;
      }
    }

    int ticketNumber = Integer.parseInt( args[0] );

    try {
      if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
        con = plugin.mysql.getConnection();
      } else {
        con = service.getConnection();
      }
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE id='"+ticketNumber+"'");
      if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
        rs.next(); //sets pointer to first record in result set
      }
      String worldName = null;
      String date;

      // compile location     
      World world = Bukkit.getWorld(rs.getString("world"));
      double x = rs.getDouble("x");        
      double y = rs.getDouble("y");
      double z = rs.getDouble("z");
      float p = (float) rs.getDouble("p");
      float f = (float) rs.getDouble("f");
      final Location locc = new Location(world, x, y, z, f, p);
      // Display Ticket
      rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE id='"+ticketNumber+"'");
      if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
        rs.next(); //sets pointer to first record in result set
      }

      String id = rs.getString("id");
      String owner = rs.getString("owner");
      if (plugin.getConfig().getBoolean("MultiWorld") == true) {
        worldName = rs.getString("world");
      }

      if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
        date = new SimpleDateFormat("dd/MMM/yy HH:mm").format(rs.getTimestamp("date"));  
      } else {
        date = rs.getString("date");
      }      

      String Assignedadmin = rs.getString("admin");
      String adminreply = rs.getString("adminreply");
      String userreply = rs.getString("userreply");
      String description = rs.getString("description");
      String status = rs.getString("status");

      if (status.equalsIgnoreCase("CLOSED")) {        
        sender.sendMessage(plugin.getMessage("CannotTakeClosedTicket").replace("&arg", id));
        stmt.close();
        rs.close();
        return true;
      }

      sender.sendMessage(ChatColor.GOLD+"[ "+ChatColor.WHITE+ChatColor.BOLD+"Ticket "+id+ChatColor.RESET+ChatColor.GOLD+" ]");
      sender.sendMessage(ChatColor.BLUE+" Owner: "+ChatColor.WHITE+owner);
      sender.sendMessage(ChatColor.BLUE+" Date: "+ChatColor.WHITE+date);
      if (plugin.getConfig().getBoolean("MultiWorld") == true) {
        sender.sendMessage(ChatColor.BLUE+" World: "+ChatColor.WHITE+worldName);
      }
      if (status.contains("OPEN")) {
        sender.sendMessage(ChatColor.BLUE+" Status: "+ChatColor.GREEN+status);
      } else {
        sender.sendMessage(ChatColor.BLUE+" Status: "+ChatColor.RED+status);
      }
      sender.sendMessage(ChatColor.BLUE+" Assigned: "+ChatColor.WHITE+Assignedadmin);      
      sender.sendMessage(ChatColor.BLUE+" Ticket: "+ChatColor.GOLD+description);
      if (adminreply.equalsIgnoreCase("NONE")) {
        sender.sendMessage(ChatColor.BLUE+" Admin Reply: "+ChatColor.WHITE+"(none)");
      } else {
        sender.sendMessage(ChatColor.BLUE+" Admin Reply: "+ChatColor.YELLOW+adminreply);
      }
      if (userreply.equalsIgnoreCase("NONE")) {
        sender.sendMessage(ChatColor.BLUE+" User Reply: "+ChatColor.WHITE+"(none)");
      } else {
        sender.sendMessage(ChatColor.BLUE+" User Reply: "+ChatColor.YELLOW+userreply);
      }

      // TELEPORT ADMIN
      if (!owner.equalsIgnoreCase("CONSOLE")) {
        player.teleport(locc);
      }
      // NOTIFY ADMIN AND USERS
      String admin = player.getDisplayName();
      Player target = plugin.getServer().getPlayer(owner);
      //      String TicketReview = plugin.getConfig().getString("MessageOutput.TicketReviewMsg");
      // ASSIGN ADMIN
      stmt.executeUpdate("UPDATE SHT_Tickets SET admin='"+admin+"' WHERE id='"+id+"'");
      // NOTIFY -OTHER- ADMINS 
      Player[] players = Bukkit.getOnlinePlayers();
      for(Player op: players){
        if(op.hasPermission("sht.admin") && op != player) {
          //          op.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GOLD + admin + ChatColor.WHITE + " is reviewing ticket #: "+ChatColor.GOLD+id);
          op.sendMessage(plugin.getMessage("TakeTicketADMIN").replace("&arg", id).replace("&admin", admin));
        }
      }
      // NOTIFY USER
      if (target != null && target != player) {
        //        target.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.replaceColorMacros(TicketReview).replace("%id%", id).replace("%admin%", admin));
        target.sendMessage(plugin.getMessage("TakeTicketOWNER").replace("&arg", id).replace("&admin", admin));   

        stmt.close();
        rs.close();
        return true;
      }

      stmt.close();
      rs.close();
      return true;
    } catch(Exception e) {
      if (e.toString().contains("ResultSet closed")) {
        sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
        return true;
      } else if (e.toString().contains("java.lang.ArrayIndexOutOfBoundsException")) {
        sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
        return true;
      } else  if (e.toString().contains("empty result set.")) {
        sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
        return true;          
      } else {
        sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
        return true;
      }
    }


  }
}


//    
//    
//    
//    
//    // Make sure ticket exists
//    if (!plugin.getStorageConfig().contains(args[0])) {
//      sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.WHITE+"Ticket " + ChatColor.GOLD + ticketno + ChatColor.WHITE + " Does Not Exist");
//      return true;
//    } else {
//
//      String tickdesc = plugin.getStorageConfig().getString(ticketno+".description");
//      String date = plugin.getStorageConfig().getString(ticketno+".dates");
//      String placedby =  plugin.getStorageConfig().getString(ticketno+".placedby");
//      String loc =  plugin.getStorageConfig().getString(ticketno+".location");
//      String reply = plugin.getStorageConfig().getString(ticketno+".reply");
//      String admin = plugin.getStorageConfig().getString(ticketno+".admin");
//
//      plugin.getStorageConfig().set(ticketno+".admin", player.getDisplayName());      
//      plugin.saveStorageConfig();
//
//      if (loc.contains("none")) { // if console ticket
//        sender.sendMessage(ChatColor.GOLD + "[ " + ChatColor.WHITE + "Ticket " + ticketno + ChatColor.GOLD + " ]");
//        sender.sendMessage(" " + ChatColor.BLUE + "Placed By: " + ChatColor.WHITE + placedby);
//        sender.sendMessage(" " + ChatColor.BLUE + "Date: " + ChatColor.WHITE + date);
//        sender.sendMessage(" " + ChatColor.BLUE + "Location: " + ChatColor.RED + "None [Console Ticket]");
//        sender.sendMessage(" " + ChatColor.BLUE + "Assigned Admin: " + ChatColor.WHITE + admin);
//        sender.sendMessage(" " + ChatColor.BLUE + "Ticket: " + ChatColor.GREEN + tickdesc);
//        sender.sendMessage(" " + ChatColor.BLUE + "Reply: " + ChatColor.YELLOW + reply);          
//        String tickuser = plugin.myGetPlayerName(placedby);
//        if(plugin.getServer().getPlayer(tickuser) == null) {
//          return true;  
//        } else {
//          String admin1 = player.getDisplayName();
//          Player target = plugin.getServer().getPlayer(tickuser);
//          String TicketReview = plugin.getConfig().getString("TicketBeingReviewedMsg");
//          if (TicketReview.equalsIgnoreCase("DEFAULT")) {
//            target.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GOLD + admin1 + ChatColor.WHITE + " is reviewing your help ticket");
//            return true;
//          } else {
//            target.sendMessage(ChatColor.GREEN + TicketReview);
//            return true;
//          }
//
//        }
//      } else {
//        // compile location
//        String[] vals = loc.split(",");
//        World world = Bukkit.getWorld(vals[0]);
//        double x = Double.parseDouble(vals[1]);        
//        double y = Double.parseDouble(vals[2]);
//        double z = Double.parseDouble(vals[3]);
//        Location locc = new Location(world, x, y, z);
//        player.teleport(locc);
//        sender.sendMessage(ChatColor.GOLD + "[ " + ChatColor.WHITE + "Ticket " + ticketno + ChatColor.GOLD + " ]");
//        sender.sendMessage(" " + ChatColor.BLUE + "Placed By: " + ChatColor.WHITE + placedby);
//        sender.sendMessage(" " + ChatColor.BLUE + "Date: " + ChatColor.WHITE + date);
//        sender.sendMessage(" " + ChatColor.BLUE + "Assigned Admin: " + ChatColor.WHITE + admin);
//        sender.sendMessage(" " + ChatColor.BLUE + "Ticket: " + ChatColor.GREEN + tickdesc);
//        sender.sendMessage(" " + ChatColor.BLUE + "Reply: " + ChatColor.YELLOW + reply);
//        //      sender.sendMessage(" " + ChatColor.BLUE + "Location: " + ChatColor.GREEN + tickLOC);
//        String tickuser = plugin.myGetPlayerName(placedby);
//        if(plugin.getServer().getPlayer(tickuser) == null) {
//          return true;  
//        } else {
//          String admin1 = player.getDisplayName();
//          Player target = plugin.getServer().getPlayer(tickuser);
//          String TicketReview = plugin.getConfig().getString("TicketBeingReviewedMsg");
//          if (TicketReview.equalsIgnoreCase("DEFAULT")) {
//            target.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GOLD + admin1 + ChatColor.WHITE + " is reviewing your help ticket");
//            return true;
//          } else {
//            target.sendMessage(ChatColor.GREEN + TicketReview);
//            return true;
//          }
//
//        }
//      }
//    }
//  }
//}