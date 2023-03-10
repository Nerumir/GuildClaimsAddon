package me.nerumir.guildclaims.listeners;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import me.nerumir.guildclaims.Claim;
import me.nerumir.guildclaims.Main;
import me.nerumir.guildclaims.Utils;

public class Commands implements Listener {

	//overwrite the /g|guild|guilds claim command of Guilds
	@EventHandler
	public void onClaim(PlayerCommandPreprocessEvent event) throws FileNotFoundException, IOException, InvalidConfigurationException {
		//check if the world is a valid claiming world.
		Player p = event.getPlayer();
		if(event.getMessage().contains("g claim") || event.getMessage().contains("guilds claim") || event.getMessage().contains("guild claim")) {
			//cancel the event to prevent default command to execute.
			event.setCancelled(true);
			HashMap<String, String> messages = Main.getMessages();
			if(Claim.validWorld(p.getWorld())) {
				//check if player is in a guild
				if(Main.getGuilds().getGuild(p) != null) {
					//check if player is the master.
					if(Main.getGuilds().getGuild(p).getGuildMaster().getAsPlayer() == p) {
						int feedback = Claim.claim(p);
						switch(feedback) {
							case 0:
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("claim-overlap")));
								break;
							case 1:
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("claim-success")));
								break;
							case 2:
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("claim-exists")));
								break;
						}
					}
					else{
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("not-leader")));
					}
				}
				else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("not-in-guild")));
				}
				
			}
			else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("not-in-valid-world")));
			}
		}
		return;
	}
	
	//overwrite the /g|guild|guilds unclaim command of Guilds
	@EventHandler
	public void onUnclaim(PlayerCommandPreprocessEvent event) throws IOException {
		Player p = event.getPlayer();
		if(event.getMessage().contains("g unclaim") || 
				event.getMessage().contains("guilds unclaim") || 
				event.getMessage().contains("guild unclaim")) {
			//cancel the event to prevent default command to execute.
			event.setCancelled(true);
			//check if player is in a guild
			HashMap<String, String> messages = Main.getMessages();
			if(Main.getGuilds().getGuild(p) != null) {
				//check if player is the master.
				if(Main.getGuilds().getGuild(p).getGuildMaster().getAsPlayer() == p) {
					int feedback = Claim.unclaim(p);
					switch(feedback) {
						case 0:
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("claim-not-exist")));
							break;
						case 1:
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("unclaim-success")));
							break;
						case 2:
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("claim-not-on-server")));
							break;
					}
				}
				else{
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("not-leader")));
				}
			}
			else {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("not-in-guild")));
			}
		}
		return;
	}
	
	//reload this plugin when Guilds is reloading
	@EventHandler
	public void onPlayerReload(PlayerCommandPreprocessEvent event) throws IOException {
		Player p = event.getPlayer();
		if(event.getMessage().contains("g reload") || 
				event.getMessage().contains("guilds reload") || 
				event.getMessage().contains("guild reload")) {
			//if player has permission, reload the plugin
			if(p.hasPermission("guilds.*")) {
				Utils.reload();
			}
		}
		return;
	}
	@EventHandler
	public void onConsoleReload(ServerCommandEvent event) throws IOException {
		if(event.getCommand().contains("g reload") || 
				event.getCommand().contains("guilds reload") || 
				event.getCommand().contains("guild reload")) {
			Utils.reload();
		}
		return;
	}
}