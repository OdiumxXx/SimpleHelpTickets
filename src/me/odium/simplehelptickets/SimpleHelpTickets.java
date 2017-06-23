package me.odium.simplehelptickets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;


import me.odium.simplehelptickets.commands.checkticket;
import me.odium.simplehelptickets.commands.closeticket;
import me.odium.simplehelptickets.commands.delticket;
import me.odium.simplehelptickets.commands.purgetickets;
import me.odium.simplehelptickets.commands.replyticket;
import me.odium.simplehelptickets.commands.sht;
import me.odium.simplehelptickets.commands.taketicket;
import me.odium.simplehelptickets.commands.ticket;
import me.odium.simplehelptickets.commands.tickets;
import me.odium.simplehelptickets.listeners.PListener;
import me.odium.simplehelptickets.DBConnection;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleHelpTickets extends JavaPlugin {
	public Logger log = Logger.getLogger("Minecraft");

	public ChatColor GREEN = ChatColor.GREEN;
	public ChatColor RED = ChatColor.RED;
	public ChatColor GOLD = ChatColor.GOLD;
	public ChatColor GRAY = ChatColor.GRAY;
	public ChatColor WHITE = ChatColor.WHITE; 
	public ChatColor AQUA = ChatColor.AQUA;

	DBConnection service = DBConnection.getInstance();

	// Custom Config  
	private FileConfiguration OutputConfig = null;
	private File OutputConfigFile = null;

	public void reloadOutputConfig() {
		if (OutputConfigFile == null) {
			OutputConfigFile = new File(getDataFolder(), "output.yml");
		}
		OutputConfig = YamlConfiguration.loadConfiguration(OutputConfigFile);

		// Look for defaults in the jar
		InputStream defConfigStream = getResource("output.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(OutputConfigFile);
			OutputConfig.setDefaults(defConfig);
		}
	}
	public FileConfiguration getOutputConfig() {
		if (OutputConfig == null) {
			reloadOutputConfig();
		}
		return OutputConfig;
	}
	public void saveOutputConfig() {
		if (OutputConfig == null || OutputConfigFile == null) {
			return;
		}
		try {
			OutputConfig.save(OutputConfigFile);
		} catch (IOException ex) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + OutputConfigFile, ex);
		}
	}
	// End Custom Config

	public String replaceColorMacros(String str) {
		str = str.replace("&r", ChatColor.RED.toString());
		str = str.replace("&R", ChatColor.DARK_RED.toString());        
		str = str.replace("&y", ChatColor.YELLOW.toString());
		str = str.replace("&Y", ChatColor.GOLD.toString());
		str = str.replace("&g", ChatColor.GREEN.toString());
		str = str.replace("&G", ChatColor.DARK_GREEN.toString());        
		str = str.replace("&c", ChatColor.AQUA.toString());
		str = str.replace("&C", ChatColor.DARK_AQUA.toString());        
		str = str.replace("&b", ChatColor.BLUE.toString());
		str = str.replace("&B", ChatColor.DARK_BLUE.toString());        
		str = str.replace("&p", ChatColor.LIGHT_PURPLE.toString());
		str = str.replace("&P", ChatColor.DARK_PURPLE.toString());
		str = str.replace("&0", ChatColor.BLACK.toString());
		str = str.replace("&1", ChatColor.DARK_GRAY.toString());
		str = str.replace("&2", ChatColor.GRAY.toString());
		str = str.replace("&w", ChatColor.WHITE.toString());
		str = str.replace("%bold", ChatColor.BOLD.toString());
		str = str.replace("%italic", ChatColor.ITALIC.toString());
		str = str.replace("%underline", ChatColor.UNDERLINE.toString());
		str = str.replace("%strike", ChatColor.STRIKETHROUGH.toString());
		str = str.replace("%reset", ChatColor.RESET.toString());
		return str;
	}
	
  
 public MySQLConnection mysql;

	public void onEnable(){    
		log.info("[" + getDescription().getName() + "] " + getDescription().getVersion() + " enabled.");
		FileConfiguration cfg = getConfig();
		FileConfigurationOptions cfgOptions = cfg.options();
		cfgOptions.copyDefaults(true);
		cfgOptions.copyHeader(true);
		saveConfig();
		// Load Custom Config
		FileConfiguration ccfg = getOutputConfig();
		FileConfigurationOptions ccfgOptions = ccfg.options();
		ccfgOptions.copyDefaults(true);
		ccfgOptions.copyHeader(true);
		saveOutputConfig();
		// declare new listener
		new PListener(this);
		// declare executors
		this.getCommand("sht").setExecutor(new sht(this));
		this.getCommand("ticket").setExecutor(new ticket(this));
		this.getCommand("tickets").setExecutor(new tickets(this));
		this.getCommand("checkticket").setExecutor(new checkticket(this));
		this.getCommand("replyticket").setExecutor(new replyticket(this));
		this.getCommand("taketicket").setExecutor(new taketicket(this));
		this.getCommand("delticket").setExecutor(new delticket(this));
		this.getCommand("closeticket").setExecutor(new closeticket(this));
		this.getCommand("purgetickets").setExecutor(new purgetickets(this));
		// If MySQL
		if (this.getConfig().getBoolean("MySQL.USE_MYSQL")) {
		  // Get Database Details
		  String hostname = this.getConfig().getString("MySQL.hostname");
		  String hostport = this.getConfig().getString("MySQL.hostport");
		  String database = this.getConfig().getString("MySQL.database");
		  String user = this.getConfig().getString("MySQL.user");
		  String password = this.getConfig().getString("MySQL.password");
		//Get Connection         
		 mysql = new MySQLConnection(hostname,hostport,database,user,password);
			// Open Connection
			try{
				mysql.open();
				log.info("[SimpleHelpTickets] Connected to MySQL Database");
				mysql.createTable();				
			}catch (Exception e){
				log.info(e.getMessage());
			}


		} else {
			// Create connection & table
			try {
				service.setPlugin(this);
				service.setConnection();
				service.createTable();
			} catch(Exception e) {
				log.info("[SimpleHelpTickets] "+"Error: "+e); 
			}
		}
			// Check for and delete any expired tickets, display progress.
			log.info("[SimpleHelpTickets] "+expireTickets()+" Expired Tickets Deleted");
		}
	


	public void onDisable(){ 
		// Check for and delete any expired tickets, display progress.
		log.info("[SimpleHelpTickets] "+expireTickets()+" Expired Tickets Deleted");
		service.closeConnection();
		
//		  mysql.close();  
				
		log.info("[" + getDescription().getName() + "] " + getDescription().getVersion() + " disabled.");
	}

	public String myGetPlayerName(String name) { 
		Player caddPlayer = getServer().getPlayerExact(name);
		String pName;
		if(caddPlayer == null) {
			caddPlayer = getServer().getPlayer(name);
			if(caddPlayer == null) {
				pName = name;
			} else {
				pName = caddPlayer.getName();
			}
		} else {
			pName = caddPlayer.getName();
		}
		return pName;
	}

	public String getCurrentDTG (String string) {
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat dtgFormat = null;
		if (getConfig().getBoolean("MySQL.USE_MYSQL")) {
		  dtgFormat = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss");  
		} else {
		  dtgFormat = new SimpleDateFormat ("dd/MMM/yy HH:mm");
		}		
		return dtgFormat.format (currentDate.getTime());
	} 

	public String getExpiration(String date) {  
		int expire = getConfig().getInt("TicketExpiration");
//		for (char c : ticketExpiration.toCharArray()) {
//			if (!Character.isDigit(c)) {
//				ticketExpiration = "7";
//			}
//		}
		SimpleDateFormat dtgFormat = null;
//		int expire = Integer.parseInt(ticketExpiration);
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		cal.add(Calendar.DAY_OF_WEEK, expire);
		java.util.Date expirationDate = cal.getTime();
		if (getConfig().getBoolean("MySQL.USE_MYSQL")) {
		  dtgFormat = new SimpleDateFormat ("yyyy/MM/dd HH:mm:ss");
		} else {
      dtgFormat = new SimpleDateFormat ("dd/MMM/yy HH:mm");    
		}
		return dtgFormat.format (expirationDate);  
	}

	public int expireTickets() {
		ResultSet rs;
		java.sql.Statement stmt;
		Connection con;
		int expirations = 0;
		Date dateNEW;
		Date expirationNEW;
		try {
		  if (getConfig().getBoolean("MySQL.USE_MYSQL")) {
        con = mysql.getConnection();
      } else {
      con = service.getConnection();
      }
			stmt = con.createStatement();
			Statement stmt2 = con.createStatement();
			rs = stmt.executeQuery("SELECT * FROM SHT_Tickets");
			while(rs.next()){
			  String exp = rs.getString("expiration");
				String id = rs.getString("id");
				// IF AN EXPIRATION HAS BEEN APPLIED 
				if (exp != null) {
					// CONVERT DATE-STRINGS FROM DB TO DATES 
				  if (getConfig().getBoolean("MySQL.USE_MYSQL")) {
				    Date date = rs.getTimestamp("date");
		        Date expiration = rs.getTimestamp("expiration");		        
				    dateNEW = date;
				    expirationNEW = expiration;
				  } else {
            String date = rs.getString("date");
            String expiration = rs.getString("expiration");
           
				    dateNEW = new SimpleDateFormat("dd/MMM/yy HH:mm", Locale.ENGLISH).parse(getCurrentDTG(date));
            expirationNEW = new SimpleDateFormat("dd/MMM/yy HH:mm", Locale.ENGLISH).parse(expiration);
				  }
					
					// COMPARE STRINGS
					int HasExpired = dateNEW.compareTo(expirationNEW);
					if (HasExpired >= 0) {
					  expirations++;
						stmt2.executeUpdate("DELETE FROM SHT_Tickets WHERE id='"+id+"'");						          
					} 
				}
			}
			//      return expirations;
		} catch(Exception e) {
			log.info("[SimpleHelpTickets] "+"Error: "+e);
		}  
		return expirations;
	}


	public void displayHelp(CommandSender sender) {
	  sender.sendMessage(ChatColor.GOLD + "[ "+ChatColor.RED+"SimpleHelpTickets" + " v" + getDescription().getVersion() + ChatColor.RESET+ChatColor.GOLD + " ]");
	  sender.sendMessage(getMessage("UserCommandsMenu-helptickets"));
	  sender.sendMessage(getMessage("UserCommandsMenu-helpme"));
	  sender.sendMessage(getMessage("UserCommandsMenu-Title"));
	  sender.sendMessage(getMessage("UserCommandsMenu-ticket"));
	  sender.sendMessage(getMessage("UserCommandsMenu-tickets"));
	  sender.sendMessage(getMessage("UserCommandsMenu-checkticket"));
	  sender.sendMessage(getMessage("UserCommandsMenu-replyticket"));
	  sender.sendMessage(getMessage("UserCommandsMenu-closeticket"));
    sender.sendMessage(getMessage("UserCommandsMenu-delticket"));
    if(sender == null || sender.hasPermission("sht.admin")) {
	    sender.sendMessage(getMessage("AdminCommandsMenu-Title"));
	    sender.sendMessage(getMessage("AdminCommandsMenu-tickets"));
	    sender.sendMessage(getMessage("AdminCommandsMenu-taketicket"));
	    sender.sendMessage(getMessage("AdminCommandsMenu-replyticket"));
	    sender.sendMessage(getMessage("AdminCommandsMenu-closeticket"));
	    sender.sendMessage(getMessage("AdminCommandsMenu-delticket"));
	    sender.sendMessage(getMessage("AdminCommandsMenu-purgeticket"));
	    sender.sendMessage(getMessage("AdminCommandsMenu-reload"));
	  }
	}

	public String getMessage(String phrase) {
	  String prefix;
	  String output;
	  String message; 
	  
	  if (phrase == "AdminCommandsMenu-reload") {
      prefix =  ChatColor.DARK_AQUA + " /helptickets reload";
      output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-reload"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "AdminCommandsMenu-purgeticket") {
      prefix =  ChatColor.DARK_AQUA + " /purgetickets [-c/-a]";
      output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-purgeticket"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "AdminCommandsMenu-delticket") {
      prefix =  ChatColor.DARK_AQUA + " /delticket <#>";
      output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-delticket"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "AdminCommandsMenu-closeticket") {
      prefix =  ChatColor.DARK_AQUA + " /closeticket [r] <#>";
      output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-closeticket"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "AdminCommandsMenu-replyticket") {
      prefix =  ChatColor.DARK_AQUA + " /replyticket <#> <Reply>";
      output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-replyticket"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "AdminCommandsMenu-taketicket") {
      prefix =  ChatColor.DARK_AQUA+ " /taketicket <#>";
      output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-taketicket"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "AdminCommandsMenu-tickets") {
      prefix =  ChatColor.DARK_AQUA + " /tickets [ac]";
      output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-tickets"));
      message = prefix+output; 
      return message;             
    }
	    
	  if (phrase == "AdminCommandsMenu-Title") {
      output = replaceColorMacros(getOutputConfig().getString("AdminCommandsMenu-Title"));
      message = output; 
      return message;             
    }
	  
	  if (phrase == "UserCommandsMenu-delticket") {
      prefix =  ChatColor.GREEN + " /delticket <#>";
      output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-delticket"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "UserCommandsMenu-closeticket") {
      prefix =  ChatColor.GREEN + " /closeticket <#>";
      output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-closeticket"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "UserCommandsMenu-replyticket") {
      prefix =  ChatColor.GREEN + " /replyticket <#> <Reply>";
      output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-replyticket"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "UserCommandsMenu-checkticket") {
      prefix =  ChatColor.GREEN + " /checkticket <#>";
      output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-checkticket"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "UserCommandsMenu-tickets") {
      prefix =  ChatColor.GREEN + " /tickets";
      output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-tickets"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "UserCommandsMenu-ticket") {
      prefix =  ChatColor.GREEN + " /ticket <Description>";
      output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-ticket"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "UserCommandsMenu-Title") {      
      output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-Title"));
      message = output; 
      return message;             
    }
	  
	   if (phrase == "UserCommandsMenu-helpme") {
	      prefix =  ChatColor.DARK_GREEN + " /helpme" + ChatColor.WHITE;
	      output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-helpme"));
	      message = prefix+output; 
	      return message;             
	    }
	  
	  if (phrase == "UserCommandsMenu-helptickets") {
      prefix =  ChatColor.DARK_GREEN + " /helptickets" + ChatColor.WHITE;
      output = replaceColorMacros(getOutputConfig().getString("UserCommandsMenu-helptickets"));
      message = prefix+output; 
      return message;             
    }	  
// END MENU
	  
	  if (phrase == "InvalidTicketNumber") {
	    prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
	    output = replaceColorMacros(getOutputConfig().getString("InvalidTicketNumber"));
	    message = prefix+output; 
	    return message;	     	      
	  }
	  
	   if (phrase == "Error") {
	      prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
	      output = replaceColorMacros(getOutputConfig().getString("Error"));
	      message = prefix+output; 
	      return message;             
	    }

	  if (phrase == "TicketNotExist") {
	    prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
	    output = replaceColorMacros(getOutputConfig().getString("TicketNotExist"));
	    message = prefix+output; 
	    return message;             
	  }

	  if (phrase == "NotYourTicketToCheck") {
	    prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
	    output = replaceColorMacros(getOutputConfig().getString("NotYourTicketToCheck"));
	    message = prefix+output; 
	    return message;             
	  }
	  
	  if (phrase == "NotYourTicketToClose") {
      prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
      output = replaceColorMacros(getOutputConfig().getString("NotYourTicketToClose"));
      message = prefix+output; 
      return message;             
    }
	  
	  if (phrase == "NewConfig") {
	    prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
	    output = replaceColorMacros(getOutputConfig().getString("NewConfig"));
	    message = prefix+output; 
	    return message;             
	  }

	  if (phrase == "NewOutput") {
	    prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
	    output = replaceColorMacros(getOutputConfig().getString("NewOutput"));
	    message = prefix+output; 
	    return message;             
	  }

	  if (phrase == "ConfigReloaded") {
	    prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
	    output = replaceColorMacros(getOutputConfig().getString("ConfigReloaded"));
	    message = prefix+output; 
	    return message;             
	  }

	  if (phrase == "NoPermission") {
	    prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
	    output = replaceColorMacros(getOutputConfig().getString("NoPermission"));
	    message = prefix+output; 
	    return message;             
	  }

	  if (phrase == "TicketAlreadyClosed") {
	    prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
	    output = replaceColorMacros(getOutputConfig().getString("TicketAlreadyClosed"));
	    message = prefix+output; 
	    return message;             
	  }

	  if (phrase == "TicketClosed") {
	    prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
	    output = replaceColorMacros(getOutputConfig().getString("TicketClosed"));
	    message = prefix+output; 
	    return message;             
	  }

	  if (phrase == "TicketClosedOWNER") {
	    prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
	    output = replaceColorMacros(getOutputConfig().getString("TicketClosedOWNER"));
	    message = prefix+output; 
	    return message;             
	  }
	  
	   if (phrase == "TicketClosedADMIN") {
	      prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
	      output = replaceColorMacros(getOutputConfig().getString("TicketClosedADMIN"));
	      message = prefix+output; 
	      return message;             
	    }
	   
	   if (phrase == "TicketNotClosed") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("TicketNotClosed"));
       message = prefix+output; 
       return message;             
     }
	   
     if (phrase == "TicketReopened") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("TicketReopened"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "TicketReopenedOWNER") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("TicketReopenedOWNER"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "TicketReopenedADMIN") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("TicketReopenedADMIN"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "AllClosedTicketsPurged") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("AllClosedTicketsPurged"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "AllTicketsPurged") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("AllTicketsPurged"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "AdminRepliedToTicket") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("AdminRepliedToTicket"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "AdminRepliedToTicketOWNER") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("AdminRepliedToTicketOWNER"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "UserRepliedToTicket") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("UserRepliedToTicket"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "CannotTakeClosedTicket") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("CannotTakeClosedTicket"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "TakeTicketOWNER") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("TakeTicketOWNER"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "TakeTicketADMIN") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("TakeTicketADMIN"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "TicketOpen") { // You have successfully opened a &YHelp Ticket&g, please wait for it to be reviewed"
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("TicketOpen"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "TicketOpenADMIN") { // %player &whas opened a &YHelp Ticket
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("TicketOpenADMIN"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "TicketMax") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("TicketMax"));
       message = prefix+output; 
       return message;             
     }

     if (phrase == "NoTickets") {       
       output = replaceColorMacros(getOutputConfig().getString("NoTickets"));
       message = output; 
       return message;             
     }
     if (phrase == "AdminJoin") {
       prefix =  replaceColorMacros(getOutputConfig().getString("Prefix"));
       output = replaceColorMacros(getOutputConfig().getString("AdminJoin"));
       message = prefix+output; 
       return message;             
     }
     
     if (phrase == "UserJoin") {
       prefix =  ChatColor.GOLD+"* ";
       output = replaceColorMacros(getOutputConfig().getString("UserJoin"));
       message = prefix+output; 
       return message;             
     }     
     if (phrase == "UserJoin-TicketReplied") {
       prefix =  ChatColor.GOLD+"* ";
       output = replaceColorMacros(getOutputConfig().getString("UserJoin-TicketReplied"));
       message = prefix+output; 
       return message;             
     }   
     if (phrase == "HelpMe_Line1") {
       prefix =  ChatColor.GOLD+"* ";
       output = replaceColorMacros(getOutputConfig().getString("HelpMe_Line1"));
       message = output; 
       return message;             
     }
     if (phrase == "HelpMe_Line2") {
       prefix =  ChatColor.GOLD+"* ";
       output = replaceColorMacros(getOutputConfig().getString("HelpMe_Line2"));
       message = output; 
       return message;             
     }
     
     return "Error";
	}



}
