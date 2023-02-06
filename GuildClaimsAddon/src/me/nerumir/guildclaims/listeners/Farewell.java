package me.nerumir.guildclaims.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.nerumir.guildclaims.Claim;
import me.nerumir.guildclaims.Main;

public class Farewell implements Listener {
	
	//everytime the player change his block position, check if he leaves or enter a claim, to trigger the farewell or greeting message
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		
		//check if player change coord.
		if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
			UUID startTerritory = Claim.claimOfPos(event.getFrom());
			UUID endTerritory = Claim.claimOfPos(event.getTo());
			//check if crossing a claim
			if(!startTerritory.equals(endTerritory)) {
				HashMap<String, String> messages = Main.getMessages();
				//check if leaving a claim
				if(!startTerritory.equals(new UUID(0,0))) {
					String guildName = Main.getGuilds().getGuild(startTerritory).getName();
					String message = messages.get("leaving-claim").replace("%name%", guildName);
					event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				}
				//check if entering a claim
				if(!endTerritory.equals(new UUID(0,0))) {
					String guildName = Main.getGuilds().getGuild(endTerritory).getName();
					String message = messages.get("entering-claim").replace("%name%", guildName);
					event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				}
			}
		}
		return;
	}
}
