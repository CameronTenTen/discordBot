import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.Discord4JHandler;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;

public class DiscordBot
{
	
	public static IDiscordClient client; // The instance of the discord client.
	
	public static DiscordBot bot;
	
	public static Set<GatherObject> gatherObjects;
	
	public static Vector<ReactWatchObject> reactWatcher;
	
	public static void addGuild(IGuild guild)
	{
		gatherObjects.add(new GatherObject(guild));
	}
	
	public static GatherObject getGatherObjectForGuild(IGuild guild)
	{
		for(GatherObject object : gatherObjects)
		{
			if(object.getGuild().equals(guild))
				return object;
		}
		return null;
	}
	
	public void addMessageReactWatch(IMessage message, IEmoji reaction, GenericReactCallbackObject callback)
	{
		reactWatcher.add(new ReactWatchObject(message, reaction, callback));
	}
	
	public void removeMessageReactWatch(IMessage message, IEmoji reaction)
	{
		for(ReactWatchObject obj : reactWatcher)
		{
			if(obj.message.equals(message) && obj.emoji.equals(reaction))
				reactWatcher.remove(obj);
		}
	}
	
	public void onReactionAdded(ReactionAddEvent event)
	{
		for(ReactWatchObject obj : reactWatcher)
		{
			if(obj.message.equals(event.getMessage()) && obj.emoji.equals(event.getReaction().getCustomEmoji()))
				obj.callback.execute(event.getUser(), event.getGuild());
		}
	}
	
	public void startBot(String token)
	{
		ClientBuilder builder = new ClientBuilder(); // Creates a new client builder instance
		
		builder.withToken(token); // Sets the bot token for the client
		
		try {
			client = builder.login(); // Builds the IDiscordClient instance and logs it in
			System.out.println("logged in");
		} catch (DiscordException e) { // Error occurred logging in
			System.err.println("Error occurred while logging in with token: "+token);
			e.printStackTrace();
			return;
		}
		
		//event listeners
		EventDispatcher dispatcher = client.getDispatcher();
	        dispatcher.registerListener(new ReadyEventListener());
	        dispatcher.registerListener(new PresenceEventListener());
	        dispatcher.registerListener(new ReactionAddEventListener());
		
		
		//load the queue object
		
		//delete the saved object so we don't load it next time
		
		//command listeners
		CommandHandler cmdHandler = new Discord4JHandler(client);
		
		//add all the commands
		cmdHandler.registerCommand(new CommandPing());
		cmdHandler.registerCommand(new CommandAdd());
		cmdHandler.registerCommand(new CommandRem());
		cmdHandler.registerCommand(new CommandList());
		cmdHandler.registerCommand(new CommandClear());
		cmdHandler.registerCommand(new CommandForceRem());
		cmdHandler.registerCommand(new CommandEnd());
	}
	
	
	public static void main(String[] args)
	{
		bot = new DiscordBot();
		gatherObjects = new HashSet<GatherObject>();
		reactWatcher = new Vector<ReactWatchObject>();
		bot.startBot(args[0]);
	}
	
	public static void setPlayingText(String newText)
	{
		client.changePlayingText(newText);
	}
	
	public static void setChannelCaption(IGuild guild, String newText)
	{
		GatherObject gather = getGatherObjectForGuild(guild);
		
		//remove spaces from the string (spaces not allowed)
		//TODO: remove other illegal characters instead of just catching the exception
		newText.replaceAll("\\s", "");
		try
		{
			gather.getCommandChannel().changeName( newText + "_" + gather.textChannelString);
		}
		catch (IllegalArgumentException e)
		{
			Discord4J.LOGGER.warn("Error renaming channel: " + e.getMessage());
		}
		catch (MissingPermissionsException e)
		{
			Discord4J.LOGGER.warn("Error renaming channel: " + e.getMessage());
		}
	}
	
	/*public void reactionAdded(ReactionAddEvent event)
	{
		if()
		return;
	}*/
	
	public static void userWentOffline(IUser user)
	{
		for(GatherObject object : gatherObjects)
		{
			if(object.remFromQueue(new PlayerObject(user, false))==1)
			{
				object.getCommandChannel().sendMessage(object.fullUserString(user)+" has been **removed** from the queue (disconnected) ("+DiscordBot.getGatherObjectForGuild(object.getGuild()).numPlayersInQueue()+"/"+DiscordBot.getGatherObjectForGuild(object.getGuild()).maxQueueSize()+")");
				DiscordBot.setPlayingText(object.numPlayersInQueue()+"/"+object.maxQueueSize()+" in queue");
				DiscordBot.setChannelCaption(object.getGuild() , object.numPlayersInQueue()+"-in-q");
			}
		}
	}
}