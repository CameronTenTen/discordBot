import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**Object for managing the database connection. Provides various useful functions for database interaction. 
 * @author cameron
 * <p>
 * players table created using the command:
 *  CREATE TABLE players (kagname VARCHAR(20), discordid BIGINT, gamesplayed INT DEFAULT 0, wins INT DEFAULT 0, losses INT DEFAULT 0, draws INT DEFAULT 0, desertions INT DEFAULT 0, substitutions INT DEFAULT 0, desertionlosses INT DEFAULT 0, substitutionwins INT DEFAULT 0, UNIQUE KEY kagname (kagname), UNIQUE KEY discordid (discordid));
 * <p>
 * games table created using the command:
 * CREATE TABLE games (gameId INT UNSIGNED NOT NULL AUTO_INCREMENT, gameLengthSeconds INT, PRIMARY KEY (gameId));
 * <p>
 * playerGames table created using the command:
 * CREATE TABLE playerGames (gameId INT UNSIGNED NOT NULL, kagName VARCHAR(20), team TINYINT, won BOOL, UNIQUE KEY (gameId, kagName));
 * <p>
 * export players table to csv using
 * SELECT *, 2000+(wins*10)-(losses*10) FROM players INTO OUTFILE '/var/lib/mysql-files/players.csv' FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n';
 * <p>
 * make copy of players table using
 * CREATE TABLE newtable LIKE players; 
 * INSERT newtable SELECT * FROM players;
 * <p>
 * clear stats data from players table using
 * UPDATE players SET gamesplayed=0, wins=0, losses=0, draws=0, desertions=0, substitutions=0, desertionlosses=0, substitutionwins=0;
 */
public class GatherDB {

	private String username;
	private String password;
	private String url;
	private Connection connection = null;
	
	GatherDB(String user, String pass, String ip, String db)
	{
		setUsername(user);
		setPassword(pass);
		setUrl(ip, db);
		connect();
	}
	
	/**
	 * @return the username to use to connect to the database
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to use to connect to the database
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password to use to connect to the database
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to use to connect to the database
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the url to use to connect to the database
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to use to connect to the databse
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**Helper function for setting the connection url by joining the ip and database name together into a properly formatted string
	 * @param ip the ip address of the server
	 * @param database the name of the database to use on the server
	 */
	public void setUrl(String ip, String database) {
		this.url = "jdbc:mysql://" + ip + "/" + database;
	}

	/**
	 * Initiates the connection between the bot and the database. 
	 */
	public void connect()
	{
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		
                try {
                	connection = DriverManager.getConnection(url, username, password);
                	
                } catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
	}
	
	/**Takes a Discord id and returns the corresponding KAG username that is stored in the database. Returns a blank string if they were not found. 
	 * @param id the Discord id of the player to be found
	 * @return the KAG username as a string, or a blank string if no user was found
	 */
	public String getKagName(long id)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT * FROM players WHERE discordid = "+id);

	        	if (result.next())
	        	{
	        		return result.getString("kagname");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return "";
	}

	/**Takes a KAG username and returns the corresponding Discord id that is stored in the database. Returns -1 if they were not found. 
	 * @param kagName the KAG username of the player to be found
	 * @return the Discord id as a long, or -1 if no user was found. 
	 */
	public long getDiscordID(String kagName)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT * FROM players WHERE kagname = \"" + kagName + "\"");

	        	if (result.next())
	        	{
	        		return result.getLong("discordid");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}
	
	/**Gets the stats of a player from the database. Returns all the stats in a StatsObject. 
	 * @param kagname the KAG username of the player
	 * @return the StatsObject holding the players stats
	 * @see #StatsObject
	 */
	public StatsObject getStats(String kagname)
	{
		Statement statement = null;
		ResultSet result = null;
		StatsObject returnObj = new StatsObject();
		returnObj.kagname = kagname;
		try
		{
			statement = connection.createStatement();
			// result = statement.executeQuery("SELECT *, 2000+(wins*10)-(losses*10) FROM players WHERE kagname = \""+kagname + "\"");
			result = statement.executeQuery("SELECT * FROM players WHERE kagname = \""+kagname + "\"");

	        	if (result.next())
	        	{
	        		returnObj.discordid = result.getLong("discordid");
	        		returnObj.gamesplayed = result.getInt("gamesplayed");
	        		returnObj.wins = result.getInt("wins");
	        		returnObj.losses = result.getInt("losses");
	        		returnObj.draws = result.getInt("draws");
	        		returnObj.desertions = result.getInt("desertions");
	        		returnObj.substitutions = result.getInt("substitutions");
	        		returnObj.desertionlosses = result.getInt("desertionlosses");
	        		returnObj.substitutionwins = result.getInt("substitutionwins");
	        		// returnObj.mmr = result.getInt("2000+(wins*10)-(losses*10)");
	        		return returnObj;
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return null;
	}

	/**Gets the stats of a player from the database. Returns all the stats in a StatsObject. 
	 * @param id the Discord id of the player
	 * @return the StatsObject holding the players stats
	 * @see #StatsObject
	 */
	public StatsObject getStats(long id)
	{
		Statement statement = null;
		ResultSet result = null;
		StatsObject returnObj = new StatsObject();
		returnObj.discordid = id;
		try
		{
			statement = connection.createStatement();
			//result = statement.executeQuery("SELECT *, 2000+(wins*10)-(losses*10) FROM players WHERE discordid = "+id);
			result = statement.executeQuery("SELECT * FROM players WHERE discordid = "+id);

	        	if (result.next())
	        	{
	        		returnObj.kagname = result.getString("kagName");
	        		returnObj.gamesplayed = result.getInt("gamesplayed");
	        		returnObj.wins = result.getInt("wins");
	        		returnObj.losses = result.getInt("losses");
	        		returnObj.draws = result.getInt("draws");
	        		returnObj.desertions = result.getInt("desertions");
	        		returnObj.substitutions = result.getInt("substitutions");
	        		returnObj.desertionlosses = result.getInt("desertionlosses");
	        		returnObj.substitutionwins = result.getInt("substitutionwins");
	        		// returnObj.mmr = result.getInt("2000+(wins*10)-(losses*10)");
	        		return returnObj;
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return null;
	}

	/**Gets the number of games played by the player from the database. 
	 * @param kagname the KAG username of the player
	 * @return the number of games played by the player, -1 if the player couldnt be found.
	 */
	public int getGamesPlayed(String kagname)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT gamesPlayed FROM players WHERE kagname = \"" + kagname + "\"");

	        	if (result.next())
	        	{
	        		return result.getInt("gamesplayed");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Gets the number of games played by the player from the database. 
	 * @param id the Discord id of the player
	 * @return the number of games played by the player, -1 if the player couldnt be found.
	 */
	public int getGamesPlayed(long id)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT gamesplayed FROM players WHERE discordid = " + id);

	        	if (result.next())
	        	{
	        		return result.getInt("gamesplayed");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Gets the number of wins of the player from the database. 
	 * @param kagname the KAG username of the player
	 * @return the number of games won by the player, -1 if the player couldnt be found.
	 */
	public int getWins(String kagname)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT wins FROM players WHERE kagname = \"" + kagname + "\"");

	        	if (result.next())
	        	{
	        		return result.getInt("wins");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Gets the number of wins of the player from the database. 
	 * @param id the Discord id of the player
	 * @return the number of games won by the player, -1 if the player couldnt be found.
	 */
	public int getWins(long id)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT wins FROM players WHERE discordid = " + id);

	        	if (result.next())
	        	{
	        		return result.getInt("wins");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Gets the number of losses of the player from the database. 
	 * @param kagname the KAG username of the player
	 * @return the number of games lost by the player, -1 if the player couldnt be found.
	 */
	public int getLosses(String kagname)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT losses FROM players WHERE kagname = \"" + kagname + "\"");

	        	if (result.next())
	        	{
	        		return result.getInt("losses");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Gets the number of losses of the player from the database. 
	 * @param id the Discord id of the player
	 * @return the number of games lost by the player, -1 if the player couldnt be found.
	 */
	public int getLosses(long id)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT losses FROM players WHERE discordid = " + id);

	        	if (result.next())
	        	{
	        		return result.getInt("losses");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Gets the number of draws of the player from the database. 
	 * @param kagname the KAG username of the player
	 * @return the number of games drawn by the player, -1 if the player couldnt be found.
	 */
	public int getDraws(String kagname)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT draws FROM players WHERE kagname = \"" + kagname + "\"");

	        	if (result.next())
	        	{
	        		return result.getInt("draws");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Gets the number of draws of the player from the database. 
	 * @param id the Discord id of the player
	 * @return the number of games drawn by the player, -1 if the player couldnt be found.
	 */
	public int getDraws(long id)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT draws FROM players WHERE discordid = " + id);

	        	if (result.next())
	        	{
	        		return result.getInt("draws");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Gets the number of desertions of the player from the database. 
	 * @param kagname the KAG username of the player
	 * @return the number of games deserted by the player, -1 if the player couldnt be found.
	 */
	public int getdesertions(String kagname)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT desertions FROM players WHERE kagname = \"" + kagname + "\"");

	        	if (result.next())
	        	{
	        		return result.getInt("desertions");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Gets the number of desertions of the player from the database. 
	 * @param id the Discord id of the player
	 * @return the number of games deserted by the player, -1 if the player couldnt be found.
	 */
	public int getdesertions(long id)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT desertions FROM players WHERE discordid = " + id);

	        	if (result.next())
	        	{
	        		return result.getInt("desertions");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds a win to the player in the database. Also increments their games played. 
	 * @param id the Discord id of the player
	 * @return the number of rows changed by this request, -1 if the player couldnt be found.
	 */
	public int addWin(long id)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET wins=wins+1, gamesplayed=gamesplayed+1 WHERE discordid="+id);
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds a win to the player in the database. Also increments their games played. 
	 * @param id the KAG username of the player
	 * @return the number of rows changed by this request, -1 if the player couldnt be found.
	 */
	public int addWin(String kagName)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET wins=wins+1, gamesplayed=gamesplayed+1 WHERE kagname=\""+kagName+"\"");
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds a loss to the player in the database. Also increments their games played. 
	 * @param id the Discord id of the player
	 * @return the number of rows changed by this request, -1 if the player couldnt be found.
	 */
	public int addLoss(long id)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET losses=losses+1, gamesplayed=gamesplayed+1 WHERE discordid="+id);
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds a loss to the player in the database. Also increments their games played. 
	 * @param id the KAG username of the player
	 * @return the number of rows changed by this request, -1 if the player couldnt be found.
	 */
	public int addLoss(String kagName)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET losses=losses+1, gamesplayed=gamesplayed+1 WHERE kagname=\""+kagName+"\"");
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds a desertion to the player in the database. 
	 * @param id the Discord id of the player
	 * @return the number of rows changed by this request, -1 if the player couldnt be found.
	 */
	public int addDesertion(long id)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET desertions=desertions+1 WHERE discordid="+id);
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds a desertion to the player in the database. 
	 * @param id the KAG username of the player
	 * @return the number of rows changed by this request, -1 if the player couldnt be found.
	 */
	public int addDesertion(String kagName)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET desertions=desertions+1 WHERE kagname=\""+kagName+"\"");
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds a desertion loss to the player in the database. A desertion loss means that in a game where this player deserted, their team lost. 
	 * @param id the Discord id of the player
	 * @return the number of rows changed by this request, -1 if the player couldnt be found.
	 */
	public int addDesertionLoss(long id)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET desertions=desertions+1, desertionlosses=desertionlosses+1 WHERE discordid="+id);
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds a desertion loss to the player in the database. A desertion loss means that in a game where this player deserted, their team lost. 
	 * @param id the KAG username of the player
	 * @return the number of rows changed by this request, -1 if the player couldnt be found.
	 */
	public int addDesertionLoss(String kagName)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET desertions=desertions+1, desertionlosses=desertionlosses+1 WHERE kagname=\""+kagName+"\"");
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds a substitution to the player in the database. 
	 * @param id the Discord id of the player
	 * @return the number of rows changed by this request, -1 if the player couldnt be found.
	 */
	public int addSubstitution(long id)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET substitutions=substitutions+1 WHERE discordid="+id);
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds a substitution to the player in the database. 
	 * @param id the KAG username of the player
	 * @return the number of rows changed by this request, -1 if the player couldnt be found.
	 */
	public int addSubstitution(String kagName)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET substitutions=substitutions+1 WHERE kagname=\""+kagName+"\"");
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds a substitution win to the player in the database. A substitution win means that in a game where this player subbed in, their team won. 
	 * @param id the Discord id of the player
	 * @return the number of rows changed by this request, -1 if the player couldnt be found.
	 */
	public int addSubstitutionWin(long id)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET substitutions=substitutions+1, substitutionwins=substitutionwins+1 WHERE discordid="+id);
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds a substitution win to the player in the database. A substitution win means that in a game where this player subbed in, their team won. 
	 * @param kagName the KAG username of the player
	 * @return the number of rows changed by this request, -1 if the player couldnt be found.
	 */
	public int addSubstitutionWin(String kagName)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET substitutions=substitutions+1, substitutionwins=substitutionwins+1 WHERE kagname=\""+kagName+"\"");
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}
	
	/**Links a KAG username and a Discord id in the database. If one of the two already exists in the database, the entry should be updated to the new values. If both already exist it is likely to return an error(not properly tested since its an unlikely case). 
	 * @param kagName the KAG username to link
	 * @param id the Discord id to link
	 * @return the number of rows changed by the request, -1 if the user couldnt be found. 
	 */
	public int linkAccounts(String kagName, long id)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("INSERT INTO players (kagname, discordid) VALUES(\""+kagName+"\","+id+") ON DUPLICATE KEY UPDATE kagname=\""+kagName+"\", discordid = "+id);
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                	//if there is a local player object update it
			DiscordBot.players.update(id);
                }
		return -1;
	}
	
	/**Increments the total number of gather games played. 
	 * @return the number of rows changed by the requeset, -1 if something went wrong. 
	 */
	public int incrementGamesPlayed()
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET gamesplayed=gamesplayed+1 WHERE kagname=\"+numgames+\"");
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}
	
	/**(not yet implemented) Gets a unique game id from the database and reserves the id for future use (so that it is not returned by this function again). 
	 * @return the game id that has been reserved
	 */
	public int reserveGameId() {
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO games () VALUES ()");
			result = statement.executeQuery("SELECT LAST_INSERT_ID();");

	        	if (result.next())
	        	{
	        		return result.getInt("LAST_INSERT_ID()");
	        	}
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}

	/**Adds the data of a game to the database. 
	 * @param game the GatherGame object representing this game. 
	 * @return the number of rows changed by the request, -1 if something went wrong. 
	 */
	public int addGame(GatherGame game)
	{
		Statement statement = null;
		int rowsChanged=-1;
		try
		{
			statement = connection.createStatement();
			//game into games table
			rowsChanged = statement.executeUpdate("INSERT INTO games (gameId, gameLengthSeconds) VALUES ("+game.getGameID()+","+game.getGameLengthSeconds()+") ON DUPLICATE KEY UPDATE gameLengthSeconds="+game.getGameLengthSeconds());
			//players into playergames table
			//TODO make an object for storing more stats related to what the player did in the game, we would then get a list of these objects instead of strings here
			//TODO appropriate stats for subs
			List<String> blueTeam = game.getBlueKagNames();
			List<String> redTeam = game.getRedKagNames();
			for(String kagName : blueTeam) {
				boolean blueWon = game.getWinningTeam()==0 ? true : false;
				rowsChanged += statement.executeUpdate("INSERT INTO playerGames (gameId, kagName, team, won) VALUES ("+game.getGameID()+", \""+kagName+"\", 0, "+blueWon+")");
			}
			for(String kagName : redTeam) {
				boolean redWon = game.getWinningTeam()==1 ? true : false;
				rowsChanged += statement.executeUpdate("INSERT INTO playerGames (gameId, kagName, team, won) VALUES ("+game.getGameID()+", \""+kagName+"\", 1, "+redWon+")");
			}
			
			return rowsChanged;
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return -1;
	}
	
	/**Returns a list of players ordered based on their rank, followed by win percentage, then games played. Players with less than 10 games are ignored. 
	 * @param numPlayers the number of players to get
	 * @return a list of StatsObject that has a length of numPlayers or less 
	 */
	public List<StatsObject> getTopPlayers(int numPlayers)
	{
		Statement statement = null;
		ResultSet result = null;
		try
		{
			statement = connection.createStatement();
			//result = statement.executeQuery("(SELECT *, ((wins+substitutionwins)/(gamesplayed+desertionlosses+substitutionwins))*100, 2000+(wins*10)-(losses*10) FROM players WHERE gamesplayed>=10 AND kagname<>\"+numgames+\" ORDER BY ((wins+substitutionwins)/(gamesplayed+desertionlosses+substitutionwins))*100 DESC LIMIT "+numPlayers+")"
			//result = statement.executeQuery("(SELECT *, ((wins+substitutionwins)/(gamesplayed+desertionlosses+substitutionwins))*100, 2000+(wins*10)-(losses*10) FROM players WHERE gamesplayed>=10 AND kagname<>\"+numgames+\" ORDER BY 2000+(wins*10)-(losses*10) DESC, ((wins+substitutionwins)/(gamesplayed+desertionlosses+substitutionwins))*100 DESC, gamesplayed DESC LIMIT "+numPlayers+")"
			result = statement.executeQuery("(SELECT *, ((wins+substitutionwins)/(gamesplayed+desertionlosses+substitutionwins))*100 AS winrate, ((1*(wins-(desertions/2))/gamesplayed)+1.96*1.96/(2*gamesplayed)-1.96*SQRT(((1*(wins-(desertions/2))/gamesplayed)*(1-(1*(wins-(desertions/2))/gamesplayed))+1.96*1.96/(4*gamesplayed))/gamesplayed))/(1+1.96*1.96/gamesplayed) AS mmr FROM players WHERE gamesplayed>(SELECT gamesplayed FROM players WHERE kagname='+numgames+')*0.1 AND kagname<>\"+numgames+\" ORDER BY mmr DESC, winrate DESC, gamesplayed DESC, kagname ASC LIMIT "+numPlayers+")"
			                             /*+ " UNION ALL "
			                              + "(SELECT *, (wins/(wins+losses+desertions))*100 FROM players WHERE gamesplayed<10 AND kagname<>\"+numgames+\" ORDER BY gamesplayed DESC)"*/);

			List<StatsObject> returnList = new ArrayList<StatsObject>();
	        	while (result.next())
	        	{
	        		StatsObject returnObj = new StatsObject();
	        		returnObj.kagname = result.getString("kagname");
	        		returnObj.discordid = result.getLong("discordid");
	        		returnObj.gamesplayed = result.getInt("gamesplayed");
	        		returnObj.wins = result.getInt("wins");
	        		returnObj.losses = result.getInt("losses");
	        		returnObj.draws = result.getInt("draws");
	        		returnObj.desertions = result.getInt("desertions");
	        		returnObj.substitutions = result.getInt("substitutions");
	        		returnObj.winRate = result.getFloat("winrate");
	        		// returnObj.mmr = result.getInt("2000+(wins*10)-(losses*10)");
	        		returnList.add(returnObj);
	        	}
        		return returnList;
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
        	finally
                {
                	if(result != null)
                	{
                		try {
                			result.close();
                		} catch (SQLException e) {
                		}
                	}
                	if(statement != null)
                	{
                		try {
                			statement.close();
                		} catch (SQLException e) {
                		}
                	}
                }
		return null;
	}
	
}
