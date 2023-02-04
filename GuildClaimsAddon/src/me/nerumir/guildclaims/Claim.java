package me.nerumir.guildclaims;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.glaremasters.guilds.guild.Guild;

public abstract class Claim {
	
	public static Boolean validWorld(World world) {
		
		List<String> worlds = Main.getWorlds();
		Boolean blacklist = Main.getBlacklist();
		Boolean contains = false;
		if(worlds.contains(world.getName())) {
			contains = true;
		}
		
		if((blacklist && !contains) || (contains && !blacklist)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	//get the radius a guild can claim, giving the player.
	public static int claimingRadius(Player p) {
		
		String tier = Integer.toString(Main.getGuilds().getGuild(p).getTier().getLevel());
		int radius = Integer.valueOf(Main.getPlugin().getConfig().getString("tiers."+tier+".radius"));
		return radius;
	}
	
	//check if claiming at the player position would overlap other claims.
	public static Boolean overlap(Player p) {
		//Get needed player's info
		String server = Main.getServerMarker();
		String world = p.getWorld().getName();
		int x = p.getLocation().getBlockX();
		int z = p.getLocation().getBlockZ();
		int radius = claimingRadius(p);
		int[] area = {x-radius, z-radius, x+radius, z+radius};
		//Parse all the claims in cache
		HashMap<String, HashMap<String, Object>> cache = Main.getClaims();
		Set<String> claims = cache.keySet();
		for(String guild_id : claims) {
			HashMap<String, Object> claim = cache.get(guild_id);
			//check if potential new claim would overlap current parsed claim
			if(world == claim.get("world") && server == claim.get("server")) {
				int xi = Integer.valueOf((String) claim.get("x"));
				int zi = Integer.valueOf((String) claim.get("z"));
				int size = Integer.valueOf((String) claim.get("size"));
				int[] rec = {xi-size, zi-size, xi+size, zi+size};
				//if those 2 variables are positives, then it overlap.
				Boolean widthComparison = Math.min(area[2], rec[2]) > Math.max(area[0], rec[0]);
				Boolean heightComparison = Math.min(area[3], rec[3]) > Math.max(area[1], rec[1]);
				if(widthComparison && heightComparison) {
					return true;
				}
			}
		}
		return false;
	}
	
	//check if a player is in his guild's claim
	public static Boolean inHisClaim(Player p) {

		int x = p.getLocation().getBlockX();
		int z = p.getLocation().getBlockZ();
		Guild playerGuild = Main.getGuilds().getGuild(p);
		//check if a player even has guild or claim
		if(playerGuild == null) {
			return false;
		}
		if(!Main.getClaims().containsKey(playerGuild.getId().toString())) {
			return false;
		}
		//retrieve player's claim
		HashMap<String, Object> claim = Main.getClaims().get(playerGuild.getId().toString());
		//check if location is in claim
		if(p.getWorld().getName() == claim.get("world") && Main.getServerMarker() == claim.get("server")) {
			int xi = Integer.valueOf((String) claim.get("x"));
			int zi = Integer.valueOf((String) claim.get("z"));
			int size = Integer.valueOf((String) claim.get("size"));
			int[] rec = {xi-size, zi-size, xi+size, zi+size};
			//check if location is in claim's rectangle
			if(x >= rec[0] && x <= rec[2] && z >= rec[1] && z <= rec[3]) {
				return true;
			}
		}
		return false;
	}
	
	//get UUID of the guild that the given location belongs to (according to claims)
	//return zeros UUID if location not in any claim.
	public static UUID claimOfPos(Location loc) {
		//Get some infos of location
		String server = Main.getServerMarker();
		String world = loc.getWorld().getName();
		int x = loc.getBlockX();
		int z = loc.getBlockZ();
		//Parse all the claims in cache
		HashMap<String, HashMap<String, Object>> cache = Main.getClaims();
		Set<String> claims = cache.keySet();
		for(String guild_id : claims) {
			HashMap<String, Object> claim = cache.get(guild_id);
			//check if location is in current parsed claim
			if(world == claim.get("world") && server == claim.get("server")) {
				int xi = Integer.valueOf((String) claim.get("x"));
				int zi = Integer.valueOf((String) claim.get("z"));
				int size = Integer.valueOf((String) claim.get("size"));
				int[] rec = {xi-size, zi-size, xi+size, zi+size};
				//check if location is in claim's rectangle
				if(x >= rec[0] && x <= rec[2] && z >= rec[1] && z <= rec[3]) {
					return UUID.fromString(guild_id);
				}
			}
		}
		return new UUID(0,0);
	}
	
	//async verify if claim exists, if it doesn't, check if overlaping, and then add claim to cache
	//async update the database or claims.yml file
	//return 0 = overlapping
	//return 1 = success
	//return 2 = already exists
	public static int claim(Player p) {
		Boolean isSQL = Main.getPlugin().getConfig().getBoolean("sql.enabled");
		//check if overlaping
		if(overlap(p)) {
			return 0;
		}
		//check if claim exists in cache
		if(Main.getClaims().keySet().contains(Main.getGuilds().getGuild(p).getId().toString())) {
			return 2;
		}
		//check if claims.yml storage is used
		if(!isSQL) {
			Guild playerGuild = Main.getGuilds().getGuild(p);
        	String guild_id = playerGuild.getId().toString();
        	//Open the claims.yml config
        	File claimsFile = new File(Main.getPlugin().getDataFolder(), "claims.yml");
			FileConfiguration claimsConfig = new YamlConfiguration();
			try {
				claimsConfig.load(claimsFile);
	        } catch (IOException | InvalidConfigurationException e) {
	            e.printStackTrace();
	        }
			//check if claim exists in claims.yml
			if(!claimsConfig.contains(guild_id)) {
				//add new claim in cache
				HashMap<String, HashMap<String, Object>> cache = Main.getClaims();
    			HashMap<String, Object> cache_claim = new HashMap<>();
    			String tier = Integer.toString(playerGuild.getTier().getLevel());
    			cache_claim.put("server",Main.getServerMarker());
    			cache_claim.put("world",p.getWorld().getName());
				cache_claim.put("name",playerGuild.getName());
				cache_claim.put("x",Integer.toString(p.getLocation().getBlockX()));
				cache_claim.put("z",Integer.toString(p.getLocation().getBlockZ()));
				cache_claim.put("size",Main.getPlugin().getConfig().getString("tiers."+tier+".radius"));
    			cache.put(guild_id, cache_claim);
    			Main.setClaims(cache);
    			
    			//update the claim in claims.yml file
    			claimsConfig.set(guild_id+".server", cache_claim.get("server"));
				claimsConfig.set(guild_id+".world", cache_claim.get("world"));
				claimsConfig.set(guild_id+".name", cache_claim.get("name"));
				claimsConfig.set(guild_id+".x", cache_claim.get("x"));
				claimsConfig.set(guild_id+".z", cache_claim.get("z"));
				claimsConfig.set(guild_id+".size", cache_claim.get("size"));
				return 1;
			}
			else {
				return 2;
			}
		}
		//this async start only if SQL is enabled
		Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), new Runnable() {
            @Override
            public void run() {
            	Guild playerGuild = Main.getGuilds().getGuild(p);
            	String guild_id = playerGuild.getId().toString();
            	ResultSet claim = Database.select("SELECT count(*) FROM "+Main.getConnexion().get("table")+" WHERE guild_id = '"+guild_id+"'");
            	try {
					claim.next();
					//check if claim doesn't exist in database.
					if(claim.getInt(1) == 0) {
            			//add new claim in cache
            			HashMap<String, HashMap<String, Object>> cache = Main.getClaims();
            			HashMap<String, Object> cache_claim = new HashMap<>();
            			String tier = Integer.toString(playerGuild.getTier().getLevel());
            			cache_claim.put("server",Main.getServerMarker());
            			cache_claim.put("world",p.getWorld().getName());
        				cache_claim.put("name",playerGuild.getName());
        				cache_claim.put("x",Integer.toString(p.getLocation().getBlockX()));
        				cache_claim.put("z",Integer.toString(p.getLocation().getBlockZ()));
        				cache_claim.put("size",Main.getPlugin().getConfig().getString("tiers."+tier+".radius"));
            			cache.put(guild_id, cache_claim);
            			Main.setClaims(cache);
            			
            			//update the claim row in the database
            			Database.execute("INSERT INTO "+Main.getConnexion().get("table")+" (server, world, guild_name, guild_id, x, z, size)"
        						+ " VALUES ('"+cache_claim.get("server")+"', '"+cache_claim.get("world")+"', '"+cache_claim.get("name")+"', '"
            					+ guild_id
        						+ "', "+cache_claim.get("x")+", "+cache_claim.get("z")+", "+cache_claim.get("size")+")"
        						+ " ON DUPLICATE KEY UPDATE server = '"+cache_claim.get("server")+"', world = '"+cache_claim.get("world")+"',"
        						+ " guild_name = '"+cache_claim.get("name")+"', x = "+cache_claim.get("x")+", z = "+cache_claim.get("z")+","
        						+ " size = "+cache_claim.get("size"));
	            	}
				} catch (SQLException e) {
					e.printStackTrace();
				}
            }
        });
		
		return 1;
	}
	
	//check if claim in cache and if on the server
	//remove the claim from cache and async update the database or claims.yml file.
	//return 0 = claim doesn't exist
	//return 1 = success
	//return 2 = claim not on server
	public static int unclaim(Player p) {
		Guild playerGuild = Main.getGuilds().getGuild(p);
    	String guild_id = playerGuild.getId().toString();
		HashMap<String, HashMap<String, Object>> cache = Main.getClaims();
		//check if claim in cache
		if(cache.containsKey(guild_id)) {
			//check if claim is on server
			if(cache.get(guild_id).get("server") == Main.getServerMarker()) {
				
				cache.remove(guild_id);
				Main.setClaims(cache);
				//check if SQL is enabled
				Boolean isSQL = Main.getPlugin().getConfig().getBoolean("sql.enabled");
				if(isSQL) {
					//remove claim from database async
					Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), new Runnable() {
			            @Override
			            public void run() {
			            	Database.execute("DELETE FROM "+Main.getConnexion().get("table")+" WHERE guild_id = '"+guild_id+"'");
			            }
			        });
				}
				else {
					//remove claim from claims.yml
					File claimsFile = new File(Main.getPlugin().getDataFolder(), "claims.yml");
					FileConfiguration claimsConfig = new YamlConfiguration();
					try {
						claimsConfig.load(claimsFile);
			        } catch (IOException | InvalidConfigurationException e) {
			            e.printStackTrace();
			        }
					claimsConfig.set(guild_id, null);
				}
				return 1;
			}
			else {
				return 2;
			}
		}
		else {
			return 0;
		}
	}
}