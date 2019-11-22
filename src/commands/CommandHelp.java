package commands;

import java.util.Arrays;
import java.util.List;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

//TODO: document this
public class CommandHelp extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandHelp(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("help", "h"), "Display this help message", "help");
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		//reply with the help text split into two messages (just a quick hack for now to prevent the message being too long)
		List<String> helpMessages = commands.getHelpMessageArray();
		StringBuilder message = new StringBuilder();
		message.append("```\n");
		for (String line : helpMessages)
		{
			//discord has a character limit of 2000, give it 100 characters leeway just in case
			if (message.length()>1900)
			{
				message.append("```");
				this.reply(messageObject, message.toString());
				message = new StringBuilder();
				message.append("```\n");
				
			}
			message.append(line);
			message.append("\n");
		}
		message.append("```");
		return message.toString();
	}
	
}