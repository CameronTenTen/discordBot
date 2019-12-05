package commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**Abstract class describing a command managing interface, to be instantiated for different frameworks/objects.
 * @author cameron
 *
 * @param <M> the message object
 * @param <U> the user object
 * @param <C> the channel object
 */
public abstract class Commands<M, U, C>
{
	//useful for finding the command associated with a command string
	private HashMap<String, Command<M, U, C>> commands;
	//useful when a list of commands is needed in the order they were created (i.e. when getting help)
	private List<Command<M, U, C>> commandList;
	List<String> prefixes;

	Commands()
	{
		this.commands = new HashMap<>();
		this.commandList = new ArrayList<>();
	}

	/**Set the list of prefixes the bot listening for
	 * @param prefixes
	 */
	public void setPrefixes(List<String> prefixes)
	{
		this.prefixes = prefixes;
	}

	/**Register a new command
	 * @param command
	 */
	public void registerCommand(Command<M, U, C> command)
	{
		for(String alias : command.getAliases())
		{
			commands.put(alias.toLowerCase(), command);
		}
		commandList.add(command);
	}

	/**Find a command by alias
	 * @param alias
	 * @return
	 */
	private Command<M, U, C> getCommand(String alias)
	{
		return commands.get(alias);
	}

	/**Should be triggered every time a message is received, checks if it is a valid command, in a valid channel, and the user has permission, then executes the command if appropriate. 
	 * @param message the message received as a string
	 * @param messageObject the message received as an object
	 * @param user the user that sent the message
	 * @param channel the channel the message was sent in
	 */
	public void onMessage(String message, M messageObject, U user, C channel)
	{
		for (String prefix : this.prefixes)
		{
			if (message != null && message.startsWith(prefix))
			{
				//the message had a prefix we were looking for, now check if its a valid command
				message = message.substring(prefix.length());
				String[] splitMessage = message.split("[\\s&&[^\\n]]++");
				//just use the first command that matches
				Command<M, U, C> command = this.getCommand(splitMessage[0].toLowerCase());
				if (command != null && command.isChannelValid(channel))
				{
					if (command.hasPermission(user, channel))
					{
						//found the command, execute it
						String returnValue = command.onCommand(splitMessage, message, messageObject, user, channel);
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
						this.onProhibitedCommandUsed(messageObject, user, channel);
					}
				}
				//if no command was found or the channel was invalid, make no response
			}
		}
	}

	/**Helper function for replying to a received message
	 * @param message the message object to reply to
	 * @param replyMessage the string to reply with
	 */
	public abstract void reply(M message, String replyMessage);

	/**Helper message for returning some feedback when a forbidden command is used (e.g. "You must be an admin to do that")
	 * @param message the command message that is forbidden
	 * @param user the user that tried to use the command
	 * @param channel the channel that the command was sent in
	 */
	public abstract void onProhibitedCommandUsed(M message, U user, C channel);

	/**The commands object can listen for multiple different prefixes, this returns the first/primary one
	 * @return the primary prefix string
	 */
	public String getPrimaryPrefix()
	{
		if (prefixes == null || prefixes.size()<1)
		{
			return null;
		}
		return this.prefixes.get(0);
	}

	/**Helper function for returning an array of strings, where each element in the array is the help message for one command
	 * @return the list of help messages
	 */
	public List<String> getHelpMessageArray() {
		List<String> returnVal = new ArrayList<>();
		for (Command<M, U, C> command : commandList)
		{
			StringBuilder message = new StringBuilder();
			if (command.showInHelp()) {
				message.append(this.getPrimaryPrefix());
				message.append(command.getUsage());
				String description = command.getDescription();
				if (description != null && description.length()>0)
				{
					message.append(" - ");
					message.append(description);
				}
				returnVal.add(message.toString());
			}
		}
		return returnVal;
	}

	/**Gets the help message as a single string, only really useful if the message is short (if its too long it cant be sent as one message)
	 * @return a string containing the help message
	 */
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
