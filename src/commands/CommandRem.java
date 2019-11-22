package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Command for players to remove themselves from the queue. Must be used in command channel. 
 * @author cameron
 *
 */
public class CommandRem extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandRem(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("rem", "remove", "leave"), "Remove yourself from the queue");
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

		synchronized(gather)
		{
			int remReturnVal = gather.remFromQueue(user);
			switch(remReturnVal)
			{
			case 1:
				Discord4J.LOGGER.info("Removing player from queue: "+user.getDisplayName(guild));
				return gather.fullUserString(user)+" **left** the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")";
			case 0:
				return "You are already not in the queue "+user.getDisplayName(guild)+"!";
			}
			return "An unexpected error occured attempting to remove "+user.getDisplayName(guild)+" from the queue";
		}
	}
}