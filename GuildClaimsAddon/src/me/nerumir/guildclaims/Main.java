package me.nerumir.guildclaims;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import me.nerumir.guildclaims.cmds.Bypass;
import me.nerumir.guildclaims.listeners.Commands;
import me.nerumir.guildclaims.listeners.Farewell;
import me.nerumir.guildclaims.listeners.GuildsEvents;
import me.nerumir.guildclaims.listeners.Protection;

public class Main extends JavaPlugin implements Listener {
	
	private static JavaPlugin plugin;
	private static GuildsAPI guilds;
	private static HashMap<String, HashMap<String, Object>> cache = new HashMap<>();
	private static HashMap<String, String> connexion = new HashMap<>();
	private static Boolean blacklist;
	private static Boolean explosions;
	private static Boolean creeperEggs;
	private static List<String> worlds;
	private static String server;
	private static List<UUID> bypassPlayers;
	private static HashMap<String, String> messages = new HashMap<>();
	
	public static List<UUID> getBypassPlayers() {
		
		return bypassPlayers;
	}
	
	public static String getServerMarker() {
		
		return server;
	}
	
	public static Boolean getBlacklist() {
		
		return blacklist;
	}

	public static Boolean getExplosions() {
		
		return explosions;
	}

	public static void setExplosions(Boolean exp) {
		
		explosions = exp;
	}

	public static Boolean getCreeperEggs() {
		
		return creeperEggs;
	}

	public static void setCreeperEggs(Boolean creep) {
		
		creeperEggs = creep;
	}
	
	public static List<String> getWorlds() {
		
		return worlds;
	}
	
	public static void setServerMarker(String marker) {
		
		server = marker;
	}
	
	public static void setBlacklist(Boolean list) {
		
		blacklist = list;
	}
	
	public static void setWorlds(List<String> worldsList) {
		
		worlds = worldsList;
	}
		
	public static JavaPlugin getPlugin() {
		
		return plugin;
	}
	
	public static GuildsAPI getGuilds() {
		
		return guilds;
	}
	
	public static HashMap<String, HashMap<String, Object>> getClaims() {
		
		return cache;
	}
	
	public static void setClaims(HashMap<String, HashMap<String, Object>> claims) {
		
		cache = claims;
	}
	
	public static HashMap<String, String> getConnexion() {
		
		return connexion;
	}
	
	public static void setConnexion(HashMap<String, String> conn) {
		
		connexion = conn;
	}
	
	public static HashMap<String, String> getMessages() {
		
		return messages;
	}
	
	public static void setMessages(HashMap<String, String> mess) {
		
		messages = mess;
	}
	public static void setBypassPlayers(List<UUID> players) {
		
		bypassPlayers = players;
	}
		
	@Override
	public void onEnable() {
		
		plugin = this;
		bypassPlayers = new ArrayList<>();
		guilds = Guilds.getApi();
		//Reload config.yml
		this.saveDefaultConfig();
		
		//load some config cache.
		worlds = this.getConfig().getStringList("worlds");
		blacklist = this.getConfig().getBoolean("blacklist");
		server = this.getConfig().getString("serverMarker");
		explosions = this.getConfig().getBoolean("explosions-on-claims");
		creeperEggs = this.getConfig().getBoolean("creeper-eggs");
		messages.put("bypass-on", this.getConfig().getString("messages.bypass-on"));
		messages.put("bypass-off", this.getConfig().getString("messages.bypass-off"));
		messages.put("no-console", this.getConfig().getString("messages.no-console"));
		messages.put("not-in-guild", this.getConfig().getString("messages.not-in-guild"));
		messages.put("not-leader", this.getConfig().getString("messages.not-leader"));
		messages.put("claim-exists", this.getConfig().getString("messages.claim-exists"));
		messages.put("claim-success", this.getConfig().getString("messages.claim-success"));
		messages.put("claim-overlap", this.getConfig().getString("messages.claim-overlap"));
		messages.put("claim-not-exist", this.getConfig().getString("messages.claim-not-exist"));
		messages.put("unclaim-success", this.getConfig().getString("messages.unclaim-success"));
		messages.put("claim-not-on-server", this.getConfig().getString("messages.claim-not-on-server"));
		messages.put("entering-claim", this.getConfig().getString("messages.entering-claim"));
		messages.put("leaving-claim", this.getConfig().getString("messages.leaving-claim"));
		messages.put("not-in-valid-world", this.getConfig().getString("messages.not-in-valid-world"));
		messages.put("cannot-do-that", this.getConfig().getString("messages.cannot-do-that"));
		
		//cache the database connection infos
		FileConfiguration guildsConfig = guilds.getGuildHandler().getGuildsPlugin().getConfig();
		
		connexion.put("url", "jdbc:mysql://" + guildsConfig.getString("storage.sql.host") + ":"
				+ guildsConfig.getString("storage.sql.port") + "/" + guildsConfig.getString("storage.sql.database"));
		connexion.put("user", guildsConfig.getString("storage.sql.username"));
		connexion.put("password", guildsConfig.getString("storage.sql.password"));
		connexion.put("database", guildsConfig.getString("storage.sql.database"));
		connexion.put("table", guildsConfig.getString("storage.sql.table-prefix")+"claims");
		//initialize file or database
		try {
			Data.initClaims();
		} catch (SQLException | IOException e1) {
			e1.printStackTrace();
		}
		//cache the claims from either claims.yml or mysql database
		try {
			cache = Data.downloadCache();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//registering commands and events
		this.getCommand("cbp").setExecutor(new Bypass());
		this.getServer().getPluginManager().registerEvents(new Commands(), this);
		this.getServer().getPluginManager().registerEvents(new GuildsEvents(), this);
		this.getServer().getPluginManager().registerEvents(new Farewell(), this);
		this.getServer().getPluginManager().registerEvents(new Protection(), this);
	}
	
	@Override
	public void onDisable() {
		
		//restart
		//stop
		//plugin reload
		//reload
	}

}