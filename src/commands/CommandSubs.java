package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import core.SubManager;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

/**Command for checking the current sub requests. Must be used in command channel. 
 * @author cameron
 * @see SubManager#toString()
 */
public class CommandSubs extends Command<Message, Member, Channel>
{
	public CommandSubs(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("subs"), "Check current sub requests");
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
		
		String currentSubs = gather.substitutions.toString();
		if(!currentSubs.isEmpty())
		{
			return "There is currently sub requests for: "+currentSubs;
		}
		else
		{
			return "No subs currently requested";
		}
	}
}