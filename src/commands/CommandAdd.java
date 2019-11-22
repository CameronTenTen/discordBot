package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;

/**
 * Chat command for players to add to the queue. Must be used in command channel. 
 * Prints a response message to the command channel depending on the result of the command.
 * Starts a game if queue is filled by the add request.
 * <p>
 * Does not allow players to add while offline(invisible mode). This is done to prevent people adding while invisible, then going offline (which the bot cant detect). 
 * The bot needs to remove players when they go offline to prevent games starting after people have left. 
 * <p>
 * Adding to queue and starting game are contained in a synchronized statement to prevent players removing while a game is being started.
 * 
 * @author cameron
 *
 */
public class CommandAdd extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandAdd(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("add","join"), "Add yourself to the queue");
	}

	@Override
	public boolean isChannelValid(IChannel channel) {
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		else return true;
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return null;
		
		if (user.getPresence().getStatus() == StatusType.OFFLINE)
		{
			return "You cannot add while you are offline "+user.getDisplayName(guild)+"!";
		}
		
		synchronized(gather)
		{
			int addReturnVal = gather.addToQueue(user);
			
			switch(addReturnVal)
			{
			case -1:
				return "You must link before you can add to the queue "+user.getDisplayName(guild)+" type **!link KAGUsernameHere** to get started or **!linkhelp** for more information";
			case 1:
				Discord4J.LOGGER.info("Adding player to queue: "+user.getDisplayName(guild));
				return gather.fullUserString(user)+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")";
			case 2:
				this.reply(messageObject, gather.fullUserString(user)+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
				Discord4J.LOGGER.info("Adding player to queue: "+user.getDisplayName(guild));
				gather.startGame();
				return null;
			case 3:
				return"You cannot add to the queue when you are **already in a game** "+user.getDisplayName(guild)+"!";
			case 0:
				return "You are already in the queue "+user.getDisplayName(guild)+"!";
			case 4:
				return "You were not added because the queue is already full, try again later "+user.getDisplayName(guild)+"!";
			}
			return "An unexpected error occured adding "+user.getDisplayName(guild)+" to the queue";
		}
	}
}