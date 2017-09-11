import java.util.HashSet;
import java.util.Set;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.Discord4JHandler;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuilder;

public class DiscordBot {

	public static IDiscordClient client; // The instance of the discord client.
	public RequestBuilder builder;
	
	public static DiscordBot bot;

	//one gather object per guild
	public static Set<GatherObject> gatherObjects;

	public boolean doKagServerConnections = true;

	public static GatherObject getGatherObjectForGuild(IGuild guild) {
		for (GatherObject object : gatherObjects) {
			if (object.getGuild().equals(guild))
				return object;
		}
		return null;
	}
	
	public static IGuild getGuildForServer(String ip, int port)
	{
		//TODO this is not good if there is multiple gather objects
		return gatherObjects.iterator().next().getGuild();
	}

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
		cmdHandler.registerCommand(new CommandGiveWin());
	}

	public static void main(String[] args) {
		bot = new DiscordBot();
		gatherObjects = new HashSet<GatherObject>();
		bot.startBot(args[0]);

	}

	public GatherServer getFreeServer(IGuild guild) {
		return getGatherObjectForGuild(guild).getFreeServer();
	}
	
	public static GatherServer getServer(IGuild guild, String ip, int port)
	{
		return getGatherObjectForGuild(guild).getServer(ip, port);
	}

	public static void setPlayingText(String newText) {
		client.changePlayingText(newText);
	}

	public static void setChannelCaption(IGuild guild, String newText) {
		GatherObject gather = getGatherObjectForGuild(guild);

		// remove spaces from the string (spaces not allowed)
		// TODO: remove other illegal characters instead of just catching the exception
		newText.replaceAll("\\s", "");
		try {
			gather.getCommandChannel().changeName(newText + "_" + gather.textChannelString);
		} catch (IllegalArgumentException e) {
			Discord4J.LOGGER.error("Error renaming channel: " + e.getMessage());
		} catch (MissingPermissionsException e) {
			Discord4J.LOGGER.error("Error renaming channel: " + e.getMessage());
		}
	}

	public static void userWentOffline(IUser user) {
		for (GatherObject object : gatherObjects) {
			if (object.remFromQueue(new PlayerObject(user, false)) == 1) {
				DiscordBot.setPlayingText(object.numPlayersInQueue() + "/" + object.getMaxQueueSize()
						+ " in queue");
				DiscordBot.setChannelCaption(object.getGuild(), object.numPlayersInQueue() + "-in-q");
				object.getCommandChannel().sendMessage(object.fullUserString(user)
						+ " has been **removed** from the queue (disconnected) ("
						+ DiscordBot.getGatherObjectForGuild(object.getGuild())
								.numPlayersInQueue()
						+ "/" + DiscordBot.getGatherObjectForGuild(object.getGuild())
								.getMaxQueueSize()
						+ ")");
			}
		}
	}
}