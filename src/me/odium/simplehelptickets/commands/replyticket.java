package me.odium.simplehelptickets.commands;

import java.util.Arrays;

import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class replyticket implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public replyticket(SimpleHelpTickets plugin)  {
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
          String details = sb.toString();                
          if (player == null) {
            plugin.getStorageConfig().set(ticketno+".reply", "(Console) " + details);
          } else {
            plugin.getStorageConfig().set(ticketno+".reply", "(" + player.getDisplayName() + ") " + details);
          }                    
          plugin.saveStorageConfig();
          sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.GREEN+"Replied to ticket " + ChatColor.GOLD + ticketno);
          String placedby =  plugin.getStorageConfig().getString(ticketno+".placedby");
          if (plugin.getServer().getPlayer(placedby) != null) {
            Player target = plugin.getServer().getPlayer(placedby);
            target.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GOLD + player.getDisplayName() + ChatColor.WHITE + " has replied to your help ticket.");
          }

    return true;    
  }

}