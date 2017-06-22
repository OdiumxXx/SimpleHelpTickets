package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;

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
      sender.sendMessage("/replyticket <#> <reply>");
      return true;      
    } else if (args.length > 1) {

      for (char c : args[0].toCharArray()) {
        if (!Character.isDigit(c)) {
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
                sender.sendMessage(plugin.getMessage("TicketNotExist").replace("&arg", args[0]));
                rs.close();
                stmt.close();
                return true;
              }
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
                Player target = Bukkit.getPlayer(rs.getString("uuid"));          
                if (target != null) {
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
              //  CHECK IF PLAYE HAS TICKET PERMS OR ADMIN PERMS
              if (!player.hasPermission("sht.ticket") && !player.hasPermission("sht.admin")) {
                sender.sendMessage(plugin.getMessage("NoPermission"));
                return true;
              }

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
              if (player.getUniqueId().equals((rs.getString("uuid")))) {                

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

                  Collection<? extends Player> players = Bukkit.getOnlinePlayers();
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
                // IF PLAYER ISNT THE TICKET OWNER 
              } else {

                if (!player.hasPermission("sht.admin")) {
                  sender.sendMessage(plugin.getMessage("NoPermission"));
                  return true;
                }

                stmt.executeUpdate("UPDATE SHT_Tickets SET adminreply='"+admin+": "+details+"', admin='"+admin+"' WHERE id='"+id+"'");
                // INFORM OPS THAT AN ADMIN REPLIED TO TICKET
                Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                for(Player op: players){
                  if(op.hasPermission("sht.admin")) {
                    op.sendMessage(plugin.getMessage("AdminRepliedToTicket").replace("&arg", id));
                  }
                }

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
                  // INFORM TICKET-OWNER THAT AN ADMIN REPLIED TO THEIR TICKET
                  Player target = Bukkit.getPlayer(rs.getString("uuid"));          
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