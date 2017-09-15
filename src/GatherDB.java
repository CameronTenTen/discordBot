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
	
	public String getKagNameFromDiscordID(long id)
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
	
	public long getDiscordIDFromKagName(String kagName)
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
}
