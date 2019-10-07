import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;

/**Listener for when a user leaves a guild
 * @author cameron
 * @see UserLeaveEventListener#handle(UserLeaveEvent)
 */
public class UserLeaveEventListener implements IListener<UserLeaveEvent>
{
	/**The function that is called when the listener is triggered
	 * @param event the event object, contains the guild and user associated with this event. 
	 * @see sx.blah.discord.api.events.IListener#handle(sx.blah.discord.api.events.Event)
	 */
	@Override
	public void handle(UserLeaveEvent event)
	{
		DiscordBot.userLeftGuild(event.getGuild(), event.getUser());
	}

}