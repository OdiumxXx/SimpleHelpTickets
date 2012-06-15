package me.odium.simplehelptickets.listeners;

import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerJoin(PlayerJoinEvent event) {      
    boolean ShowTicketsOnJoin = plugin.getConfig().getBoolean("ShowTicketsOnJoin");
    if (ShowTicketsOnJoin == true) {
      Player player = event.getPlayer();
      if (player.hasPermission("sht.admin")) {
        int ticklength = plugin.getStorageConfig().getStringList("Tickets").size();
        if (ticklength == 0) {
          // DO NOTHING
        } else if(ticklength > 0) {
          player.sendMessage(ChatColor.GOLD + "* " + ChatColor.WHITE + "There are currently " + ChatColor.GOLD + ticklength + ChatColor.WHITE + " open Help Tickets");
        }
      } 
    } else {
      // Do Some Nothing
    }
  }
}
