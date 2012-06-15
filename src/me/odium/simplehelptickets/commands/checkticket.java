package me.odium.simplehelptickets.commands;

import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class checkticket implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public checkticket(SimpleHelpTickets plugin)  {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

    for (char c : args[0].toCharArray()) {
      if (!Character.isDigit(c)) {
        sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + args[0]);
        return true;
      }
    }
    java.util.List<String> Tickets = plugin.getStorageConfig().getStringList("Tickets");
    if (!Tickets.contains(args[0])) {
      sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.RED + "Ticket " + ChatColor.WHITE + args[0] + ChatColor.RED + " does not exist.");
      return true;
    }      
    int ticketno = Integer.parseInt( args[0] );
    //      java.util.List<String> tickets = plugin.getStorageConfig().getStringList("Tickets");  
    String placedby =  plugin.getStorageConfig().getString(ticketno+".placedby");
    if (player != null && !placedby.contains(player.getDisplayName()) && !player.hasPermission("sht.admin")) {
      sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.RED+"This is not your ticket to check");
    } else {
      String tickdesc = plugin.getStorageConfig().getString(ticketno+".description");
      String date = plugin.getStorageConfig().getString(ticketno+".dates");        
      //          String loc =  plugin.getStorageConfig().getString(ticketno+".location");
      String reply = plugin.getStorageConfig().getString(ticketno+".reply");
      String admin = plugin.getStorageConfig().getString(ticketno+".admin");        

      sender.sendMessage(ChatColor.GOLD + "[ " + ChatColor.WHITE + "Ticket " + ticketno + ChatColor.GOLD + " ]");
      sender.sendMessage(" " + ChatColor.BLUE + "Placed By: " + ChatColor.WHITE + placedby);
      sender.sendMessage(" " + ChatColor.BLUE + "Date: " + ChatColor.WHITE + date);
      sender.sendMessage(" " + ChatColor.BLUE + "Assigned Admin: " + ChatColor.WHITE + admin);
      sender.sendMessage(" " + ChatColor.BLUE + "Ticket: " + ChatColor.GREEN + tickdesc);
      sender.sendMessage(" " + ChatColor.BLUE + "Reply: " + ChatColor.YELLOW + reply);
      return true;
    }
    return true;
  }
}
