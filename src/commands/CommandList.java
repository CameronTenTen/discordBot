package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;

/**Command for saying the current player list and queue size for the gather object associated with this channel.  Must be used in command channel. 
 * @author cameron
 * @see #GatherQueueObject
 * @see GatherObject#queueString()
 */
public class CommandList extends Command<Message, Member, Channel>
{
	public CommandList(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("list", "queue"), "Check the current player list");
	}

	@Override
	public boolean isChannelValid(Channel channel) {
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		else return true;
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return null;
		
		String currentQueue = gather.queueString();
		if(!currentQueue.isEmpty())
		{
			return "Current **queue** ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+"): "+currentQueue;
		}
		else
		{
			return "Queue is **empty**";
		}
	}
}