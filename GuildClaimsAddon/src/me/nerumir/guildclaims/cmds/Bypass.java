package me.nerumir.guildclaims.cmds;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.nerumir.guildclaims.Main;

public class Bypass implements CommandExecutor{
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("cbp")) {
			
			HashMap<String, String> messages = Main.getMessages();
		
			if(sender instanceof Player) {
				Player p = (Player) sender;
				//check if player has perm
				if(p.hasPermission("guilds.bypass")) {
					List<UUID> bypass = Main.getBypassPlayers();
					//check if player already has bypass activated
					if(bypass.contains(p.getUniqueId())) {
						bypass.remove(p.getUniqueId());
						Main.setBypassPlayers(bypass);
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("bypass-off")));
					}
					else {
						bypass.add(p.getUniqueId());
						Main.setBypassPlayers(bypass);
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("bypass-on")));
					}
				}
			}
			else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("no-console")));
			}
		}
		return false;
	}
}
