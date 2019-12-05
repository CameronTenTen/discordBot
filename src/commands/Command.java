package commands;

import java.util.List;

/**Abstract class describing the interface to use when creating a command. 
 * @author cameron
 *
 * @param <M> the message object
 * @param <U> the user object
 * @param <C> the channel object
 */
public abstract class Command<M, U, C>
{
	Commands<M, U, C> commands;
	private List<String> aliases;
	private String description;
	private String usage;

	/**Create a new command object
	 * <p>
	 * e.g. super(commands, Arrays.asList("add","join"), "Add yourself to the queue", "add");
	 * @param commands the command object this command is attached to
	 * @param aliases a list of aliases for this command
	 * @param description the description of this command to be used in help messages
	 * @param usage usage string to be used in displaying help messages
	 */
	Command(Commands<M, U, C> commands, List<String> aliases, String description, String usage)
	{
		this.commands = commands;
		this.aliases = aliases;
		this.description = description;
		this.usage = usage;
	}

	/**Initialising without an alias means the alias is the same as the command
	 * <p>
	 * e.g. super(commands, Arrays.asList("add","join"), "Add yourself to the queue");
	 * @param commands the command object this command is attached to
	 * @param aliases a list of aliases for this command
	 * @param description the description of this command to be used in help messages
	 */
	Command(Commands<M, U, C> commands, List<String> aliases, String description)
	{
		this(commands, aliases, description, aliases.get(0));
	}

	/**Get a list of aliases for this command
	 * @return
	 */
	public List<String> getAliases()
	{
		return this.aliases;
	}

	/**The primary alias is the first one that was defined
	 * @return primary alias string
	 */
	public String getPrimaryAlias()
	{
		return this.aliases.get(0);
	}

	/**The message description sentence
	 * @return message description string
	 */
	public String getDescription()
	{
		return this.description;
	}

	/**The message usage example
	 * @return message usage string
	 */
	public String getUsage()
	{
		if (this.usage == null || this.usage.length()<=0)
		{
			return this.aliases.get(0);
		}
		return this.usage;
	}

	/**Function called to check if the user has permssion to use that command in the channel they sent the message
	 * @param user the user sending the message
	 * @param channel the channel the message was sent in
	 * @return true if they have permission, false otherwise
	 */
	public boolean hasPermission(U user, C channel)
	{
		return true;
	}

	/**Function called to check if this command was used in a valid channel
	 * @param channel the channel the message was sent in
	 * @return true if the channel is valid, false otherwise
	 */
	public boolean isChannelValid(C channel)
	{
		return true;
	}

	/**Function called when constructing help to check if this command should be added
	 * @return true if the command should be shown in help, false otherwise
	 */
	public boolean showInHelp()
	{
		return true;
	}

	/**The function that is called when this command should be executed, after checking if the user has permission and the channel is valid
	 * @param splitMessage the message split into strings with whitespace as the delimiter
	 * @param messageString the whole unmodified message string
	 * @param messageObject the message object
	 * @param user the user object
	 * @param channel the channel object
	 * @return any message that should be sent as a reply to the command
	 */
	public abstract String onCommand(String[] splitMessage, String messageString, M messageObject, U user, C channel);

	/**Reply to a message with the specified string
	 * @param message the message to reply to
	 * @param replyMessage the string to reply with
	 */
	public void reply(M message, String replyMessage)
	{
		commands.reply(message, replyMessage);
	}
}
