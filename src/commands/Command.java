package commands;

import java.util.List;

public abstract class Command<M, U, C, G>
{
	Commands<M, U, C, G> commands;
	private List<String> aliases;
	private String description;
	private String usage;

	Command(Commands<M, U, C, G> commands, List<String> aliases, String description, String usage)
	{
		this.commands = commands;
		this.aliases = aliases;
		this.description = description;
		this.usage = usage;
	}

	Command(Commands<M, U, C, G> commands, List<String> aliases, String description)
	{
		this(commands, aliases, description, aliases.get(0));
	}

	public List<String> getAliases()
	{
		return this.aliases;
	}

	public String getPrimaryAlias()
	{
		return this.aliases.get(0);
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getUsage()
	{
		if (this.usage == null || this.usage.length()<=0)
		{
			return this.aliases.get(0);
		}
		return this.usage;
	}

	public boolean hasPermission(U user, C channel, G guild)
	{
		return true;
	}

	public boolean isChannelValid(C channel)
	{
		return true;
	}

	public boolean showInHelp()
	{
		return true;
	}

	public abstract String onCommand(String[] splitMessage, String messageString, M messageObject, U user, C channel, G guild);

	public void reply(M message, String replyMessage)
	{
		commands.reply(message, replyMessage);
	}
}
