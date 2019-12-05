package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

/**Command for checking the status of any currently running games. Must be used in command channel. 
 * @author cameron
 * @see GatherObject#statusString()
 */
public class CommandStatus extends Command<Message, Member, Channel>
{
	public CommandStatus(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("status"), "Check status of current games");
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
		
		String currentStatus = gather.statusString();
		if(!currentStatus.isEmpty())
		{
			return "Current games: \n" + currentStatus;
		}
		else
		{
			return "No games currently running";
		}
	}
}