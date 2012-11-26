package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;

import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class replyticket implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public replyticket(SimpleHelpTickets plugin)  {
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

    if (args.length <= 1) {      
    	sender.sendMessage(plugin.replaceColorMacros(plugin.getOutputConfig().getString("UserCommandsDescription-replyticket")));
    	return true;   
    } else if (args.length > 1) {

      for (char c : args[0].toCharArray()) {
        if (!Character.isDigit(c)) {
//          sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + args[0]);
          sender.sendMessage(plugin.getMessage("InvalidTicketNumber").replace("&arg", args[0]));
          return true;
        }
      }

      StringBuilder sb = new StringBuilder();
      for (String arg : args)
        sb.append(arg + " ");            
          String[] temp = sb.toString().split(" ");
          String[] temp2 = Arrays.copyOfRange(temp, 1, temp.length);
          sb.delete(0, sb.length());
          for (String details : temp2)
          {
            sb.append(details);
            sb.append(" ");
          }
          String details = sb.toString().replace("'", "''");  
          String id = args[0];

          try {
            if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
              con = plugin.mysql.getConnection();
            } else {
              con = service.getConnection();
            }
            stmt = con.createStatement();


            if (player == null) {
              // CONSOLE COMMANDS
              String admin = "CONSOLE";
              //CHECK IF TICKET EXISTS
              rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM SHT_Tickets WHERE id='"+id+"'");
              if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
                rs.next(); //sets pointer to first record in result set
              }
              if (rs.getInt("ticketTotal") == 0) {
//                sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.RED+"Ticket "+ChatColor.GOLD+args[0]+ChatColor.RED+" does not exist!");
                sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
                rs.close();
                stmt.close();
                return true;
              }
              stmt.executeUpdate("UPDATE SHT_Tickets SET adminreply='"+admin+": "+details+"', admin='"+admin+"' WHERE id='"+id+"'");
//              sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.WHITE+"Replied to Ticket: "+ChatColor.GOLD+id);
              sender.sendMessage(plugin.getMessage("AdminRepliedToTicket").replace("&arg", id));


              try {
                if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
                  con = plugin.mysql.getConnection();
                } else {
                  con = service.getConnection();
                }
                stmt = con.createStatement();

                rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE id='"+id+"'");  
                if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
                  rs.next(); //sets pointer to first record in result set
                }
//                String TicketReply = plugin.getConfig().getString("MessageOutput.TicketReplyMsg");
                Player target = Bukkit.getPlayer(rs.getString("owner"));          
                if (target != null) {
//                  target.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.replaceColorMacros(TicketReply));
                  target.sendMessage(plugin.getMessage("AdminRepliedToTicketOWNER").replace("&arg", id).replace("&admin", admin));
                  return true;                  
                }
              } catch(Exception e) {
                if (e.toString().contains("ResultSet closed")) {
                  sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
                  return true;
                } else {
                  sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
                  return true;
                }
              }




              return true;
            } else {
              // PLAYER COMMANDS
              String admin = player.getName();
              // CHECK IF TICKET EXISTS
              rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM SHT_Tickets WHERE id='"+id+"'");
              if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
                rs.next(); //sets pointer to first record in result set
              }
              if (rs.getInt("ticketTotal") == 0) {
                sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
                rs.close();
                stmt.close();
                return true;
              }

              rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE id='"+id+"'");
              if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
                rs.next(); //sets pointer to first record in result set
              }
              // IF PLAYER IS THE TICKET OWNER
              if (player.getName().equalsIgnoreCase(rs.getString("owner"))) {
                
                stmt.executeUpdate("UPDATE SHT_Tickets SET userreply='"+details+"' WHERE id='"+id+"'");
                sender.sendMessage(plugin.getMessage("AdminRepliedToTicket").replace("&arg", id));

                try {
                  if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
                    con = plugin.mysql.getConnection();
                  } else {
                    con = service.getConnection();
                  }
                  stmt = con.createStatement();
                  rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE id='"+id+"'");
                  if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
                    rs.next(); //sets pointer to first record in result set
                  }

                  Player[] players = Bukkit.getOnlinePlayers();
                  for(Player op: players){
                    if(op.hasPermission("sht.admin") && op != player ) {
                      op.sendMessage(plugin.getMessage("UserRepliedToTicket").replace("%player", player.getName()).replace("&arg", id));
                    }
                  }
                } catch(Exception e) {
                  if (e.toString().contains("ResultSet closed")) {
                    sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
                    return true;
                  } else {
                    sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
                    return true;
                  }         
                }
                
              } else {
              
              stmt.executeUpdate("UPDATE SHT_Tickets SET adminreply='"+admin+": "+details+"', admin='"+admin+"' WHERE id='"+id+"'");
              sender.sendMessage(plugin.getMessage("AdminRepliedToTicket").replace("&arg", id));

              try {
                if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
                  con = plugin.mysql.getConnection();
                } else {
                  con = service.getConnection();
                }
                stmt = con.createStatement();
                rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE id='"+id+"'");
                if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
                  rs.next(); //sets pointer to first record in result set
                }

                Player target = Bukkit.getPlayer(rs.getString("owner"));          
                if (target != null && target != player) {
                  target.sendMessage(plugin.getMessage("AdminRepliedToTicketOWNER").replace("&arg", id).replace("&admin", admin));
                  return true;
                }
              } catch(Exception e) {
                if (e.toString().contains("ResultSet closed")) {
                  sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
                  return true;
                } else {
                  sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
                  return true;
                }         
              }

              return true;
            }
            }
          } catch(Exception e) {
            if (e.toString().contains("ResultSet closed")) {
              sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
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



//      java.util.List<String> Tickets = plugin.getStorageConfig().getStringList("Tickets");
//      if (!Tickets.contains(args[0])) {
//        sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.RED + "Ticket " + ChatColor.WHITE + args[0] + ChatColor.RED + " does not exist.");
//        return true;
//      }      
//      int ticketno = Integer.parseInt( args[0] );
//      StringBuilder sb = new StringBuilder();
//      for (String arg : args)
//        sb.append(arg + " ");          
//          String[] temp = sb.toString().split(" ");
//          String[] temp2 = Arrays.copyOfRange(temp, 1, temp.length);
//          sb.delete(0, sb.length());
//          for (String details : temp2)
//          {
//            sb.append(details);
//            sb.append(" ");
//          }
//          String details = sb.toString();                
//          if (player == null) {
//            plugin.getStorageConfig().set(ticketno+".reply", "(Console) " + details);
//          } else {
//            plugin.getStorageConfig().set(ticketno+".reply", "(" + player.getDisplayName() + ") " + details);
//          }                    
//          plugin.saveStorageConfig();
//          sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.GREEN+"Replied to ticket " + ChatColor.GOLD + ticketno);
//          String placedby =  plugin.getStorageConfig().getString(ticketno+".placedby");
//          if (plugin.getServer().getPlayer(placedby) != null) {
//            Player target = plugin.getServer().getPlayer(placedby);
//            target.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GOLD + player.getDisplayName() + ChatColor.WHITE + " has replied to your help ticket.");
//          }
//
//    return true;    
//  }
//
//}