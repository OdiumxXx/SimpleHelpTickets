package me.odium.simplehelptickets.listeners;

import java.sql.Connection;
import java.sql.ResultSet;
import me.odium.simplehelptickets.DBConnection;
import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PListener implements Listener {

  public SimpleHelpTickets plugin;
  public PListener(SimpleHelpTickets plugin) {
    this.plugin = plugin;    
    Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
  }

  DBConnection service = DBConnection.getInstance();
  ResultSet rs;
  java.sql.Statement stmt;
  Connection con;

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerJoin(PlayerJoinEvent event) {      
    Player player = event.getPlayer();
    // IF PLAYER IS ADMIN
    if (player.hasPermission("sht.admin")) {
      boolean DisplayTicketAdmin = plugin.getConfig().getBoolean("OnJoin.DisplayTicketAdmin");      
      if (DisplayTicketAdmin == true) {

        try {        
          if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
            con = plugin.mysql.getConnection();
          } else {
            con = service.getConnection();
          }
          stmt = con.createStatement();

          rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM SHT_Tickets WHERE status='"+"OPEN"+"'");
          if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
            rs.next(); //sets pointer to first record in result set
          }

          int ticketTotal = rs.getInt("ticketTotal");
          if (ticketTotal == 0) {
            // DO NOTHING
            rs.close();
            stmt.close();
          } else if(ticketTotal > 0) {
            player.sendMessage(plugin.getMessage("AdminJoin").replace("&arg", ticketTotal+""));
            //            player.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GOLD+"* "+ChatColor.WHITE + "There are currently " + ChatColor.GOLD + ticketTotal + ChatColor.WHITE+ " open Help Tickets");
            rs.close();
            stmt.close();
          }
        } catch(Exception e) {
          //          plugin.log.info(plugin.GRAY+"[SimpleHelpTickets] "+plugin.RED+"Error: "+plugin.WHITE+e);
          plugin.log.info(plugin.getMessage("Error").replace("&arg", e.toString()));
        }

      }
      // IF PLAYER IS USER      
    } else {
      boolean DisplayTicketUser = plugin.getConfig().getBoolean("OnJoin.DisplayTicketUser");
      try {   
        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
          con = plugin.mysql.getConnection();
        } else {
          con = service.getConnection();
        }

        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM SHT_Tickets WHERE owner='"+player.getName()+"'" );
                if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
                  rs.next(); //sets pointer to first record in result set
                }
        int ticketTotal = rs.getInt("ticketTotal");
        if (ticketTotal == 0) {
          // DO NOTHING
          rs.close();
          stmt.close();
        } else if(ticketTotal > 0) {
         
          // This is already handled onenable and ondisable - code is broken - and it's overkill anyways
          
//                    rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE owner='"+player.getName()+"'" );
//                    while (rs.next()) {
//                      String date = rs.getString("date");
//                      String expiration = rs.getString("expiration");
//                      String id = rs.getString("id");           
//          
//                      // IF AN EXPIRATION HAS BEEN APPLIED 
//                      if (!expiration.equalsIgnoreCase("NONE")) {
//                        // CONVERT DATE-STRINGS FROM DB TO DATES 
//                        Date dateNEW = new SimpleDateFormat("dd/MMM/yy HH:mm", Locale.ENGLISH).parse(date);
//                        Date expirationNEW = new SimpleDateFormat("dd/MMM/yy HH:mm", Locale.ENGLISH).parse(expiration);
//                        // COMPARE STRINGS
//                        int HasExpired = dateNEW.compareTo(expirationNEW);
//                        if (HasExpired >= 0) {
//                          stmt.executeUpdate("DELETE FROM SHT_Tickets WHERE id='"+id+"'");
//                        }
//                      }
//                    }
//                    rs.close();
//                    rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal2 FROM SHT_Tickets WHERE owner='"+player.getName()+"'" );
//                    if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
//                      rs.next(); //sets pointer to first record in result set
//                    }
//                    int ticketTotal = rs.getInt("ticketTotal");
          // END COMMENTOUT

          rs.close();
          rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE owner='"+player.getName()+"'" );
          if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
            rs.next(); //sets pointer to first record in result set
          }
          String adminreply = rs.getString("adminreply");
          String status = rs.getString("status");
          if (DisplayTicketUser == true) {

            if (adminreply.equalsIgnoreCase("NONE") && status.equalsIgnoreCase("OPEN")) {
              player.sendMessage(plugin.getMessage("UserJoin").replace("&arg", ticketTotal+""));
              rs.close();
              stmt.close();
            } else if (!adminreply.equalsIgnoreCase("NONE") && status.equalsIgnoreCase("OPEN")) {
              player.sendMessage(plugin.getMessage("UserJoin-TicketReplied"));
              rs.close();
              stmt.close();
            }

          }
        }

      } catch(Exception e) {
        plugin.log.info(plugin.getMessage("Error").replace("&arg", e.toString()));
      }      
    }
  }
}

