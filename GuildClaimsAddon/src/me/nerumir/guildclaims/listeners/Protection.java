package me.nerumir.guildclaims.listeners;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
			if(event_entity instanceof Player && event_attacker instanceof Player) {
				Player p = (Player) event_entity;
				//check if the player being hit is in his own guild claim
				if(Claim.inHisClaim(p)) {
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
					if(Claim.inHisClaim(p)) {
						event.setCancelled(true);
						return;
					}
				}
			}
			return;
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
			if(guildTerritory == new UUID(0,0)) {
				return;
			}
			//check if a player even has guild or claim, if not, cancel the event.
			Guild playerGuild = Main.getGuilds().getGuild(p);
			if(playerGuild == null) {
				event.setCancelled(true);
				return;
			}
			if(!Main.getClaims().containsKey(playerGuild.getId().toString())) {
				event.setCancelled(true);
				return;
			}
			//if player has a guild, check if the claim of the block is his own claim
			if(guildTerritory != playerGuild.getId() || !Main.getGuilds().getGuildRole(p).hasPerm(GuildRolePerm.PLACE)) {
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
			if(guildTerritory == new UUID(0,0)) {
				return;
			}
			//check if a player even has guild or claim, if not, cancel the event.
			Guild playerGuild = Main.getGuilds().getGuild(p);
			if(playerGuild == null) {
				event.setCancelled(true);
				return;
			}
			if(!Main.getClaims().containsKey(playerGuild.getId().toString())) {
				event.setCancelled(true);
				return;
			}
			//if player has a guild, check if the claim of the block is his own claim
			if(guildTerritory != playerGuild.getId() || !Main.getGuilds().getGuildRole(p).hasPerm(GuildRolePerm.DESTROY)) {
				event.setCancelled(true);
				return;
			}
		}
		return;
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
		if(Claim.validWorld(p.getWorld())) {
			UUID guildTerritory = Claim.claimOfPos(event.getClickedBlock().getLocation());
			//if block not in any claim, return.
			if(guildTerritory == new UUID(0,0)) {
				return;
			}
			//if the action is to attempt placing a creeper egg, then do and continue to execute the rest of the code, if not, prevent item use.
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getMaterial() == Material.CREEPER_SPAWN_EGG) {
				event.setUseItemInHand(Result.ALLOW);
			}
			else {
				event.setUseItemInHand(Result.DENY);
			}
			//check if a player even has guild or claim, if not, cancel the event.
			Guild playerGuild = Main.getGuilds().getGuild(p);
			if(playerGuild == null) {
				event.setUseInteractedBlock(Result.DENY);
				return;
			}
			if(!Main.getClaims().containsKey(playerGuild.getId().toString())) {
				event.setUseInteractedBlock(Result.DENY);
				return;
			}
			//if player has a guild, check if the claim of the block is his own claim
			if(guildTerritory != playerGuild.getId() || !Main.getGuilds().getGuildRole(p).hasPerm(GuildRolePerm.INTERACT)) {
				event.setUseInteractedBlock(Result.DENY);
				return;
			}
		}
		return;
	}
}
