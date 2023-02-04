package me.nerumir.guildclaims;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class Data {
	
	//initClaims(plugin) --> Check if using MySQL, if yes, then check if claiming table exists, if not, create it, if no, generate YAML file.
	public static void initClaims() throws SQLException, IOException {
		
		Boolean isSQL = Main.getPlugin().getConfig().getBoolean("sql.enabled");
		if(isSQL) {
			//checks if claiming table exists, in not create it.
			if(!Database.tableExistsSQL(Main.getConnexion().get("table"))) {
				
				Database.execute("CREATE TABLE "+Main.getConnexion().get("table")+" ("
						+ "server varchar(255),"
						+ "world varchar(255),"
						+ "guild_name varchar(255),"
						+ "guild_id varchar(255) UNIQUE,"
						+ "x int,"
						+ "z int,"
						+ "size int"
						+ ");");
			}
		}
		else {
			//Check if claims.yml exists, if not, create it.
			File claimsFile = new File(Main.getPlugin().getDataFolder(), "claims.yml");
	        if (!claimsFile.exists()) {
	        	claimsFile.getParentFile().mkdirs();
	        	FileConfiguration claimsConfig = YamlConfiguration.loadConfiguration(claimsFile);
                claimsConfig.save(claimsFile);
	         }
		}
	}
	
	public static void cleanExpiratedClaims() throws SQLException {
		
		Boolean isSQL = Main.getPlugin().getConfig().getBoolean("sql.enabled");
		if(isSQL) {
			//parse all the claims in the database
			ResultSet claims = Database.select("SELECT * FROM "+Main.getConnexion().get("table")+" WHERE 1");
			while(claims.next()) {
				UUID guild = UUID.fromString(claims.getString("guild_id"));
				long lastPlayed = Main.getGuilds().getGuild(guild).getGuildMaster().getAsOfflinePlayer().getLastPlayed();
				String tier = String.valueOf(Main.getGuilds().getGuild(guild).getTier().getLevel());
				long expiration = Main.getPlugin().getConfig().getLong("tiers."+tier+".expiration");
				//check if expirated
				if(lastPlayed < System.currentTimeMillis() - expiration*24*3600*1000) {
					//remove claim from database
					Database.execute("DELETE FROM "+Main.getConnexion().get("table")+" WHERE guild_id = '"+claims.getString("guild_id")+"'");
				}
			}
		}
		else {
			//Ouvrir la config du claims.yml
			File claimsFile = new File(Main.getPlugin().getDataFolder(), "claims.yml");
			FileConfiguration claimsConfig = new YamlConfiguration();
			try {
				claimsConfig.load(claimsFile);
	        } catch (IOException | InvalidConfigurationException e) {
	            e.printStackTrace();
	        }
			//parse all the claims in claims.yml
			Set<String> claims = claimsConfig.getKeys(false);
			for(String guild_id : claims) {
				UUID guild = UUID.fromString(guild_id);
				long lastPlayed = Main.getGuilds().getGuild(guild).getGuildMaster().getAsOfflinePlayer().getLastPlayed();
				String tier = String.valueOf(Main.getGuilds().getGuild(guild).getTier().getLevel());
				long expiration = Main.getPlugin().getConfig().getLong("tiers."+tier+".expiration");
				//check if expirated
				if(lastPlayed < System.currentTimeMillis() - expiration*24*3600*1000) {
					//remove claim from claims.yml
					claimsConfig.set(guild_id, null);
				}
			}
		}
	}
	
	//downloadCache(plugin) --> Rafraîchir le cache en fonction de la méthode de stockage.
	public static HashMap<String, HashMap<String, Object>> downloadCache() throws SQLException{
		
		Boolean isSQL = Main.getPlugin().getConfig().getBoolean("sql.enabled");
		if(isSQL) {
			//return HashMap based on Database rows.
			HashMap<String, HashMap<String, Object>> map = new HashMap<>();
			ResultSet claims = Database.select("SELECT * FROM "+Main.getConnexion().get("table")+" WHERE 1");
			while(claims.next()) {
				
				HashMap<String, Object> claim = new HashMap<>();
				claim.put("server",claims.getString("server"));
				claim.put("world",claims.getString("world"));
				claim.put("name",claims.getString("guild_name"));
				claim.put("x",claims.getString("x"));
				claim.put("z",claims.getString("z"));
				claim.put("size",claims.getString("size"));
				map.put(claims.getString("guild_id"),claim);
			}
			return map;
		}
		else {
			//return HashMap based of claims.yml
			//Déclarer la hashmap vide.
			HashMap<String, HashMap<String, Object>> map = new HashMap<>();
			//Ouvrir la config du claims.yml
			File claimsFile = new File(Main.getPlugin().getDataFolder(), "claims.yml");
			FileConfiguration claimsConfig = new YamlConfiguration();
			try {
				claimsConfig.load(claimsFile);
	        } catch (IOException | InvalidConfigurationException e) {
	            e.printStackTrace();
	        }
			//parcourir les claims de claims.yml et les stocker dans la hashmap.
			Set<String> claims = claimsConfig.getKeys(false);
			for(String guild_id : claims) {
				HashMap<String, Object> claim = new HashMap<>();
				claim.put("server",claimsConfig.getString(guild_id+".server"));
				claim.put("world",claimsConfig.getString(guild_id+".world"));
				claim.put("name",claimsConfig.getString(guild_id+".name"));
				claim.put("x",claimsConfig.getString(guild_id+".x"));
				claim.put("z",claimsConfig.getString(guild_id+".z"));
				claim.put("size",claimsConfig.getString(guild_id+".size"));
				map.put(guild_id,claim);
			}
			//Renvoyer la hashmap.
			return map;
		}
	}
	
	//uploadCache(plugin) --> Actualiser les données stockées en fonction du cache.
	public static void uploadCache() {
		
		Boolean isSQL = Main.getPlugin().getConfig().getBoolean("sql.enabled");
		if(isSQL) {
			//write the cache into the database.
			//déclarer le cache.
			HashMap<String, HashMap<String, Object>> cache = Main.getClaims();
			//parcourir le cache pour ajouter les valeurs à la BDD ou les updates à la duplication
			Set<String> claims = cache.keySet();
			for(String guild_id : claims) {
				HashMap<String, Object> claim = cache.get(guild_id);
				Database.execute("INSERT INTO "+Main.getConnexion().get("table")+" (server, world, guild_name, guild_id, x, z, size)"
						+ " VALUES ('"+claim.get("server")+"', '"+claim.get("world")+"', '"+claim.get("name")+"', '"+guild_id
						+ "', "+claim.get("x")+", "+claim.get("z")+", "+claim.get("size")+")"
						+ " ON DUPLICATE KEY UPDATE server = '"+claim.get("server")+"', world = '"+claim.get("world")+"',"
						+ " guild_name = '"+claim.get("name")+"', x = "+claim.get("x")+", z = "+claim.get("z")+","
						+ " size = "+claim.get("size"));
			}
			//retirer de la BDD les claims qui ne sont plus présents dans le cache.
			Database.execute("DELETE FROM "+Main.getConnexion().get("table")+" WHERE guild_id NOT IN ("+String.join(", ", "'"+claims+"'")+")");
		}
		else {
			//write the cache into claims.yml.
			//call the cache.
			HashMap<String, HashMap<String, Object>> cache = Main.getClaims();
			//charger l'objet de config de claims.yml
			File claimsFile = new File(Main.getPlugin().getDataFolder(), "claims.yml");
			FileConfiguration claimsConfig = new YamlConfiguration();
			try {
				claimsConfig.load(claimsFile);
	        } catch (IOException | InvalidConfigurationException e) {
	            e.printStackTrace();
	        }
			//parcourir le cache pour ajouter les valeurs au claims.yml
			Set<String> claims = cache.keySet();
			String server = Main.getPlugin().getConfig().getString("serverMarker");
			for(String guild_id : claims) {
				HashMap<String, Object> claim = cache.get(guild_id);
				claimsConfig.set(guild_id+".server", server);
				claimsConfig.set(guild_id+".world", claim.get("world"));
				claimsConfig.set(guild_id+".name", claim.get("name"));
				claimsConfig.set(guild_id+".x", claim.get("x"));
				claimsConfig.set(guild_id+".z", claim.get("z"));
				claimsConfig.set(guild_id+".size", claim.get("size"));
			}
			//retirer du claims.yml les claims qui ne sont plus présents dans le cache.
			Set<String> oldClaims = claimsConfig.getKeys(false);
			for(String oldGuild_id : oldClaims) {
				if(!claims.contains(oldGuild_id)) {
					claimsConfig.set(oldGuild_id, null);
				}
			}
		}
	}
}