package me.nerumir.guildclaims.listeners;

import java.io.IOException;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.nerumir.guildclaims.Claim;
import me.nerumir.guildclaims.Main;

public class GuildsEvents implements Listener {
	//remove the claim when a guild is dismantled.
	@EventHandler
	public void onGuildRemove(GuildRemoveEvent event) throws IOException {
		Player p = event.getPlayer();
		int feedback = Claim.unclaim(p);
		HashMap<String, String> messages = Main.getMessages();
		if(feedback == 2) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.get("claim-not-on-server")));
			event.setCancelled(true);
		}
		return;
	}
}