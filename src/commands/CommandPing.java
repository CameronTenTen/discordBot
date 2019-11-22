package commands;
import java.util.Arrays;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CommandPing extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandPing(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("ping"), "Pong!");
	}
	
	@Override
	public boolean showInHelp() {
		return false;
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		return "Pong!";
	}
}