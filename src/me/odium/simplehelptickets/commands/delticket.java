package me.odium.simplehelptickets.commands;

import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class delticket implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public delticket(SimpleHelpTickets plugin)  {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

      if(args.length == 0) {        
        sender.sendMessage(ChatColor.WHITE + "/delticket <#>");
      } else if(args.length == 1) {
        for (char c : args[0].toCharArray()) {
          if (!Character.isDigit(c)) {
            sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.RED + "Invalid Ticket Number: " + ChatColor.WHITE + args[0]);
            return true;
          }
        }
        int ticketno = Integer.parseInt( args[0] );
        int TicketNumber = plugin.getStorageConfig().getInt("ticketnumber");

        String placedby =  plugin.getStorageConfig().getString(ticketno+".placedby");
        if (!placedby.contains(player.getDisplayName()) && !player.hasPermission("sht.admin")) {
          sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.RED+"This is not your ticket to delete");
        } else {

          java.util.List<String> Tickets = plugin.getStorageConfig().getStringList("Tickets");
          Tickets.remove(args[0]);
          plugin.getStorageConfig().set("Tickets", Tickets);
          plugin.getStorageConfig().set(args[0], null);
          --TicketNumber;
          plugin.getStorageConfig().set("ticketnumber", TicketNumber);
          plugin.saveStorageConfig();

          sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GREEN+" Ticket "+plugin.WHITE+ticketno+plugin.GREEN+" deleted");
          return true;
        }
      }


    return true;    
  }

}