import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent;
import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.StatusType;

public class PresenceEventListener implements IListener<PresenceUpdateEvent>
{

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