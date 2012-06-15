package me.odium.simplehelptickets.commands;

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

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

    if (player == null) {
      sender.sendMessage(plugin.RED+"This command can only be run by a player, use /checkticket instead.");
      return true;
    }
    for (char c : args[0].toCharArray()) {
      if (!Character.isDigit(c)) {
        sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + args[0]);
        return true;
      }
    }

    int ticketno = Integer.parseInt( args[0] );
    // Make sure ticket exists
    if (!plugin.getStorageConfig().contains(args[0])) {
      sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.WHITE+"Ticket " + ChatColor.GOLD + ticketno + ChatColor.WHITE + " Does Not Exist");
      return true;
    } else {

      String tickdesc = plugin.getStorageConfig().getString(ticketno+".description");
      String date = plugin.getStorageConfig().getString(ticketno+".dates");
      String placedby =  plugin.getStorageConfig().getString(ticketno+".placedby");
      String loc =  plugin.getStorageConfig().getString(ticketno+".location");
      String reply = plugin.getStorageConfig().getString(ticketno+".reply");
      String admin = plugin.getStorageConfig().getString(ticketno+".admin");

      plugin.getStorageConfig().set(ticketno+".admin", player.getDisplayName());      
      plugin.saveStorageConfig();

      if (loc.contains("none")) { // if console ticket
        sender.sendMessage(ChatColor.GOLD + "[ " + ChatColor.WHITE + "Ticket " + ticketno + ChatColor.GOLD + " ]");
        sender.sendMessage(" " + ChatColor.BLUE + "Placed By: " + ChatColor.WHITE + placedby);
        sender.sendMessage(" " + ChatColor.BLUE + "Date: " + ChatColor.WHITE + date);
        sender.sendMessage(" " + ChatColor.BLUE + "Location: " + ChatColor.RED + "None [Console Ticket]");
        sender.sendMessage(" " + ChatColor.BLUE + "Assigned Admin: " + ChatColor.WHITE + admin);
        sender.sendMessage(" " + ChatColor.BLUE + "Ticket: " + ChatColor.GREEN + tickdesc);
        sender.sendMessage(" " + ChatColor.BLUE + "Reply: " + ChatColor.YELLOW + reply);          
        String tickuser = plugin.myGetPlayerName(placedby);
        if(plugin.getServer().getPlayer(tickuser) == null) {
          return true;  
        } else {
          String admin1 = player.getDisplayName();
          Player target = plugin.getServer().getPlayer(tickuser);
          String TicketReview = plugin.getConfig().getString("TicketBeingReviewedMsg");
          if (TicketReview == null) {
            target.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GOLD + admin1 + ChatColor.WHITE + " is reviewing your help ticket");
            return true;
          } else {
            target.sendMessage(ChatColor.GREEN + TicketReview);
            return true;
          }

        }
      } else {
        // compile location
        String[] vals = loc.split(",");
        World world = Bukkit.getWorld(vals[0]);
        double x = Double.parseDouble(vals[1]);        
        double y = Double.parseDouble(vals[2]);
        double z = Double.parseDouble(vals[3]);
        Location locc = new Location(world, x, y, z);
        player.teleport(locc);
        sender.sendMessage(ChatColor.GOLD + "[ " + ChatColor.WHITE + "Ticket " + ticketno + ChatColor.GOLD + " ]");
        sender.sendMessage(" " + ChatColor.BLUE + "Placed By: " + ChatColor.WHITE + placedby);
        sender.sendMessage(" " + ChatColor.BLUE + "Date: " + ChatColor.WHITE + date);
        sender.sendMessage(" " + ChatColor.BLUE + "Assigned Admin: " + ChatColor.WHITE + admin);
        sender.sendMessage(" " + ChatColor.BLUE + "Ticket: " + ChatColor.GREEN + tickdesc);
        sender.sendMessage(" " + ChatColor.BLUE + "Reply: " + ChatColor.YELLOW + reply);
        //      sender.sendMessage(" " + ChatColor.BLUE + "Location: " + ChatColor.GREEN + tickLOC);
        String tickuser = plugin.myGetPlayerName(placedby);
        if(plugin.getServer().getPlayer(tickuser) == null) {
          return true;  
        } else {
          String admin1 = player.getDisplayName();
          Player target = plugin.getServer().getPlayer(tickuser);
          String TicketReview = plugin.getConfig().getString("TicketBeingReviewedMsg");
          if (TicketReview == null) {
            target.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GOLD + admin1 + ChatColor.WHITE + " is reviewing your help ticket");
            return true;
          } else {
            target.sendMessage(ChatColor.GREEN + TicketReview);
            return true;
          }

        }
      }
    }
  }
}