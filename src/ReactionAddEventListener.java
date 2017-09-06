import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;

public class ReactionAddEventListener implements IListener<ReactionAddEvent>
{

	@Override
	public void handle(ReactionAddEvent event)
	{
		DiscordBot.bot.onReactionAdded(event);
			
	}

}