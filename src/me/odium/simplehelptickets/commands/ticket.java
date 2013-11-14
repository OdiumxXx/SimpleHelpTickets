package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import me.odium.simplehelptickets.SimpleHelpTickets;
import me.odium.simplehelptickets.DBConnection;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ticket implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public ticket(SimpleHelpTickets plugin)  {
    this.plugin = plugin;
  }

  String date;
  String owner;
  String world;
  double locX;
  double locY;
  double locZ;
  double locP;
  double locF;
  String reply;
  String status;
  String admin;
  String adminreply;
  String expire;
  String userreply;

  DBConnection service = DBConnection.getInstance();

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

    if(args.length == 0) {
      sender.sendMessage(plugin.GOLD+"[ SimpleHelpTickets ]");
      sender.sendMessage(plugin.getMessage("HelpMe_Line1"));
      sender.sendMessage(plugin.getMessage("HelpMe_Line2"));
    } else if(args.length > 0) {

      // Build the command string
      StringBuilder sb = new StringBuilder();
      for (String arg : args)
        sb.append(arg + " ");            
          String[] temp = sb.toString().split(" ");
          String[] temp2 = Arrays.copyOfRange(temp, 0, temp.length);
          sb.delete(0, sb.length());
          for (String details : temp2)
          {
            sb.append(details);
            sb.append(" ");
          }
          String details = sb.toString();  

          Connection con;
          @SuppressWarnings("unused")
          java.sql.Statement stmt;

          if (player == null) {     
            // SET VARIABLES FOR CONSOLE
            date = plugin.getCurrentDTG("date");
            owner = "CONSOLE";
            world = "NONE";
            locX = 00;
            locY = 00;
            locZ = 00;
            locP = 00;
            locF = 00;
            adminreply = "NONE";
            userreply = "NONE";
            status = "OPEN";
            admin = "NONE";
            expire = null;
          } else {
            // SET VARIABLES FOR PLAYER
            date = plugin.getCurrentDTG("date");
            owner = player.getName();
            world = player.getWorld().getName();
            locX = player.getLocation().getX();
            locY = player.getLocation().getY();
            locZ = player.getLocation().getZ();
            locP = player.getLocation().getPitch();
            locF = player.getLocation().getYaw();
            adminreply = "NONE";
            userreply = "NONE";
            status = "OPEN";
            admin = "NONE";
            expire = null;
          }

          // REFERENCE CONNECTION AND ADD DATA
          if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {              

            try {
              con = plugin.mysql.getConnection();
              stmt = con.createStatement();
              PreparedStatement statement = con.prepareStatement("insert into SHT_Tickets(description, date, owner, world, x, y, z, p, f, adminreply, userreply, status, admin, expiration) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
              // INSERT INTO lyrics1(name, artist) values(?, ?) [Example]

              statement.setString(1, details);              
              statement.setString(2, date );             
              statement.setString(3, owner);
              statement.setString(4, world);
              statement.setDouble(5, locX);
              statement.setDouble(6, locY);
              statement.setDouble(7, locZ);
              statement.setDouble(8, locP);
              statement.setDouble(9, locF);
              statement.setString(10, adminreply);
              statement.setString(11, userreply);
              statement.setString(12, status);
              statement.setString(13, admin);
              statement.setString(14, expire);

              statement.executeUpdate();
              statement.close();

              // Message player and finish
              sender.sendMessage(plugin.getMessage("TicketOpen"));
              // Notify admin of new ticket
              Player[] players = Bukkit.getOnlinePlayers();
              for(Player onlinePlayer: players){ // for every player online
                if(onlinePlayer.hasPermission("sht.admin") && onlinePlayer.getName() != owner) { // if admin perm & not ticket owner                     
                  onlinePlayer.sendMessage(plugin.getMessage("TicketOpenADMIN").replace("%player", owner));
                }
              }


            } catch (SQLException e) {
              sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
            }

          } else {
            try {        
              con = service.getConnection();
              stmt = con.createStatement();
              PreparedStatement statement = con.prepareStatement("insert into SHT_Tickets values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
              // description, date, owner, world, x, y, z, p, f, reply, status, admin

              statement.setString(2, details);              
              statement.setString(3, date);             
              statement.setString(4, owner);
              statement.setString(5, world);
              statement.setDouble(6, locX);
              statement.setDouble(7, locY);
              statement.setDouble(8, locZ);
              statement.setDouble(9, locP);
              statement.setDouble(10, locF);
              statement.setString(11, adminreply);
              statement.setString(12, userreply);
              statement.setString(13, status);
              statement.setString(14, admin);
              statement.setString(15, expire);

              statement.executeUpdate();
              statement.close();

              // Message player and finish
              sender.sendMessage(plugin.getMessage("TicketOpen"));
              // Notify admin of new ticket
              Player[] players = Bukkit.getOnlinePlayers();
              for(Player onlinePlayer: players){ // for every player online
                if(onlinePlayer.hasPermission("sht.admin") && onlinePlayer.getName() != owner) {     // if admin permissiong                     
                  onlinePlayer.sendMessage(plugin.getMessage("TicketOpenADMIN").replace("%player", owner));
                }
              }
            } catch(Exception e) {
              sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
            }
          }
    }
    return true;
  }
}
