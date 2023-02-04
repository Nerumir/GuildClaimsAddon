package me.nerumir.guildclaims;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class Utils {

	public static void reload() {
		
		Main.getPlugin().reloadConfig();
		//reload config cache.
		Main.setWorlds(Main.getPlugin().getConfig().getStringList("worlds"));
		Main.setBlacklist(Main.getPlugin().getConfig().getBoolean("blacklist"));
		Main.setServerMarker(Main.getPlugin().getConfig().getString("serverMarker"));
		HashMap<String, String> messages = new HashMap<>();
		messages.put("bypass-on", Main.getPlugin().getConfig().getString("messages.bypass-on"));
		messages.put("bypass-off", Main.getPlugin().getConfig().getString("messages.bypass-off"));
		messages.put("no-console", Main.getPlugin().getConfig().getString("messages.no-console"));
		messages.put("not-in-guild", Main.getPlugin().getConfig().getString("messages.not-in-guild"));
		messages.put("not-leader", Main.getPlugin().getConfig().getString("messages.not-leader"));
		messages.put("claim-exists", Main.getPlugin().getConfig().getString("messages.claim-exists"));
		messages.put("claim-success", Main.getPlugin().getConfig().getString("messages.claim-success"));
		messages.put("claim-overlap", Main.getPlugin().getConfig().getString("messages.claim-overlap"));
		messages.put("claim-not-exist", Main.getPlugin().getConfig().getString("messages.claim-not-exist"));
		messages.put("unclaim-success", Main.getPlugin().getConfig().getString("messages.unclaim-success"));
		messages.put("claim-not-on-server", Main.getPlugin().getConfig().getString("messages.claim-not-on-server"));
		messages.put("entering-claim", Main.getPlugin().getConfig().getString("messages.entering-claim"));
		messages.put("leaving-claim", Main.getPlugin().getConfig().getString("messages.leaving-claim"));
		Main.setMessages(messages);
		//cache the database connection infos
		FileConfiguration guildsConfig = Main.getGuilds().getGuildHandler().getGuildsPlugin().getConfig();
		HashMap<String, String> connexion = new HashMap<>();
		connexion.put("url", "jdbc:mysql:" + guildsConfig.getString("storage.sql.host") + ":"
				+ guildsConfig.getString("storage.sql.port") + "/" + guildsConfig.getString("storage.sql.database"));
		connexion.put("user", guildsConfig.getString("storage.sql.username"));
		connexion.put("password", guildsConfig.getString("storage.sql.password"));
		connexion.put("database", guildsConfig.getString("storage.sql.database"));
		connexion.put("table", guildsConfig.getString("storage.sql.table-prefix")+"claims");
		Main.setConnexion(connexion);
		//initialize file or database
		try {
			Data.initClaims();
		} catch (SQLException | IOException e1) {
			e1.printStackTrace();
		}
		//clean the expirated claims in file or database
		try {
			Data.cleanExpiratedClaims();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		//cache the claims from either claims.yml or mysql database
		try {
			Main.setClaims(Data.downloadCache());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
