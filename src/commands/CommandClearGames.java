package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;

/**
 * Admin only command for clearing all currently running games for a gather object. Must be used in command channel. 
 * Calls GatherObject.clearGames()
 * @author cameron
 * @see GatherObject#clearGames()
 */
public class CommandClearGames extends Command<Message, Member, Channel>
{
	public CommandClearGames(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("cleargames"), "Admin only - clear all currently running games");
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

		gather.clearGames();
		return "cleared all currently running games";
	}
}