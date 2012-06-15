package me.odium.simplehelptickets.commands;

import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class tickets implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public tickets(SimpleHelpTickets plugin)  {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

      int NumberOfTickets = plugin.getStorageConfig().getStringList("Tickets").size();
      sender.sendMessage(ChatColor.GOLD + "[ " + ChatColor.WHITE + "Current Help Tickets" + ChatColor.GOLD + " ]");
      if (NumberOfTickets == 0) {        
        sender.sendMessage(ChatColor.WHITE + " There are currently no help tickets to display.");  
      } else if( NumberOfTickets != 0) {        
        for(int i = 0; i < NumberOfTickets; ++i) {
          java.util.List<String> Tickets = plugin.getStorageConfig().getStringList("Tickets");
          String TicketNumber = Tickets.get(i);
          String TicketDesc = plugin.getStorageConfig().getString(TicketNumber+".description");
          String TicketPB = plugin.getStorageConfig().getString(TicketNumber+".placedby");
          String TicketREPLY = plugin.getStorageConfig().getString(TicketNumber+".reply");          
          if (player != null && !player.hasPermission("sht.admin") && TicketPB.contains(player.getDisplayName())) {            
            if (!TicketREPLY.equalsIgnoreCase("none")) {
              sender.sendMessage(ChatColor.GOLD + " (" + ChatColor.YELLOW + TicketNumber + ChatColor.GOLD + ") " + ChatColor.DARK_GREEN + TicketPB + ChatColor.YELLOW + ": " + TicketDesc);  
            } else {
              sender.sendMessage(ChatColor.GOLD + " (" + ChatColor.WHITE + TicketNumber + ChatColor.GOLD + ") " + ChatColor.DARK_GREEN + TicketPB + ChatColor.WHITE + ": " + TicketDesc);
            }
          } else if(player == null || player.hasPermission("sht.admin")) {            
            if (!TicketREPLY.equalsIgnoreCase("none")) {
              sender.sendMessage(ChatColor.GOLD + " (" + ChatColor.YELLOW + TicketNumber + ChatColor.GOLD + ") " + ChatColor.DARK_GREEN + TicketPB + ChatColor.YELLOW + ": " + TicketDesc);
            } else {
              sender.sendMessage(ChatColor.GOLD + " (" + ChatColor.WHITE + TicketNumber + ChatColor.GOLD + ") " + ChatColor.DARK_GREEN + TicketPB + ChatColor.WHITE + ": " + TicketDesc);
            }
          }
        }
        return true;
      }

    return true;    
  }

}