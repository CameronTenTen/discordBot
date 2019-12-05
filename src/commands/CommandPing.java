package commands;
import java.util.Arrays;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

public class CommandPing extends Command<Message, Member, Channel>
{
	public CommandPing(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("ping"), "Pong!");
	}
	
	@Override
	public boolean showInHelp() {
		return false;
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
	{
		return "Pong!";
	}
}