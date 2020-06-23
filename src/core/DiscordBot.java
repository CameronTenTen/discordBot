package core;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import commands.CommandAdd;
import commands.CommandCachedPlayerInfo;
import commands.CommandCancelGame;
import commands.CommandClearGames;
import commands.CommandClearPlayerCache;
import commands.CommandClearQueue;
import commands.CommandClearSubs;
import commands.CommandConnect;
import commands.CommandDisconnect;
import commands.CommandEnd;
import commands.CommandEndGame;
import commands.CommandForceRem;
import commands.CommandForceSub;
import commands.CommandHelp;
import commands.CommandLink;
import commands.CommandLinkHelp;
import commands.CommandLinkServer;
import commands.CommandList;
import commands.CommandPing;
import commands.CommandPlayerInfo;
import commands.CommandPlayers;
import commands.CommandRefreshServers;
import commands.CommandRem;
import commands.CommandRsub;
import commands.CommandScramble;
import commands.CommandSetQueue;
import commands.CommandStart;
import commands.CommandStats;
import commands.CommandStatus;
import commands.CommandSub;
import commands.CommandSubs;
import commands.CommandSuspend;
import commands.Discord4JCommands;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.lifecycle.ConnectEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.presence.Status;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;

/**The main bot class. Contains most of the interaction with the Discord bot client. Contains various helper functions and objects.
 * @author cameron
 *
 */
public class DiscordBot
{
	//environment variables
	static final String botTokenEnvVarName = "BOT_TOKEN";
	static final String dbUsernameEnvVarName = "DB_USERNAME";
	static final String dbPasswordEnvVarName = "DB_PASSWORD";
	static final String dbIpAddressEnvVarName = "DB_IP";
	static final String dbPortEnvVarName = "DB_PORT";
	static final String dbURLEnvVarName = "DATABASE_URL";
	static final String dbDatabaseEnvVarName = "DB_DATABASE";
	static final String serversJsonEnvVarName = "SERVERS_JSON";
	
	//config file paths
	static final String databasePropertiesFilePath = "database.propertiesa";
	static final String serversJsonFilePath = "servers.jsona";
	
	static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
	/**
	 * The instance of the Discord4J client
	 */
	public static DiscordClient client;
	
	public static DiscordBot bot;
	
	/**The database manipulation object
	 * @see GatherDB
	 */
	public static GatherDB database;
	/**The player object tracker
	 * @see PlayerObjectManager
	 */
	public static PlayerObjectManager players;

	//TODO: change this to a map of channel to gather object for quicker lookup in the most common case
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
	public static GatherObject getGatherObjectForChannel(Channel channel) {
		if(channel==null) return null;
		for (GatherObject object : gatherObjects) {
			if (object.getCommandChannel().equals(channel))
				return object;
		}
		return null;
	}

	/**Helper function for getting the correct gather objects for a guild, used when a user leaves a guild.
	 * @param guildId - the snowflake id of the guild
	 * @return A list of GatherObjects associated with the guild, list is empty if there are no objects for this guild
	 * @see GatherObject
	 */
	public static List<GatherObject> getGatherObjectsForGuild(Snowflake guildId) {
		if(guildId==null) return null;
		List<GatherObject> returnList = new ArrayList<GatherObject>();
		for (GatherObject object : gatherObjects) {
			if (object.getGuild().getId().equals(guildId))
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

	/**Initial bot setup goes here.
	 * <p>
	 * Some things that are done here include instantiation of the client and request builder,
	 * registering event listeners, registering command listeners, reading the gather object config file,
	 * connecting to the kag servers, client login.
	 * @param token - the bot login token to be used for authentication
	 */
	public void startBot(String token) {
		client = new DiscordClientBuilder(token).setInitialPresence(Presence.online()).build();

		// event listeners
		client.getEventDispatcher().on(PresenceUpdateEvent.class).subscribe((PresenceUpdateEvent event) -> 
		{
			if(event.getCurrent().getStatus()==Status.OFFLINE)
			{
				DiscordBot.userWentOffline(event.getMember().block());
			}
		});
		client.getEventDispatcher().on(MemberLeaveEvent.class).subscribe((MemberLeaveEvent event) ->
		{
			DiscordBot.userLeftGuild(event.getGuildId(), event.getMember().get());
		});

		//TODO load the queue object
		//TODO delete the saved object so we don't load it next time

		// command listening
		Discord4JCommands commands = new Discord4JCommands();
		commands.registerCommand(new CommandHelp(commands));
		client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(commands::onMessageReceivedEvent);

		// add all the commands
		commands.registerCommand(new CommandPing(commands));
		commands.registerCommand(new CommandLink(commands));
		commands.registerCommand(new CommandLinkServer(commands));
		commands.registerCommand(new CommandLinkHelp(commands));
		commands.registerCommand(new CommandAdd(commands));
		commands.registerCommand(new CommandRem(commands));
		commands.registerCommand(new CommandList(commands));
		commands.registerCommand(new CommandPlayers(commands));
		commands.registerCommand(new CommandRsub(commands));
		commands.registerCommand(new CommandSub(commands));
		commands.registerCommand(new CommandSubs(commands));
		commands.registerCommand(new CommandScramble(commands));
		commands.registerCommand(new CommandCancelGame(commands));
		commands.registerCommand(new CommandStatus(commands));
		commands.registerCommand(new CommandStats(commands));
		commands.registerCommand(new CommandPlayerInfo(commands));
		commands.registerCommand(new CommandCachedPlayerInfo(commands));
		commands.registerCommand(new CommandClearQueue(commands));
		commands.registerCommand(new CommandForceRem(commands));
		commands.registerCommand(new CommandClearGames(commands));
		commands.registerCommand(new CommandSetQueue(commands));
		commands.registerCommand(new CommandEndGame(commands));
		commands.registerCommand(new CommandStart(commands));
		commands.registerCommand(new CommandEnd(commands));
		commands.registerCommand(new CommandClearSubs(commands));
		commands.registerCommand(new CommandForceSub(commands));
		commands.registerCommand(new CommandRefreshServers(commands));
		//commands.registerCommand(new CommandPingMe(commands));
		//commands.registerCommand(new CommandRandomTeams(commands));
		commands.registerCommand(new CommandDisconnect(commands));
		commands.registerCommand(new CommandConnect(commands));
		commands.registerCommand(new CommandClearPlayerCache(commands));
		commands.registerCommand(new CommandSuspend(commands));

		/*List<IGuild> guilds = event.getClient().getGuilds();
		if(guilds != null && guilds.size()>0)
		{
			for(IGuild guild : guilds)
			{
				DiscordBot.addGuild(guild);
			}
		}*/
		try {
			Gson gson = new Gson();
			JsonReader reader;
			GatherObjectConfig config = null;
			File f = new File(DiscordBot.serversJsonFilePath);
			if(f.exists() && !f.isDirectory())
			{
				reader = new JsonReader(new FileReader(f));
				config = gson.fromJson(reader, GatherObjectConfig.class);
			}
			else
			{
				//try the environment variable
				config = gson.fromJson(System.getenv(DiscordBot.serversJsonEnvVarName), GatherObjectConfig.class);
			}
			if(config==null || config.guildID==0L)
			{
				//print an error and exit if no server config found (or guild is 0 since that probably means the config was empty)
				LOGGER.error("ERROR: No servers configuration detected(or guild not set), either provide a server json file, "
				                   + "or set the environment variable ("+DiscordBot.serversJsonEnvVarName+")");
				return;
			}
			GatherObject obj = new GatherObject(config);
			DiscordBot.gatherObjects.add(obj);
			obj.connectKAGServers(true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		//just get the first gather object for now to set the playing text
		//TODO: playing text wont really work if there was ever multiple servers
		Iterator<GatherObject> itr = DiscordBot.gatherObjects.iterator();
		GatherObject gather = itr.next();
		if(gather == null) return;
		gather.clearQueueRole();

		//wait until the bot connects to update the channel caption (doesn't work before then)
		client.getEventDispatcher().on(ConnectEvent.class).subscribe((ConnectEvent event) ->
		{
			gather.updateChannelCaption();
		});

		LOGGER.info("logging in");
		client.login().block();
	}

	/**Main, instantiates some things, loads the database properties, sets up the database and player object managers
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		bot = new DiscordBot();
		gatherObjects = new HashSet<GatherObject>();
		
		//load database properties
		String user = null;
		String pass = null;
		String ip = null;
		String port = null;
		String db = null;
		String url = null;
		File f = new File(DiscordBot.databasePropertiesFilePath);
		if(f.exists() && !f.isDirectory()) { 
			Properties props = new Properties();
			FileInputStream input = new FileInputStream(f);
			props.load(input);
			user = props.getProperty("username");
			pass = props.getProperty("password");
			ip = props.getProperty("ipaddress");
			port = props.getProperty("port");
			db = props.getProperty("database");
			input.close();
		}
		else
		{
			//database properties file doesn't exist, try getting the config from environment variables instead
			url = System.getenv(DiscordBot.dbURLEnvVarName);
			user = System.getenv(DiscordBot.dbUsernameEnvVarName);
			pass = System.getenv(DiscordBot.dbPasswordEnvVarName);
			ip = System.getenv(DiscordBot.dbIpAddressEnvVarName);
			port = System.getenv(DiscordBot.dbPortEnvVarName);
			db = System.getenv(DiscordBot.dbDatabaseEnvVarName);
		}
		//initialise the database manager and connect to the database
		if(url!=null)
		{
			URI dbUri = null;
			try {
				dbUri = new URI(url);
			} catch (URISyntaxException e) {
				LOGGER.error("ERROR: DB URL format not recognised");
			}
			//extract the parameters from the url, if they can be extracted
			//use the value of the environment variable if the variable is not in the url
			if (dbUri.getUserInfo().split(":")[0]!=null && dbUri.getUserInfo().split(":")[0]!="")
			{
				user =  dbUri.getUserInfo().split(":")[0];
			}
			if (dbUri.getUserInfo().split(":")[1]!=null && dbUri.getUserInfo().split(":")[1]!="")
			{
				pass = dbUri.getUserInfo().split(":")[1];
			}
			if (dbUri.getHost()!=null && dbUri.getHost()!="")
			{
				ip = dbUri.getHost();
			}
			if (Integer.toString(dbUri.getPort())!=null && Integer.toString(dbUri.getPort())!="")
			{
				port = Integer.toString(dbUri.getPort());
			}
			if (dbUri.getPath().replaceFirst("/", "")!=null && dbUri.getPath().replaceFirst("/", "")!="")
			{
				db = dbUri.getPath().replaceFirst("/", "");
			}
		}
		
		if (user!=null && pass != null && ip != null && port != null && db != null)
		{
			database = new GatherDB(user, pass, ip, port, db);
		}
		else
		{
			LOGGER.error("ERROR: No database configuration detected, either provide a database properties file, "
			                   + "or set the appropriate environment variables ("+
			                   DiscordBot.dbURLEnvVarName+", "+
			                   DiscordBot.dbUsernameEnvVarName+", "+
			                   DiscordBot.dbPasswordEnvVarName+", "+
			                   DiscordBot.dbIpAddressEnvVarName+", "+
			                   DiscordBot.dbPortEnvVarName+", "+
			                   DiscordBot.dbDatabaseEnvVarName+")");
			return;
		}
		
		players = new PlayerObjectManager();
		
		linkRequests = new ArrayList<PlayerObject>();
		
		String token = null;
		if(args.length>=1)
		{
			token = args[0];
		}
		else
		{
			//try the environment variable instead
			token = System.getenv(DiscordBot.botTokenEnvVarName);
			if (token==null)
			{
				LOGGER.error("ERROR: need a bot token, specify it as a command line argument or use the "+DiscordBot.botTokenEnvVarName+" environment variable");
				return;
			}
		}
		
		bot.startBot(token);
	}

	//wrappers, so we are a little detached from the library

	/**Wrapper function for sending messages with tts defaulted to false.
	 * @param channel - the channel to put the message in
	 * @param msg - the message to send
	 * @return the message object after the message has been created, usually only want this if we are going to edit the message. 
	 * @see #sendMessage(MessageChannel, String, boolean)
	 */
	public static Message sendMessage(MessageChannel channel, String msg)
	{
		return sendMessage(channel, msg, false);
	}
	
	/**Wrapper function for sending messages. 
	 * @param channel - the channel to put the message in
	 * @param msg - the message to send
	 * @param tts - should use text to speech
	 * @return the message object after the message has been created, usually only want this if we are going to edit the message. 
	 * @see Discord4J: {@link MessageChannel#createMessage(java.util.function.Consumer)}
	 */
	public static Message sendMessage(MessageChannel channel, String msg, boolean tts)
	{
		return channel.createMessage(messageSpec ->
		{
			messageSpec.setContent(msg);
			messageSpec.setTts(tts);
		}).block();
	}
	
	/**Wrapper for editing messages. 
	 * @param msg - the message to be edited
	 * @param newString - the new version of the message
	 * @see Discord4J: {@link Message#edit(java.util.function.Consumer)}
	 */
	public static void editMessage(Message msg, String newString)
	{
		if (msg == null) return;
		msg.edit(editSpec ->
		{
			editSpec.setContent(newString);
		}).block();
	}
	
	/**Wrapper for deleting messages.
	 * @param msg - the message to delete
	 * @see Discord4J: {@link Message#delete()}
	 */
	public static void deleteMessage(Message msg)
	{
		if (msg == null) return;
		msg.delete().block();
	}
	
	/**Wrapper for adding roles to a guild member. 
	 * @param member - the member to be changed
	 * @param role - the role to give them
	 * @see Discord4J: {@link Member#addRole(Snowflake)}
	 */
	public static void addRole(Member member, Role role)
	{
		if(member == null || role == null) return;
		member.addRole(role.getId()).block();
	}
	
	/**Wrapper for removing roles from a guild member. 
	 * @param member - the member to be changed
	 * @param role - the role to take away
	 * @see Discord4J: {@link Member#removeRole(Snowflake)}
	 */
	public static void removeRole(Member member, Role role)
	{
		if(member == null || role == null) return;
		member.removeRole(role.getId()).block();
	}
	
	/**Wrapper for deleting a role from a guild.
	 * @param role the role object to delete
	 */
	public static void deleteRole(Role role)
	{
		if(role==null) return;
		role.delete().block();
	}
	
	/**Wrapper for getting a list of the roles of a guild sorted by their natural positions. 
	 * @param guild the guild to get the roles from
	 * @return Flux<Role> that emits the guilds roles
	 * @see Discord4J: {@link Guild#getRoles()}
	 */
	public static Flux<Role> getRoles(Guild guild)
	{
		return guild.getRoles();
	}
	
	/**Wrapper for moving a guild member to a voice channel. User must already be in a voice channel to allow moving them. Requires the Permission.MOVE_MEMBERS permission.
	 * @param member - the member to be moved
	 * @param channel - the channel to move them to
	 * @see Discord4J: {@link Member#edit(java.util.function.Consumer)}
	 */
	public static void moveToVoiceChannel(Member member, VoiceChannel channel)
	{
		member.edit(editSpec ->
		{
			editSpec.setNewVoiceChannel(channel.getId());
		}).block();
	}

	/**Wrapper for getting/creating a users private message channel. Used for sending messages directly to a user. 
	 * @param user the user you want to message
	 * @return the user's private channel
	 * @see Discord4J: {@link User#getPrivateChannel()}
	 */
	public static PrivateChannel getPMChannel(User user)
	{
		return user.getPrivateChannel().block();
	}

	/**Wrapper for setting the bot "playing" text. Playing text is global for all guilds the bot is in. 
	 * @param newText - the new playing text to use
	 * @see Discord4J: {@link DiscordClient#updatePresence(Presence)}
	 */
	public static void setPlayingText(String newText)
	{
		//TODO: make this a custom status
		client.updatePresence(Presence.online(Activity.playing(newText))).block();
	}

	/**Wrapper for setting the title of a text channel. Does some basic parsing of the input text to remove spaces, but will throw an error for other invalid characters. 
	 * <p>
	 * Discord only allows alphanumeric characters, dashes, and underscores in text channel names. 
	 * @param channel the channel to be changed
	 * @param newText the new channel name
	 */
	public static void setChannelCaption(TextChannel channel, String newText)
	{
		// remove spaces from the string (spaces not allowed)
		// TODO: remove other illegal characters
		newText.replaceAll("\\s", "");
		channel.edit(editSpec ->
		{
			editSpec.setName(newText);
		}).block();
	}

	/**Wrapper for fetching a Discord user.
	 * @param id of user to be fetched
	 * @return Discord user
	 */
	public static User fetchUser(Snowflake id)
	{
		return client.getUserById(id).block();
	}

	/**Wrapper for fetching a Discord member.
	 * @param guildId of the guild the member should be associated with
	 * @param userId of the user to be fetched
	 * @return Discord guild member
	 */
	public static Member fetchMember(Snowflake guildId, Snowflake userId)
	{
		return client.getMemberById(guildId, userId).block();
	}

	/**Helper for fetching a Discord member by username. Returns the first matching member, I don't know if this order is consistent. 
	 * @param guild to search
	 * @param name of user to be found
	 * @return Discord guild member
	 */
	public static Member findMemberByUsername(Guild guild, String name)
	{
		if(name==null) return null;
		return guild.getMembers().filter(member -> name.equalsIgnoreCase(member.getUsername())).blockFirst();
	}

	/**Helper for fetching a Discord member by display name. Returns the first matching member, I don't know if this order is consistent. 
	 * @param guild to search
	 * @param name of user to be found
	 * @return Discord guild member
	 */
	public static Member findMemberByDisplayName(Guild guild, String name)
	{
		if(name==null) return null;
		return guild.getMembers().filter(member -> name.equalsIgnoreCase(member.getDisplayName())).blockFirst();
	}

	/**Does the things needed when a player disconnects. As of writing this it only removes them from any queue they might be in. 
	 * @param member the member that disconnected
	 */
	public static void userWentOffline(Member member)
	{
		for (GatherObject object : gatherObjects)
		{
			if (object.remFromQueue(member) == 1)
			{
				DiscordBot.sendMessage(object.getCommandChannel(), object.fullUserString(member)
				                + " has been **removed** from the queue (disconnected) ("
				                + object.numPlayersInQueue()+ "/" 
				                + object.getMaxQueueSize()+ ")");
			}
		}
	}

	/**Does the things needed when a player leaves the server. As of writing this it only removes them from any queue they might be in. 
	 * @param guildId the guild the member left
	 * @param member the member that disconnected
	 * @see #PresenceEventListener
	 */
	public static void userLeftGuild(Snowflake guildId, Member member)
	{
		List<GatherObject> gatherObjects = DiscordBot.getGatherObjectsForGuild(guildId);
		for(GatherObject obj : gatherObjects) {
			if (obj.remFromQueue(member) == 1) {
				DiscordBot.sendMessage(obj.getCommandChannel(), obj.fullUserString(member)
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
	public static PlayerObject getDiscordLinkRequest(Snowflake userId)
	{
		for(PlayerObject p : linkRequests)
		{
			if(p.getDiscordUserInfo().getId().equals(userId))
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
	public static int addLinkRequest(Member member, String kagname)
	{
		//check for an existing request
		PlayerObject p = getDiscordLinkRequest(member.getId());
		if(p != null)
		{
			p.setKagName(kagname);
			return 0;
		}
		//link request doesnt already exist for this user, make a new one
		//careful with these player objects, they are not managed, dont use them anywhere else
		linkRequests.add(new PlayerObject(member, kagname));
		return 1;
	}

	/** Complete a server link request checks if the first half of the request already exists and the details are correct, then links the accounts. 
	 * @param kagname to be linked
	 * @param user to be linked
	 * @return -1 if the first half of the link request doesnt already exist, -2 if the first half of the link request is for a different account, -4 if a cross link was detected, and 1 if the link was successful
	 * @see GatherDB#linkAccounts(String, long)
	 * @see DiscordBot#addLinkRequest(IUser, String)
	 * @see DiscordBot#doLinkRequest(String, long)
	 */
	public static int doLinkRequest(String kagname, Snowflake userId)
	{
		//check if the other half of this request exists
		PlayerObject p = getDiscordLinkRequest(userId);
		if(p != null)
		{
			//other half of the request exists
			if(!p.getKagName().equals(kagname))
			{
				//they are using a different account to what they said they would
				return -2;
			}
			//both kag name and user info match
			int result = database.linkAccounts(kagname, userId.asLong(), p.getDiscordUserInfo().getGuildId().asLong());
			LOGGER.info("account linking changed "+result+" lines in the sql database");
			linkRequests.remove(p);
			if(result==-2) return -4;
			return 1;
		}
		return -1;
		
	}

	/**Wrapper function for {@link #doLinkRequest(String, IUser)}. Converts the id to an IUser object. Returns -3 if the user wasn't found. 
	 * @param kagname to be linked
	 * @param id to be linked
	 * @return -1 if the first half of the link request doesnt already exist, -2 if the first half of the link request is for a different account, 1 if the link was successful, and -3 if user could not be found
	 * @see DiscordBot#doLinkRequest(String, IUser)
	 */
	public static int doLinkRequest(String kagname, long id)
	{
		return doLinkRequest(kagname, Snowflake.of(id));
	}
	
	/**Helper function for getting the game a player is in by user object
	 * @param user to look for
	 * @return the GatherGame object for the game they are in, or null if no game was found
	 */
	public static GatherGame getPlayersGame(User user)
	{
		for(GatherObject gather : DiscordBot.gatherObjects)
		{
			GatherGame game = gather.getPlayersGame(user);
			if(game != null) return game;
		}
		return null;
	}

	/**Function to be triggered when a player updates their link info. Triggers an update of the teams on any game they are in incase they were fixing their username. 
	 * @param user
	 */
	public static void playerChanged(User user)
	{
		GatherGame game = DiscordBot.getPlayersGame(user);
		if(game != null)
		{
			game.updateTeamsOnServer();
			DiscordBot.sendMessage(DiscordBot.getGatherObjectForGame(game).getCommandChannel(), "the **link details** of "+user.getMention()+" have been **updated** for game #"+game.getGameID());
		}
	}
	
}