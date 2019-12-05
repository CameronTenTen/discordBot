package commands;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.DiscordBot;
import core.GatherObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

/**Command for players to remove themselves from the queue. Must be used in command channel. 
 * @author cameron
 *
 */
public class CommandRem extends Command<Message, Member, Channel>
{
	static final Logger LOGGER = LoggerFactory.getLogger(CommandRem.class);
	
	public CommandRem(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("rem", "remove", "leave"), "Remove yourself from the queue");
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

		synchronized(gather)
		{
			int remReturnVal = gather.remFromQueue(member);
			switch(remReturnVal)
			{
			case 1:
				LOGGER.info("Removing player from queue: "+member.getDisplayName());
				return gather.fullUserString(member)+" **left** the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")";
			case 0:
				return "You are already not in the queue "+member.getDisplayName()+"!";
			}
			return "An unexpected error occured attempting to remove "+member.getDisplayName()+" from the queue";
		}
	}
}