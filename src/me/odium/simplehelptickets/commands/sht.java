package me.odium.simplehelptickets.commands;

import me.odium.simplehelptickets.SimpleHelpTickets;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class sht implements CommandExecutor {   

  public SimpleHelpTickets plugin;
  public sht(SimpleHelpTickets plugin)  {
    this.plugin = plugin;
  }

  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {    
    Player player = null;
    if (sender instanceof Player) {
      player = (Player) sender;
    }

    if(args.length == 0) {      
      plugin.displayHelp(player);
      return true;
    } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
      if(player == null || player.hasPermission("sht.reload") ) {
        plugin.reloadConfig();
        sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.GREEN + "Config Reloaded!");
        return true;
      } else {
        sender.sendMessage(plugin.GRAY+"[SimpleHelpTickets] "+ChatColor.RED + "You do not have permission");
        return true;
      }
    }    

    return true;    
  }

}