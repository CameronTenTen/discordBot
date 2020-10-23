package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;

/**Admin only command for setting queue size. Useful for testing. Must be used in command channel. 
 * @author cameron
 *
 */
public class CommandSetQueue extends Command<Message, Member, Channel>
{
	public CommandSetQueue(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("setqueue"), "Admin only - change the queue size", "setqueue queueSize");
	}

	@Override
	public boolean isChannelValid(Channel channel) {
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		else return true;
	}

	@Override
	public boolean hasPermission(Member member, Channel channel)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		return gather.isAdmin(member);
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return null;

		int newSize;
		//use the second argument as the queue size
		if(splitMessage.length<=1)
		{
			return "Invalid command format, queue size as a number must be provided";
		}
		try
		{
			newSize = Integer.parseInt(splitMessage[1]);
		}
		catch (NumberFormatException e)
		{
			return "Invalid command format, queue size as a number must be provided";
		}
		if(newSize<=gather.numPlayersInQueue())
		{
			return "Cannot set queue size less than or equal to current queue size: "+gather.numPlayersInQueue();
		}
		gather.setMaxQueueSize(newSize);
		
		gather.updateChannelCaption();
		return "Queue size has been set to "+gather.getMaxQueueSize();
	}
}