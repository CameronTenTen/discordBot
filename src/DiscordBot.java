import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.gson.Gson;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.Discord4JHandler;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuilder;

/**The main bot class. Contains most of the interaction with the Discord bot client. Contains various helper functions and objects.
 * @author cameron
 *
 */
public class DiscordBot {

	/**
	 * The instance of the Discord4J client
	 */
	public static IDiscordClient client;
	/**
	 * This builder object is used for making rate limit safe discord requests
	 */
	public RequestBuilder builder;
	
	public static DiscordBot bot;
	
	/**The database manipulation object
	 * @see GatherDB
	 */
	public static GatherDB database;
	/**The player object tracker
	 * @see PlayerObjectManager
	 */
	public static PlayerObjectManager players;

	/**Set of gather objects. Each gather object represents one gather queue, and one command channel. 
	 * @see GatherObject
	 */
	public static Set<GatherObject> gatherObjects;

	public boolean doKagServerConnections = true;

	/**Helper function for getting the correct gather object, helpful for when a player uses a command. 
	 * @param channel - the channel being used
	 * @return The GatherObject associated with the channel, or null if there is no object for this channel
	 * @see GatherObject
	 */
	public static GatherObject getGatherObjectForChannel(IChannel channel) {
		if(channel==null) return null;
		for (GatherObject object : gatherObjects) {
			if (object.getCommandChannel().equals(channel))
				return object;
		}
		return null;
	}

	/**Helper function for getting the correct gather objects for a guild, used when a user leaves a guild.
	 * @param guild - the channel being used
	 * @return A list of GatherObjects associated with the channel, list is empty if there are no objects for this channel
	 * @see GatherObject
	 */
	public static List<GatherObject> getGatherObjectsForGuild(IGuild guild) {
		if(guild==null) return null;
		List<GatherObject> returnList = new ArrayList<GatherObject>();
		for (GatherObject object : gatherObjects) {
			if (object.getGuild().equals(guild))
				returnList.add(object);
		}
		return returnList;
	}
	
	/**Helper function for getting the correct gather object when data or commands are sent from a connected KAG server. 
	 * @param ip - the ip address of the server
	 * @param port - the port of the server
	 * @return The GatherObject this server is attached to, or null if the server couldnt be found
	 * @see GatherObject
	 */
	public static GatherObject getGatherObjectForServer(String ip, int port)
	{
		for(GatherObject obj : gatherObjects)
		{
			GatherServer server = obj.getServer(ip, port);
			if(server!=null)
			{
				return obj;
			}
		}
		return null;
	}
	
	/**Wrapper function for {@link #getGatherObjectForServer(String, int)}
	 * @param game - the GatherGame object
	 * @return the GatherObject that contains this game
	 * @see GatherGame
	 * @see GatherObject
	 */
	public static GatherObject getGatherObjectForGame(GatherGame game)
	{
		return DiscordBot.getGatherObjectForServer(game.getServerIp(), game.getServerPort());
	}

	/**Initial bot setup goes here. Some things that require the bot to be setup fully to work are done in a ReadyEventListener. 
	 * <p>
	 * Some things that are done here include instantiation of the client and request builder, client login, registering event listeners, registering command listeners. 
	 * @param token - the bot login token to be used for authentication
	 */
	public void startBot(String token) {
		ClientBuilder builder = new ClientBuilder(); // Creates a new client builder instance

		builder.withToken(token); // Sets the bot token for the client

		try {
			client = builder.login(); // Builds the IDiscordClient instance and logs it in
			System.out.println("logged in");
		} catch (DiscordException e) { // Error occurred logging in
			System.err.println("Error occurred while logging in with token: " + token);
			e.printStackTrace();
			return;
		}
		this.builder= new RequestBuilder(client);
		this.builder.shouldBufferRequests(true);

		// event listeners
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new ReadyEventListener());
		dispatcher.registerListener(new PresenceEventListener());

		//TODO load the queue object
		// might want to do this in ready listener
		//TODO delete the saved object so we don't load it next time

		// command listeners
		CommandHandler cmdHandler = new Discord4JHandler(client);

		// add all the commands
		cmdHandler.registerCommand(new CommandPing());
		cmdHandler.registerCommand(new CommandAdd());
		cmdHandler.registerCommand(new CommandRem());
		cmdHandler.registerCommand(new CommandList());
		cmdHandler.registerCommand(new CommandClearQueue());
		cmdHandler.registerCommand(new CommandForceRem());
		cmdHandler.registerCommand(new CommandEnd());
		cmdHandler.registerCommand(new CommandClearGames());
		cmdHandler.registerCommand(new CommandSetQueue());
		cmdHandler.registerCommand(new CommandEndGame());
		cmdHandler.registerCommand(new CommandPlayers());
		cmdHandler.registerCommand(new CommandReconnect());
		cmdHandler.registerCommand(new CommandRsub());
		cmdHandler.registerCommand(new CommandSub());
		cmdHandler.registerCommand(new CommandStart());
		cmdHandler.registerCommand(new CommandClearSubs());
		cmdHandler.registerCommand(new CommandSubs());
		cmdHandler.registerCommand(new CommandForceSub());
		cmdHandler.registerCommand(new CommandLink());
		cmdHandler.registerCommand(new CommandLinkServer());
		cmdHandler.registerCommand(new CommandLinkHelp());
		//cmdHandler.registerCommand(new CommandStats());
		cmdHandler.registerCommand(new CommandPlayerInfo());
		cmdHandler.registerCommand(new CommandRefreshServers());
		cmdHandler.registerCommand(new CommandScramble());
		cmdHandler.registerCommand(new CommandStatus());
		cmdHandler.registerCommand(new CommandPingMe());
		cmdHandler.registerCommand(new CommandRandomTeams());
		cmdHandler.registerCommand(new CommandDisconnect());
		cmdHandler.registerCommand(new CommandCancelGame());
		cmdHandler.registerCommand(new CommandConnect());
		cmdHandler.registerCommand(new CommandCachedPlayerInfo());
	}

	/**Main, instantiates some things, loads the database properties, sets up the database and player object managers
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		bot = new DiscordBot();
		gatherObjects = new HashSet<GatherObject>();
		
		//load database properties
		Properties props = new Properties();
		FileInputStream input = new FileInputStream("database.properties");
		props.load(input);
		String user = props.getProperty("username");
		String pass = props.getProperty("password");
		String id = props.getProperty("ipaddress");
		String db = props.getProperty("database");
		input.close();
		
		//connect to database
		database = new GatherDB(user, pass, id, db);
		database.connect();
		
		players = new PlayerObjectManager();
		
		if(args.length<1)
		{
			System.err.println("ERROR: no command line arguments detected - you must specify the bot token as an argument");
			return;
		}
		bot.startBot(args[0]);
		
		linkRequests = new ArrayList<PlayerObject>();
	}

	//wrappers for doing things in order to avoid rate limit exceptions

	/**Wrapper function for sending messages without getting rate limit exceptions and with tts defaulted to false.
	 * @param channel - the channel to put the message in
	 * @param msg - the message to send
	 * @return the message object after the message has been created, usually only want this if we are going to edit the message. 
	 * @see #sendMessage(IChannel, String, boolean)
	 * @see https://discord4j.readthedocs.io/en/latest/RateLimitExceptions-and-you/
	 */
	public static IMessage sendMessage(IChannel channel, String msg)
	{
		return sendMessage(channel, msg, false);
	}
	
	/**Wrapper function for sending messages without getting rate limit exceptions. 
	 * @param channel - the channel to put the message in
	 * @param msg - the message to send
	 * @param tts - should use text to speech
	 * @return the message object after the message has been created, usually only want this if we are going to edit the message. 
	 * @see Discord4J: {@link IChannel#sendMessage(String, boolean)}
	 * @see https://discord4j.readthedocs.io/en/latest/RateLimitExceptions-and-you/
	 * @see Discord4J: {@link RequestBuffer#request(sx.blah.discord.util.RequestBuffer.IRequest)}
	 */
	public static IMessage sendMessage(IChannel channel, String msg, boolean tts)
	{
		return RequestBuffer.request(() -> {
			try
			{
				return channel.sendMessage(msg, tts);
			}
			catch(NullPointerException e)
			{
				//this happened once???
				Discord4J.LOGGER.warn("Null pointer exception caught from Discord4J code: " + e.getMessage());
			}
			return null;
		}).get();
	}
	
	/**Wrapper for editing messages without getting rate limit exceptions. 
	 * @param msg - the message to be edited
	 * @param newString - the new version of the message
	 * @see Discord4J: {@link IMessage#edit(String)}
	 * @see https://discord4j.readthedocs.io/en/latest/RateLimitExceptions-and-you/
	 * @see Discord4J: {@link RequestBuffer#request(sx.blah.discord.util.RequestBuffer.IRequest)}
	 */
	public static void editMessage(IMessage msg, String newString)
	{
		try
		{
			RequestBuffer.request(() -> {
				msg.edit(newString);
			});
		}
		catch (DiscordException e)
		{
			Discord4J.LOGGER.warn("DiscordException caught from Discord4J code when trying to edit a message: " + e.getMessage());
		}
	}
	
	/**Wrapper for replying to messages without getting rate limit exceptions. 
	 * @param msg - the message to reply to
	 * @param reply - the reply message
	 * @see Discord4J: {@link IMessage#reply(String)}
	 * @see https://discord4j.readthedocs.io/en/latest/RateLimitExceptions-and-you/
	 * @see Discord4J: {@link RequestBuffer#request(sx.blah.discord.util.RequestBuffer.IRequest)}
	 */
	public static void reply(IMessage msg, String reply)
	{
		RequestBuffer.request(() -> {
			msg.reply(reply);
		});
	}
	
	/**Wrapper for deleting messages without getting rate limit exceptions.
	 * @param msg - the message to delete
	 * @see Discord4J: {@link IMessage#delete()}
	 * @see https://discord4j.readthedocs.io/en/latest/RateLimitExceptions-and-you/
	 * @see Discord4J: {@link RequestBuffer#request(sx.blah.discord.util.RequestBuffer.IRequest)}
	 */
	public static void delete(IMessage msg)
	{
		RequestBuffer.request(() -> {
			msg.delete();
		});
	}
	
	/**Wrapper for adding roles to a user without getting rate limit exceptions. 
	 * @param user - the user to be changed
	 * @param role - the role to give them
	 * @see Discord4J: {@link IUser#addRole(IRole)}
	 * @see https://discord4j.readthedocs.io/en/latest/RateLimitExceptions-and-you/
	 * @see Discord4J: {@link RequestBuffer#request(sx.blah.discord.util.RequestBuffer.IRequest)}
	 */
	public static void addRole(IUser user, IRole role)
	{
		if(user == null || role == null) return;
		RequestBuffer.request(() -> {
			user.addRole(role);
		});
	}
	
	/**Wrapper for removing roles from a user without getting rate limit exceptions. 
	 * @param user - the user to be changed
	 * @param role - the role to take away
	 * @see Discord4J: {@link IUser#removeRole(IRole)}
	 * @see https://discord4j.readthedocs.io/en/latest/RateLimitExceptions-and-you/
	 * @see Discord4J: {@link RequestBuffer#request(sx.blah.discord.util.RequestBuffer.IRequest)}
	 */
	public static void removeRole(IUser user, IRole role)
	{
		if(user == null || role == null) return;
		RequestBuffer.request(() -> {
			user.removeRole(role);
		});
	}
	
	/**Wrapper for removing a role from a user without getting rate limit exceptions. 
	 * Checks the user acutally has the role before removing it. 
	 * @param user - the user to be changed
	 * @param role - the role to take away
	 * @see Discord4J: {@link IUser#removeRole(IRole)}
	 * @see Discord4J: {@link IUser#getRolesForGuild(IGuild)}
	 * @see https://discord4j.readthedocs.io/en/latest/RateLimitExceptions-and-you/
	 * @see Discord4J: {@link RequestBuffer#request(sx.blah.discord.util.RequestBuffer.IRequest)}
	 */
	public static void removeRoleIfPresent(IUser user, IRole role)
	{
		if(user == null || role == null) return;
		List<IRole> roles = RequestBuffer.request(() -> {
			return user.getRolesForGuild(role.getGuild());
		}).get();
		if(roles.contains(role))
		{
			removeRole(user, role);
		}
	}
	
	/**Wrapper function for creating a role on a guild. The role object should then be manipulated as needed. 
	 * @param guild the guild for the role
	 * @return IRole object representing the new role
	 */
	public static IRole createRole(IGuild guild)
	{
		return RequestBuffer.request(() -> {
			return guild.createRole();
		}).get();
	}
	
	/**Wrapper for deleting a role from a guild without getting rate limit exceptions.
	 * @param role the role object to delete
	 */
	public static void deleteRole(IRole role)
	{
		if(role==null) return;
		RequestBuffer.request(() -> {
			role.delete();
		});
	}
	
	/**Wrapper for getting a list of the roles of a guild sorted by their effective positions without getting rate limit exceptions. 
	 * @param guild the guild to get the roles from
	 * @return List<IRole> the list of roles for the guild
	 */
	public static List<IRole> getRoles(IGuild guild)
	{
		return RequestBuffer.request(() -> {
			return guild.getRoles();
		}).get();
	}
	
	public static void reorderRoles(IGuild guild, List<IRole> newOrder)
	{
		RequestBuffer.request(() -> {
			guild.reorderRoles(newOrder.toArray(new IRole[] {}));
		}).get();
	}
	
	/**Wrapper for moving a user to a voice channel without getting rate limit exceptions. User must already be in a voice channel to allow moving them. 
	 * @param user - the user to be moved
	 * @param channel - the channel to move them to
	 * @see Discord4J: {@link IUser#moveToVoiceChannel(IVoiceChannel)}
	 * @see https://discord4j.readthedocs.io/en/latest/RateLimitExceptions-and-you/
	 * @see Discord4J: {@link RequestBuffer#request(sx.blah.discord.util.RequestBuffer.IRequest)}
	 */
	public static void moveToVoiceChannel(IUser user, IVoiceChannel channel)
	{
		RequestBuffer.request(() -> {
			try
			{
				user.moveToVoiceChannel(channel);
			}
			catch (DiscordException e)
			{
				//this can happen if the room doesnt exist or the user isnt already in a channel?
				//or does it just happen if the bot doesnt have the permissions? I dont remember
				Discord4J.LOGGER.warn(e.getMessage());
			}
		});
	}

	/**Wrapper for getting/creating a users private message channel without getting rate limit exceptions. Used for sending messages directly to a user. 
	 * @param user the user you want to message
	 * @return the user's private channel
	 */
	public static IPrivateChannel getPMChannel(IUser user)
	{
		return RequestBuffer.request(() -> {
			return user.getOrCreatePMChannel();
		}).get();
	}

	/**Wrapper for setting the bot "playing" text without getting rate limit exceptions. Playing text is global for all guilds the bot is in. 
	 * @param newText - the new playing text to use
	 * @see Discord4J: {@link IDiscordClient#changePlayingText(String)}
	 * @see https://discord4j.readthedocs.io/en/latest/RateLimitExceptions-and-you/
	 * @see Discord4J: {@link RequestBuffer#request(sx.blah.discord.util.RequestBuffer.IRequest)}
	 */
	public static void setPlayingText(String newText) {
		RequestBuffer.request(() -> {
			//TODO: make this not reset online and playing - I don't know if there is a way to get the current status/activity type
			client.changePresence(StatusType.ONLINE, ActivityType.PLAYING, newText);
		});
	}

	/**Wrapper for setting the title of a text channel without getting rate limit exceptions. Does some basic parsing of the input text to remove spaces, but prints an error for other invalid characters. 
	 * <p>
	 * Discord only allows alphanumeric characters, dashes, and underscores in text channel names. 
	 * @param guild the guild containing the channel
	 * @param channel the channel to be changed
	 * @param newText the new channel name
	 */
	public static void setChannelCaption(IGuild guild, IChannel channel, String newText) {
		// remove spaces from the string (spaces not allowed)
		// TODO: remove other illegal characters instead of just catching the exception
		newText.replaceAll("\\s", "");
		try {
			RequestBuffer.request(() -> {
				channel.changeName(newText);
			});
		} catch (IllegalArgumentException e) {
			Discord4J.LOGGER.error("Error renaming channel: " + e.getMessage());
		} catch (MissingPermissionsException e) {
			Discord4J.LOGGER.error("Error renaming channel: " + e.getMessage());
		}
	}

	/**Wrapper for fetching a Discord user without getting rate limit exceptions. Used for fetching multiple users at once.
	 * @param id of user to be fetched
	 * @return Discord user
	 */
	public static IUser fetchUser(long id) {
		return RequestBuffer.request(() -> {
			return client.fetchUser(id);
		}).get();
	}

	/**Does the things needed when a player disconnects. As of writing this it only removes them from any queue they might be in. Called by the PresenceEventListener when a user changes presence state. 
	 * @param user the user that disconnected
	 * @see #PresenceEventListener
	 */
	public static void userWentOffline(IUser user)
	{
		for (GatherObject object : gatherObjects)
		{
			if (object.remFromQueue(user) == 1)
			{
				DiscordBot.sendMessage(object.getCommandChannel(), object.fullUserString(user)
				                + " has been **removed** from the queue (disconnected) ("
				                + object.numPlayersInQueue()+ "/" 
				                + object.getMaxQueueSize()+ ")");
			}
		}
	}

	/**Does the things needed when a player leaves the server. As of writing this it only removes them from any queue they might be in. Called by the UserLeaveEventListener when a user leaves any guild. 
	 * @param user the user that disconnected
	 * @see #PresenceEventListener
	 */
	public static void userLeftGuild(IGuild guild, IUser user)
	{
		List<GatherObject> gatherObjects = DiscordBot.getGatherObjectsForGuild(guild);
		for(GatherObject obj : gatherObjects) {
			if (obj.remFromQueue(user) == 1) {
				DiscordBot.sendMessage(obj.getCommandChannel(), obj.fullUserString(user)
				                + " has been **removed** from the queue (left server) ("
				                + obj.numPlayersInQueue()+ "/" 
				                + obj.getMaxQueueSize()+ ")");
			}
		}
	}
	
	/**Used to get the info of a player from the kag2d api by username
	 * @param username The username of the player to look up
	 * @return PlayerInfoObject An object containing the player info
	 * @see https://developers.thd.vg/api/players.html
	 * @see https://api.kag2d.com/v1/player/username/info
	 */
	public static PlayerInfoObject getPlayerInfo(String username)
	{
		String caseCheckURL = "https://api.kag2d.com/v1/player/"+username+"/info";
		try
		{
	        	URL url = new URL(caseCheckURL);
			InputStreamReader reader = new InputStreamReader(url.openStream());
			CaseCheckObject caseCheck = new Gson().fromJson(reader, CaseCheckObject.class);
			return caseCheck.playerInfo;
		}
		catch(IOException e)
		{
			//dont want to do anything here, this function is supposed to find some wrong usernames
		}
		return null;
	}
	
	/**Used to get the correct case of a players username given a string of their username with potentially erroneous upper/lower case. Returns an empty string if player not found, otherwise the correct string
	 * @param username the username provided by someone that might have wrong case
	 * @return An empty string if the player could not be found, otherwise the correct player string
	 * @see https://developers.thd.vg/api/players.html
	 * @see https://api.kag2d.com/v1/player/username/info
	 */
	public static String getCorrectCase(String username)
	{
		PlayerInfoObject info = getPlayerInfo(username);
		if(info==null) return "";
		else return info.username;
	}
	
	public static List<PlayerObject> linkRequests;
	
	/**Helper function for checking for existing link requests
	 * @param user the user to check for
	 * @return the PlayerObject of the user if found, null if no player found
	 * @see DiscordBot#addLinkRequest(IUser, String)
	 * @see DiscordBot#doLinkRequest(String, IUser)
	 * @see DiscordBot#doLinkRequest(String, long)
	 */
	public static PlayerObject getDiscordLinkRequest(IUser user)
	{
		for(PlayerObject p : linkRequests)
		{
			if(p.getDiscordUserInfo().equals(user))
			{
				return p;
			}
		}
		return null;
	}
	
	/**Adds the first half of a link request to link the discord user and KAG user, this is called by the !linkserver command when a player wants to start the server linking process.
	 * @param user to be linked
	 * @param kagname to be linked
	 * @return 0 if updated an existing link request, 1 if a new link request was added
	 * @see CommandLinkServer
	 */
	public static int addLinkRequest(IUser user, String kagname)
	{
		//check for an existing request
		PlayerObject p = getDiscordLinkRequest(user);
		if(p != null)
		{
			p.setKagName(kagname);
			return 0;
		}
		//link request doesnt already exist for this user, make a new one
		//careful with these player objects, they are not managed, dont use them anywhere else
		linkRequests.add(new PlayerObject(user, kagname));
		return 1;
	}

	/** Complete a server link request checks if the first half of the request already exists and the details are correct, then links the accounts. 
	 * @param kagname to be linked
	 * @param user to be linked
	 * @return Returns -1 if the first half of the link request doesnt already exist, -2 if the first half of the link request is for a different account, -4 if a cross link was detected, and 1 if the link was successful
	 * @see GatherDB#linkAccounts(String, long)
	 * @see DiscordBot#addLinkRequest(IUser, String)
	 * @see DiscordBot#doLinkRequest(String, long)
	 */
	public static int doLinkRequest(String kagname, IUser user)
	{
		//check if the other half of this request exists
		PlayerObject p = getDiscordLinkRequest(user);
		if(p != null)
		{
			//other half of the request exists
			if(!p.getKagName().equals(kagname))
			{
				//they are using a different account to what they said they would
				return -2;
			}
			//both kag name and user info match
			int result = database.linkAccounts(kagname, user.getLongID());
			Discord4J.LOGGER.info("account linking changed "+result+" lines in the sql database");
			linkRequests.remove(p);
			if(result==-2) return -4;
			return 1;
		}
		return -1;
		
	}

	/**Wrapper function for {@link #doLinkRequest(String, IUser)}. Converts the id to an IUser object. Returns -3 if the user wasn't found. 
	 * @param kagname to be linked
	 * @param id to be linked
	 * @return Returns -1 if the first half of the link request doesnt already exist, -2 if the first half of the link request is for a different account, 1 if the link was successful, and -3 if user could not be found
	 * @see DiscordBot#doLinkRequest(String, IUser)
	 */
	public static int doLinkRequest(String kagname, long id)
	{
		IUser user = client.getUserByID(id);
		if(user == null) return -3;
		else return doLinkRequest(kagname, user);
	}
	
	public static GatherGame getPlayersGame(IUser user) {
		for(GatherObject gather : DiscordBot.gatherObjects)
		{
			GatherGame game = gather.getPlayersGame(user);
			if(game != null) return game;
		}
		return null;
	}

	public static void playerChanged(IUser user) {
		GatherGame game = DiscordBot.getPlayersGame(user);
		if(game != null)
		{
			game.updateTeamsOnServer();
			DiscordBot.sendMessage(DiscordBot.getGatherObjectForGame(game).getCommandChannel(), "the **link details** of "+user.mention()+" have been **updated** for game #"+game.getGameID());
		}
	}
	
}