package me.nerumir.guildclaims.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRolePerm;
import me.nerumir.guildclaims.Claim;
import me.nerumir.guildclaims.Main;

public class Protection implements Listener{
	
	//prevent PvP damages if player is in his claim
	@EventHandler
	public void onHit(EntityDamageByEntityEvent event) {
		//consider bypassing players
		if(event.getDamager() instanceof Player) {
			Player bypasser = (Player) event.getDamager();
			if(Main.getBypassPlayers().contains(bypasser.getUniqueId())) {
				return;
			}
		}
		//check if event is not canceled and the world is a valid claiming world.
		if(!event.isCancelled() && Claim.validWorld(event.getEntity().getWorld())) {
			//Prevent hit damages
			Entity event_entity = event.getEntity();
			Entity event_attacker = event.getDamager();
			HashMap<String, String> messages = Main.getMessages();
			if(event_entity instanceof Player && event_attacker instanceof Player) {
				Player p = (Player) event_entity;
				Player attacker = (Player) event_attacker;
				//check if the player being hit is in his own guild claim
				if(Claim.inHisClaim(p)) {
					String guildName = Main.getGuilds().getGuild(p).getName();
					String message = messages.get("cannot-do-that").replace("{name}", guildName);
					attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
					event.setCancelled(true);
					return;
				}
			}
			//prevent projectile damages
			if(event_entity instanceof Player && event_attacker instanceof Projectile) {
				Projectile prj = (Projectile) event_attacker;
				Entity attacker_entity = (Entity) prj.getShooter();
				if(attacker_entity instanceof Player) {
					Player p = (Player) event_entity;
					Player attacker = (Player) attacker_entity;
					if(Claim.inHisClaim(p)) {
						String guildName = Main.getGuilds().getGuild(p).getName();
						String message = messages.get("cannot-do-that").replace("{name}", guildName);
						attacker.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
						event.setCancelled(true);
						return;
					}
				}
			}
			return;
		}
		return;
	}
	
	@EventHandler
	public void onExplosion(EntityExplodeEvent event) {
		//check if explosions are disabled in config.yml
		if(!Main.getExplosions()) {
			//check if event is not canceled and the world is a valid claiming world.
			if(!event.isCancelled() && Claim.validWorld(event.getLocation().getWorld())) {
				UUID guildTerritory = Claim.claimOfPos(event.getLocation());
				//if block is in any claim, cancel the event.
				if(!guildTerritory.equals(new UUID(0,0))) {
					event.setCancelled(true);
				}
			}
		}
		return;
	}
	
	//prevent placing block if player is in another guild's claim or doesn't have the place role perm
	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		//consider bypassing players
		if(Main.getBypassPlayers().contains(p.getUniqueId())) {
			return;
		}
		//check if event is not canceled and the world is a valid claiming world.
		if(!event.isCancelled() && Claim.validWorld(p.getWorld())) {
			UUID guildTerritory = Claim.claimOfPos(event.getBlockPlaced().getLocation());
			//if block not in any claim, return.
			if(guildTerritory.equals(new UUID(0,0))) {
				return;
			}
			HashMap<String, String> messages = Main.getMessages();
			//check if a player even has guild or claim, if not, cancel the event.
			Guild playerGuild = Main.getGuilds().getGuild(p);
			if(playerGuild == null) {
				String guildName = Main.getGuilds().getGuild(guildTerritory).getName();
				String message = messages.get("cannot-do-that").replace("{name}", guildName);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				event.setCancelled(true);
				return;
			}
			if(!Main.getClaims().containsKey(playerGuild.getId().toString())) {
				String guildName = Main.getGuilds().getGuild(guildTerritory).getName();
				String message = messages.get("cannot-do-that").replace("{name}", guildName);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				event.setCancelled(true);
				return;
			}
			//if player has a guild, check if the claim of the block is his own claim
			if(!guildTerritory.equals(playerGuild.getId())  || !Main.getGuilds().getGuildRole(p).hasPerm(GuildRolePerm.PLACE)) {
				String guildName = Main.getGuilds().getGuild(guildTerritory).getName();
				String message = messages.get("cannot-do-that").replace("{name}", guildName);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				event.setCancelled(true);
				return;
			}
		}
		return;
	}
	
	//prevent breaking block if player is in another guild's claim or doesn't have the destroy role perm
	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		//consider bypassing players
		if(Main.getBypassPlayers().contains(p.getUniqueId())) {
			return;
		}
		//check if event is not canceled and the world is a valid claiming world.
		if(!event.isCancelled() && Claim.validWorld(p.getWorld())) {
			UUID guildTerritory = Claim.claimOfPos(event.getBlock().getLocation());
			//if block not in any claim, return.
			if(guildTerritory.equals(new UUID(0,0))) {
				return;
			}
			HashMap<String, String> messages = Main.getMessages();
			//check if a player even has guild or claim, if not, cancel the event.
			Guild playerGuild = Main.getGuilds().getGuild(p);
			if(playerGuild == null) {
				String guildName = Main.getGuilds().getGuild(guildTerritory).getName();
				String message = messages.get("cannot-do-that").replace("{name}", guildName);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				event.setCancelled(true);
				return;
			}
			if(!Main.getClaims().containsKey(playerGuild.getId().toString())) {
				String guildName = Main.getGuilds().getGuild(guildTerritory).getName();
				String message = messages.get("cannot-do-that").replace("{name}", guildName);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				event.setCancelled(true);
				return;
			}
			//if player has a guild, check if the claim of the block is his own claim
			if(!guildTerritory.equals(playerGuild.getId()) || !Main.getGuilds().getGuildRole(p).hasPerm(GuildRolePerm.DESTROY)) {
				String guildName = Main.getGuilds().getGuild(guildTerritory).getName();
				String message = messages.get("cannot-do-that").replace("{name}", guildName);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				event.setCancelled(true);
				return;
			}
		}
		return;
	}
	
	//Si le joueur essaye d'utiliser un oeuf de creeper, alors ça va l'activer manuellement.
	public void onCreeperEggUse(PlayerInteractEvent event) {
		//check if creeper eggs are disabled in config.yml
		if(Main.getCreeperEggs()) {
			Player p = event.getPlayer();
			//check if player try to spawn a creeper
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getMaterial() == Material.CREEPER_SPAWN_EGG) {
				//take back one creeper spawn egg
				if(p.getInventory().getItemInMainHand().getType() == Material.CREEPER_SPAWN_EGG) {
					ItemStack stack = p.getInventory().getItemInMainHand();
					stack.setAmount(stack.getAmount() -1);
					p.updateInventory();
				}
				else if(p.getInventory().getItemInOffHand().getType() == Material.CREEPER_SPAWN_EGG){
					ItemStack stack = p.getInventory().getItemInOffHand();
					stack.setAmount(stack.getAmount() -1);
					p.updateInventory();
				}
				//spawn creeper manually
				p.getWorld().spawnEntity(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(), EntityType.CREEPER);
			}
		}
	}
	
	//prevent interactions with blocks and entities if player is in another guild's claim or doesn't have the interact role perm
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		//consider bypassing players
		if(Main.getBypassPlayers().contains(p.getUniqueId())) {
			return;
		}
		//check if event is not canceled and the world is a valid claiming world.
		if(Claim.validWorld(p.getWorld()) && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			UUID guildTerritory = Claim.claimOfPos(event.getClickedBlock().getLocation());
			//if block not in any claim, return.
			if(guildTerritory.equals(new UUID(0,0))) {
				return;
			}
			HashMap<String, String> messages = Main.getMessages();
			//check if a player even has guild or claim, if not, cancel the event.
			Guild playerGuild = Main.getGuilds().getGuild(p);
			if(playerGuild == null) {
				String guildName = Main.getGuilds().getGuild(guildTerritory).getName();
				String message = messages.get("cannot-do-that").replace("{name}", guildName);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				event.setUseInteractedBlock(Result.DENY);
				event.setUseItemInHand(Result.DENY);
				onCreeperEggUse(event);
				return;
			}
			if(!Main.getClaims().containsKey(playerGuild.getId().toString())) {
				String guildName = Main.getGuilds().getGuild(guildTerritory).getName();
				String message = messages.get("cannot-do-that").replace("{name}", guildName);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				event.setUseInteractedBlock(Result.DENY);
				event.setUseItemInHand(Result.DENY);
				onCreeperEggUse(event);
				return;
			}
			//if player has a guild, check if the claim of the block is his own claim
			if(!guildTerritory.equals(playerGuild.getId()) || !Main.getGuilds().getGuildRole(p).hasPerm(GuildRolePerm.INTERACT)) {
				String guildName = Main.getGuilds().getGuild(guildTerritory).getName();
				String message = messages.get("cannot-do-that").replace("{name}", guildName);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				event.setUseInteractedBlock(Result.DENY);
				event.setUseItemInHand(Result.DENY);
				onCreeperEggUse(event);
				return;
			}
		}
		return;
	}
}
