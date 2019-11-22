package commands;

import java.util.Arrays;

import core.DiscordBot;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

//TODO: document this
public class Discord4JCommands extends Commands<IMessage, IUser, IChannel, IGuild>
{
	public Discord4JCommands()
	{
		super();
		this.setPrefixes(Arrays.asList("!"));
		
	}

	@Override
	public void reply(IMessage message, String replyMessage)
	{
		DiscordBot.sendMessage(message.getChannel(), replyMessage);
	}

	public void onMessageReceivedEvent(final MessageReceivedEvent event)
	{
		this.onMessage(event.getMessage().getContent(), event.getMessage(), event.getAuthor(), event.getChannel(), event.getGuild());
	}

	@Override
	public void onProhibitedCommandUsed(IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		this.reply(messageObject, "Only **admins** can do that "+user.getDisplayName(guild)+"!");
	}
}