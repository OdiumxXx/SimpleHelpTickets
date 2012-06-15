package me.odium.simplehelptickets.commands;

import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class closeticket implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public closeticket(SimpleHelpTickets plugin)  {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

      if(args.length == 0) {        
        sender.sendMessage(ChatColor.WHITE + "/closeticket <#>");
      } else if(args.length == 1) {
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
        int TicketNumber = plugin.getStorageConfig().getInt("ticketnumber");

        String placedby =  plugin.getStorageConfig().getString(ticketno+".placedby");
        if (player != null && !placedby.contains(player.getDisplayName()) && !player.hasPermission("sht.admin")) {
          sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.RED+"This is not your ticket to delete");
        } else if(player != null && placedby.contains(player.getDisplayName()) && !player.hasPermission("sht.admin")) { // if the player owns the ticket (NOTADMIN)

          //          java.util.List<String> Tickets = plugin.getStorageConfig().getStringList("Tickets"); [Moved above for a sanity check.]
          Tickets.remove(args[0]);
          plugin.getStorageConfig().set("Tickets", Tickets);
          plugin.getStorageConfig().set(args[0], null);
          --TicketNumber;
          plugin.getStorageConfig().set("ticketnumber", TicketNumber);

          String username = player.getDisplayName();
          String UsersNoOfTickets = plugin.getStorageConfig().getString(username);
          int temp1 = Integer.parseInt( UsersNoOfTickets );
          --temp1;
          if (temp1 == 0) {
            plugin.getStorageConfig().set(username, null);
          } else {
            plugin.getStorageConfig().set(username, temp1);
          }          
          plugin.saveStorageConfig();
          sender.sendMessage(ChatColor.GREEN + " Ticket " + ticketno + " closed.");
          Player[] players = Bukkit.getOnlinePlayers();
          for(Player op: players){
            if(op.hasPermission("sht.admin") && plugin.getConfig().getBoolean("NotifyAdminOnTicketClose")) {
              op.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.WHITE+"User " + ChatColor.GOLD + player.getDisplayName() + ChatColor.WHITE + " has closed ticket " + ChatColor.GOLD + ticketno);
            }
          } 
          return true;  
        } else if(player == null || player.hasPermission("sht.admin")) {
          //          java.util.List<String> Tickets = plugin.getStorageConfig().getStringList("Tickets"); [Moved above or sanity check.]
          Tickets.remove(args[0]);
          plugin.getStorageConfig().set("Tickets", Tickets);
          plugin.getStorageConfig().set(args[0], null);
          --TicketNumber;
          plugin.getStorageConfig().set("ticketnumber", TicketNumber);

          String username = placedby;
          String UsersNoOfTickets = plugin.getStorageConfig().getString(username);
          int temp1 = Integer.parseInt( UsersNoOfTickets );
          --temp1;
          if (temp1 == 0) {
            plugin.getStorageConfig().set(username, null);
          } else {
            plugin.getStorageConfig().set(username, temp1);
          }
          plugin.saveStorageConfig();
          sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GREEN+"Ticket "+plugin.WHITE+ticketno+plugin.GREEN+" closed");
          if (player == null) {
            String admin = "Console";
            if (plugin.getServer().getPlayer(placedby) != null) {
              Player target = plugin.getServer().getPlayer(placedby);
              target.sendMessage(plugin.WHITE+"[SimpleHelpTickets] "+ChatColor.GOLD + admin + ChatColor.WHITE + " has closed 1 of your help tickets");
            }
            Player[] players = Bukkit.getOnlinePlayers();
            for(Player op: players){
              if(op.hasPermission("sht.admin") && plugin.getConfig().getBoolean("NotifyAdminOnTicketClose") && op != player) {
                op.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GOLD + admin + ChatColor.WHITE + " has closed ticket " + ChatColor.GOLD + ticketno);
              }
            }
          } else {
            String admin = player.getDisplayName();
            if (plugin.getServer().getPlayer(placedby) != null && player != plugin.getServer().getPlayer(placedby)) {
              Player target = plugin.getServer().getPlayer(placedby);
              target.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ ChatColor.GOLD + admin + ChatColor.WHITE + " has closed your help ticket");
            }
            Player[] players = Bukkit.getOnlinePlayers();
            for(Player op: players){
              if(op.hasPermission("sht.admin") && plugin.getConfig().getBoolean("NotifyAdminOnTicketClose") && op != player) {
                op.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ ChatColor.GOLD + admin + ChatColor.WHITE + " has closed ticket " + ChatColor.GOLD + ticketno);
              }
            }
          }
          return true;
        }
      }    

    return true;    
  }

}