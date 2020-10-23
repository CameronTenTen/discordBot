package commands;

import java.util.Arrays;

import core.DiscordBot;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;

/**Instantiation of the Command manager class for discord4J
 * @author cameron
 *
 */
public class Discord4JCommands extends Commands<Message, Member, Channel>
{
	public Discord4JCommands()
	{
		super();
		this.setPrefixes(Arrays.asList("!"));

	}

	/**Gets called whenever a command triggers a response
	 * @param message the message to reply to
	 * @param replyMessage the message to reply with
	 * @see commands.Commands#reply(java.lang.Object, java.lang.String)
	 */
	@Override
	public void reply(Message message, String replyMessage)
	{
		DiscordBot.sendMessage(message.getChannel().block(), replyMessage);
	}

	/**To be called by the event dispatcher, converts the event into the require onMessage() call
	 * @param event the Discord4J message create event object
	 */
	public void onMessageReceivedEvent(final MessageCreateEvent event)
	{
		this.onMessage(event.getMessage().getContent(), event.getMessage(), event.getMember().orElse(null), event.getMessage().getChannel().block());
	}

	/**When the someone uses a command they don't have permission for, want to give them some feedback
	 * @param messageObject the message they sent
	 * @param member the member that sent it
	 * @param channel the channel it was sent in
	 * @see commands.Commands#onProhibitedCommandUsed(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void onProhibitedCommandUsed(Message messageObject, Member member, Channel channel)
	{
		this.reply(messageObject, "Only **admins** can do that "+member.getDisplayName()+"!");
	}
}
