package me.odium.simplehelptickets.commands;

import java.util.Arrays;

import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ticket implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public ticket(SimpleHelpTickets plugin)  {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

    if(args.length == 0) {
      sender.sendMessage(plugin.GOLD+"[ SimpleHelpTickets ]");
      sender.sendMessage(plugin.WHITE+" To request help or report grief, stand at the relevant location and open a ticket with an informative description of the issue.");
      sender.sendMessage(plugin.GRAY+"For more information, type: " +plugin.GREEN+ "/sht");
      
      
    } else {
      // CONSOLE COMMANDS
      if (player == null) {
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
            String sb2 = "none";
            java.util.List<String> Tickets = plugin.getStorageConfig().getStringList("Tickets");
            int Tsize = Tickets.size();
            if (Tsize == 0) {
              int TicketNumber = 0;
              Tickets.add(""+TicketNumber); 
              plugin.getStorageConfig().set("Tickets", Tickets); 
              plugin.getStorageConfig().set(TicketNumber+".description", details);  // insert the ticket
              plugin.getStorageConfig().set(TicketNumber+".dates", plugin.getCurrentDTG("date")); // insert the date
              plugin.getStorageConfig().set(TicketNumber+".placedby", "Console"); // insert who placed the ticket
              plugin.getStorageConfig().set(TicketNumber+".location", sb2); // insert location of the ticket
              plugin.getStorageConfig().set(TicketNumber+".reply", "NONE"); // insert reply
              plugin.getStorageConfig().set(TicketNumber+".admin", "noone"); // insert admin who has viewed     
              ++TicketNumber;
              plugin.getStorageConfig().set("ticketnumber", TicketNumber);  // insert the ticket

              String username = "Console";              
              if (plugin.getStorageConfig().getString(username) == null) {
                plugin.getStorageConfig().set(username, 1);
              } else {
                String UsersNoOfTickets = plugin.getStorageConfig().getString(username);
                int temp1 = Integer.parseInt( UsersNoOfTickets );
                ++temp1;
                plugin.getStorageConfig().set(username, temp1);
              }
              plugin.saveStorageConfig();
            } else {
              --Tsize;
              String finalinlist = Tickets.get(Tsize);            
              int sizetemp = Integer.parseInt( finalinlist );
              sizetemp++;
              int TicketNumber = sizetemp;
              Tickets.add(""+TicketNumber);
              plugin.getStorageConfig().set("Tickets", Tickets);  // create the new ticket name
              plugin.getStorageConfig().set(TicketNumber+".description", details);  // insert the ticket
              plugin.getStorageConfig().set(TicketNumber+".dates", plugin.getCurrentDTG("date")); // insert the date
              plugin.getStorageConfig().set(TicketNumber+".placedby", "Console"); // insert who placed the ticket
              plugin.getStorageConfig().set(TicketNumber+".location", sb2); // insert location of the ticket
              plugin.getStorageConfig().set(TicketNumber+".reply", "NONE"); // insert admin who has viewed
              plugin.getStorageConfig().set(TicketNumber+".admin", "noone"); // insert admin who has viewed 
              plugin.getStorageConfig().set("ticketnumber", Tickets.size());  // Set the ticketnumber to the size of the Ticket's list

              String username = "Console";          
              if (plugin.getStorageConfig().getString(username) == null) {
                plugin.getStorageConfig().set(username, 1);
              } else {
                String UsersNoOfTickets = plugin.getStorageConfig().getString(username);
                int temp1 = Integer.parseInt( UsersNoOfTickets );
                ++temp1;
                plugin.getStorageConfig().set(username, temp1);
              }
              plugin.saveStorageConfig();
            }          

            sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GREEN + "Your ticket has been logged");

            Player[] players = Bukkit.getOnlinePlayers();
            for(Player op: players){
              if(op.hasPermission("sht.admin")) {
                op.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.GOLD+"Console" + ChatColor.WHITE + " has opened a " + ChatColor.GOLD + "Help Ticket");
              }
            }
            return true;
            // Player Commands
      } else if (plugin.getStorageConfig().getString(player.getDisplayName()) != null ) {
        String UsersNoOfTickets = plugin.getStorageConfig().getString(player.getDisplayName());
        int temp1 = Integer.parseInt( UsersNoOfTickets );
        int MaxTickets = plugin.getConfig().getInt("MaxTickets");
        if (temp1 >= MaxTickets) {
          sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.RED + "You've reached your limit of " + MaxTickets + " tickets.");
          return true;
        }
      }
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
          World PlayerWorld = player.getWorld();
          String PlayerWorldName = PlayerWorld.getName();
          double locX = player.getLocation().getX();
          double locY = player.getLocation().getY();
          double locZ = player.getLocation().getZ();

          StringBuilder sb1 = new StringBuilder();
          sb1.append(PlayerWorldName+",");
          sb1.append(locX+",");
          sb1.append(locY+",");
          sb1.append(locZ);
          String sb2 = sb1.toString();

          java.util.List<String> Tickets = plugin.getStorageConfig().getStringList("Tickets");
          int Tsize = Tickets.size();
          if (Tsize == 0) {
            int TicketNumber = 0;
            Tickets.add(""+TicketNumber); 
            plugin.getStorageConfig().set("Tickets", Tickets); 
            plugin.getStorageConfig().set(TicketNumber+".description", details);  // insert the ticket
            plugin.getStorageConfig().set(TicketNumber+".dates", plugin.getCurrentDTG("date")); // insert the date
            plugin.getStorageConfig().set(TicketNumber+".placedby", player.getDisplayName()); // insert who placed the ticket
            plugin.getStorageConfig().set(TicketNumber+".location", sb2); // insert location of the ticket
            plugin.getStorageConfig().set(TicketNumber+".reply", "NONE"); // insert reply
            plugin.getStorageConfig().set(TicketNumber+".admin", "noone"); // insert admin who has viewed     
            ++TicketNumber;
            plugin.getStorageConfig().set("ticketnumber", TicketNumber);  // insert the ticket

            String username = player.getDisplayName();              
            if (plugin.getStorageConfig().getString(username) == null) {
              plugin.getStorageConfig().set(username, 1);
            } else {
              String UsersNoOfTickets = plugin.getStorageConfig().getString(username);
              int temp1 = Integer.parseInt( UsersNoOfTickets );
              ++temp1;
              plugin.getStorageConfig().set(username, temp1);
            }
            plugin.saveStorageConfig();
          } else {
            --Tsize;
            String finalinlist = Tickets.get(Tsize);            
            int sizetemp = Integer.parseInt( finalinlist );
            sizetemp++;
            int TicketNumber = sizetemp;
            Tickets.add(""+TicketNumber);
            plugin.getStorageConfig().set("Tickets", Tickets);  // create the new ticket name
            plugin.getStorageConfig().set(TicketNumber+".description", details);  // insert the ticket
            plugin.getStorageConfig().set(TicketNumber+".dates", plugin.getCurrentDTG("date")); // insert the date
            plugin.getStorageConfig().set(TicketNumber+".placedby", player.getDisplayName()); // insert who placed the ticket
            plugin.getStorageConfig().set(TicketNumber+".location", sb2); // insert location of the ticket
            plugin.getStorageConfig().set(TicketNumber+".reply", "NONE"); // insert admin who has viewed
            plugin.getStorageConfig().set(TicketNumber+".admin", "noone"); // insert admin who has viewed 
            plugin.getStorageConfig().set("ticketnumber", Tickets.size());  // Set the ticketnumber to the size of the Ticket's list

            String username = player.getDisplayName();              
            if (plugin.getStorageConfig().getString(username) == null) {
              plugin.getStorageConfig().set(username, 1);
            } else {
              String UsersNoOfTickets = plugin.getStorageConfig().getString(username);
              int temp1 = Integer.parseInt( UsersNoOfTickets );
              ++temp1;
              plugin.getStorageConfig().set(username, temp1);
            }
            plugin.saveStorageConfig();
          }          

          sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GREEN + "Your ticket has been logged");

          Player[] players = Bukkit.getOnlinePlayers();
          for(Player op: players){
            if(op.hasPermission("sht.admin") && op != player) {
              op.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+plugin.GOLD+sender.getName() + ChatColor.WHITE + " has opened a " + ChatColor.GOLD + "Help Ticket");
            }
          }
    }   

    return true;    
  }

}