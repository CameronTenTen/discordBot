import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent;
import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.StatusType;

/**Listener for when a user changes presence state (offline/away/online). 
 * @author cameron
 * @see PresenceEventListener#handle(PresenceUpdateEvent)
 */
public class PresenceEventListener implements IListener<PresenceUpdateEvent>
{

	/**The function that is called when the listener is triggered
	 * @param event the event object, contains both new and old presence states
	 * @see sx.blah.discord.api.events.IListener#handle(sx.blah.discord.api.events.Event)
	 */
	@Override
	public void handle(PresenceUpdateEvent event)
	{
		IPresence pres = event.getNewPresence();
		if(pres.getStatus()==StatusType.OFFLINE)
		{
			DiscordBot.userWentOffline(event.getUser());
		}
			
	}

}