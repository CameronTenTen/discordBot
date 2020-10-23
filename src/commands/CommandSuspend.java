package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;

/**
 * Admin only command for disabling interaction with the gather bot. 
 * @author cameron
 */
public class CommandSuspend extends Command<Message, Member, Channel>
{
	public CommandSuspend(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("suspend"), "Admin only - disable interaction with the gather bot", "suspend");
	}

	@Override
	public boolean isChannelValid(Channel channel) {
		return true;
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
		this.commands.suspend("resume", "Gather bot interaction has been **resumed**");
		return "Gather bot interaction has been **suspended** use !resume to resume";
	}
}