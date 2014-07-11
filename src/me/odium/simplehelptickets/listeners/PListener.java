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
            rs.close();
            stmt.close();
          }
        } catch(Exception e) {
          plugin.log.info(plugin.getMessage("Error").replace("&arg", e.toString()));
        }

      }
      // IF PLAYER IS USER      
    } else {
      boolean DisplayTicketUser = plugin.getConfig().getBoolean("OnJoin.DisplayTicketUser");

      if (DisplayTicketUser == true) {

        if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {
          try {
            con = plugin.mysql.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM SHT_Tickets WHERE uuid='"+player.getUniqueId().toString()+"'" );      
            rs.next(); //sets pointer to first record in result set

            int ticketTotal = rs.getInt("ticketTotal");
            if (ticketTotal == 0) {
              // DO NOTHING              
              rs.close();
              stmt.close();
            } else if(ticketTotal > 0) {              
              rs.close();
              rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE uuid='"+player.getUniqueId().toString()+"'" );
              int openNumber = 0;              
              while (rs.next()) {
                String adminreply = rs.getString("adminreply");
                String status = rs.getString("status");
                if (status.equalsIgnoreCase("OPEN")) {
                  openNumber++;
                }                
                if (!adminreply.equalsIgnoreCase("NONE") && status.equalsIgnoreCase("OPEN")) {
                  player.sendMessage(plugin.getMessage("UserJoin-TicketReplied"));
                }
              }              
              if (DisplayTicketUser == true && openNumber > 0) {                
                player.sendMessage(plugin.getMessage("UserJoin").replace("&arg", openNumber+""));              
              }
              rs.close();
              stmt.close();      
            }
          } catch(Exception e) {
            plugin.log.info(plugin.getMessage("Error").replace("&arg", e.toString()));
          }      
        } else {

          try {
            con = service.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(id) AS ticketTotal FROM SHT_Tickets WHERE uuid='"+player.getUniqueId().toString()+"'" );

            int ticketTotal = rs.getInt("ticketTotal");
            if (ticketTotal == 0) {
              // DO NOTHING
              rs.close();
              stmt.close();
            } else if(ticketTotal > 0) {
              rs.close();
              rs = stmt.executeQuery("SELECT * FROM SHT_Tickets WHERE uuid='"+player.getUniqueId().toString()+"'" );
              int openNumber = 0;

              while (rs.next()) {
                String adminreply = rs.getString("adminreply");
                String status = rs.getString("status");
                if (status.equalsIgnoreCase("OPEN")) {
                  openNumber++;
                }                
                if (!adminreply.equalsIgnoreCase("NONE") && status.equalsIgnoreCase("OPEN")) {
                  player.sendMessage(plugin.getMessage("UserJoin-TicketReplied"));
                }
              }
              if (DisplayTicketUser == true && openNumber > 0) {                
                player.sendMessage(plugin.getMessage("UserJoin").replace("&arg", openNumber+""));              
              }

              rs.close();
              stmt.close();
            }
          } catch(Exception e) {
            plugin.log.info(plugin.getMessage("Error").replace("&arg", e.toString()));
          } 
        }
      }
    }
  }
}