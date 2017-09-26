import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setUrl(String ip, String database) {
		this.url = "jdbc:mysql://" + ip + "/" + database;
	}

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
                	e.printStackTrace();
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
	}
	
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
	
	public StatsObject getStats(String kagname)
	{
		Statement statement = null;
		ResultSet result = null;
		StatsObject returnObj = new StatsObject();
		returnObj.kagname = kagname;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT * FROM players WHERE kagname = \""+kagname + "\"");

	        	if (result.next())
	        	{
	        		returnObj.discordid = result.getLong("discordid");
	        		returnObj.gamesPlayed = result.getInt("gamesplayed");
	        		returnObj.wins = result.getInt("wins");
	        		returnObj.losses = result.getInt("losses");
	        		returnObj.draws = result.getInt("draws");
	        		returnObj.desertions = result.getInt("desertions");
	        		returnObj.substitutions = result.getInt("substitutions");
	        		return returnObj;
	        	}
		}
		catch (SQLException e)
		{
			    System.out.println("SQLException: " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
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
	
	public StatsObject getStats(long id)
	{
		Statement statement = null;
		ResultSet result = null;
		StatsObject returnObj = new StatsObject();
		returnObj.discordid = id;
		try
		{
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT * FROM players WHERE discordid = "+id);

	        	if (result.next())
	        	{
	        		returnObj.kagname = result.getString("kagName");
	        		returnObj.gamesPlayed = result.getInt("gamesplayed");
	        		returnObj.wins = result.getInt("wins");
	        		returnObj.losses = result.getInt("losses");
	        		returnObj.draws = result.getInt("draws");
	        		returnObj.desertions = result.getInt("desertions");
	        		returnObj.substitutions = result.getInt("substitutions");
	        		return returnObj;
	        	}
		}
		catch (SQLException e)
		{
			    System.out.println("SQLException: " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
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
	
	public int addDesertion(long id)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET desertions=desertions+1, gamesplayed=gamesplayed+1 WHERE discordid="+id);
		}
		catch (SQLException e)
		{
			    System.out.println("SQLException: " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
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
	
	public int addDesertion(String kagName)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET desertions=desertions+1, gamesplayed=gamesplayed+1 WHERE kagname=\""+kagName+"\"");
		}
		catch (SQLException e)
		{
			    System.out.println("SQLException: " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
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
	
	public int addSubstitution(long id)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET substitutions=substitutions+1, gamesplayed=gamesplayed+1 WHERE discordid="+id);
		}
		catch (SQLException e)
		{
			    System.out.println("SQLException: " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
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
	
	public int addSubstitution(String kagName)
	{
		Statement statement = null;
		try
		{
			statement = connection.createStatement();
			return statement.executeUpdate("UPDATE players SET substitutions=substitutions+1, gamesplayed=gamesplayed+1 WHERE kagname=\""+kagName+"\"");
		}
		catch (SQLException e)
		{
			    System.out.println("SQLException: " + e.getMessage());
			    System.out.println("SQLState: " + e.getSQLState());
			    System.out.println("VendorError: " + e.getErrorCode());
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
	
}
