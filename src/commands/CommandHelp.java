package commands;

import java.util.Arrays;
import java.util.List;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

/**Command for fetching a list of possible commands with descriptions and usage examples
 * @author cameron
 *
 */
public class CommandHelp extends Command<Message, Member, Channel>
{
	public CommandHelp(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("help", "h"), "Display this help message", "help");
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
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