package me.nerumir.guildclaims;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public abstract class Database {
	
	static boolean tableExistsSQL(String tableName) throws SQLException {
		Connection conn = null;
		try
	    {
			Class.forName("com.mysql.cj.jdbc.Driver");
			HashMap<String, String> ids = Main.getConnexion();
			conn = DriverManager.getConnection(ids.get("url")+"?serverTimezone=UTC", ids.get("user"), ids.get("password"));
		    PreparedStatement preparedStatement = conn.prepareStatement("SELECT count(*) "
		      + "FROM information_schema.tables "
		      + "WHERE table_name = ?"
		      + "LIMIT 1;");
		    preparedStatement.setString(1, tableName);
		    ResultSet resultSet = preparedStatement.executeQuery();
		    resultSet.next();
		    return resultSet.getInt(1) != 0;
	    }
	    catch(Exception e){ 
	      System.out.println(e);
	      return false;
	    }
	}
	
	public static int execute(String query) {
		
		Connection conn = null;
		Statement stmt = null;
		int feedback = 0;
		
		try
	    {
	      //étape 1: charger la classe driver
	      Class.forName("com.mysql.cj.jdbc.Driver");
	      //étape 2: créer l'objet de connexion
	      HashMap<String, String> ids = Main.getConnexion();
	      conn = DriverManager.getConnection(ids.get("url")+"?serverTimezone=UTC", ids.get("user"), ids.get("password"));
	      //étape 3: créer l'objet statement
	      stmt = conn.createStatement();
	      //étape 4: exécuter la requéte
	      feedback = stmt.executeUpdate(query);
			conn.close();
			stmt.close();
	    }
	    catch(Exception e){ 
	      System.out.println(e);
	    }
		
		return feedback;
	}
	
	public static ResultSet select(String query) {
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet res = null;
		
		try
	    {
	      //étape 1: charger la classe driver
	      Class.forName("com.mysql.cj.jdbc.Driver");
	      //étape 2: créer l'objet de connexion
	      HashMap<String, String> ids = Main.getConnexion();
	      conn = DriverManager.getConnection(ids.get("url")+"?serverTimezone=UTC", ids.get("user"), ids.get("password"));
	      //étape 3: créer l'objet statement
	      stmt = conn.createStatement();
	      //étape 4: exécuter la requéte
	      res = stmt.executeQuery(query);
	    }
	    catch(Exception e){ 
	      System.out.println(e);
	    }
		
		return res;
	}

}
