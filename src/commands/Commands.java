package commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO: document this
public abstract class Commands<M, U, C, G>
{
	//useful for finding the command associated with a command string
	private HashMap<String, Command<M, U, C, G>> commands;
	//useful when a list of commands is needed in the order they were created (i.e. when getting help)
	private List<Command<M, U, C, G>> commandList;
	List<String> prefixes;

	Commands()
	{
		this.commands = new HashMap<>();
		this.commandList = new ArrayList<>();
	}

	public void setPrefixes(List<String> prefixes)
	{
		this.prefixes = prefixes;
	}

	public void registerCommand(Command<M, U, C, G> command)
	{
		for(String alias : command.getAliases())
		{
			commands.put(alias, command);
		}
		commandList.add(command);
	}

	private Command<M, U, C, G> getCommand(String alias)
	{
		return commands.get(alias);
	}

	public void onMessage(String message, M messageObject, U user, C channel, G guild)
	{
		for (String prefix : this.prefixes)
		{
			if (message.startsWith(prefix))
			{
				//the message had a prefix we were looking for, now check if its a valid command
				message = message.substring(prefix.length());
				String[] splitMessage = message.split("[\\s&&[^\\n]]++");
				//just use the first command that matches
				Command<M, U, C, G> command = this.getCommand(splitMessage[0]);
				if (command != null && command.isChannelValid(channel))
				{
					if (command.hasPermission(user, channel, guild))
					{
						//found the command, execute it
						String returnValue = command.onCommand(splitMessage, message, messageObject, user, channel, guild);
						if (returnValue != null && returnValue.length()>0)
						{
							//send the return value as a reply to the message
							this.reply(messageObject, returnValue);
						}
						return;
					}
					else
					{
						//give some feedback to tell them they can't do that
						this.onProhibitedCommandUsed(messageObject, user, channel, guild);
					}
				}
				//if no command was found or the channel was invalid, make no response
			}
		}
	}

	public abstract void reply(M message, String replyMessage);

	public abstract void onProhibitedCommandUsed(M message, U user, C channel, G guild);

	public String getPrimaryPrefix()
	{
		if (prefixes == null || prefixes.size()<1)
		{
			return null;
		}
		return this.prefixes.get(0);
	}

	public List<String> getHelpMessageArray() {
		List<String> returnVal = new ArrayList<>();
		for (Command<M, U, C, G> command : commandList)
		{
			StringBuilder message = new StringBuilder();
			if (command.showInHelp()) {
				message.append(this.getPrimaryPrefix());
				//message.append(command.getPrimaryAlias());
				message.append(command.getUsage());
				String description = command.getDescription();
				if (description != null && description.length()>0)
				{
					message.append(" - ");
					message.append(description);
				}
				/*String usage = command.getUsage();
				if (usage != null && usage.length()>0)
				{
					message.append(" (");
					message.append(this.getPrimaryPrefix());
					message.append(usage);
					message.append(")");
				}*/
				returnVal.add(message.toString());
			}
		}
		return returnVal;
	}

	public String getHelpMessage()
	{
		StringBuilder message = new StringBuilder();
		message.append("```\n");
		List<String> helpMessages = this.getHelpMessageArray();
		for (String line : helpMessages)
		{
			message.append(line);
			message.append("\n");
		}
		message.append("```");
		return message.toString();
	}
}
