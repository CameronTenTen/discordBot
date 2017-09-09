import java.util.Iterator;
import java.util.List;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;

public class ReadyEventListener implements IListener<ReadyEvent>
{

	@Override
	public void handle(ReadyEvent event)
	{
		List<IGuild> guilds = event.getClient().getGuilds();
		if(guilds != null && guilds.size()>0)
		{
			for(IGuild guild : guilds)
			{
				DiscordBot.addGuild(guild);
			}
		}
		

		//just get the first gather object for now to set the playing text
		//TODO: playing text wont really work if there was ever multiple servers
		Iterator<GatherObject> itr = DiscordBot.gatherObjects.iterator();
		GatherObject gather = itr.next();
		if(gather == null) return;
		DiscordBot.setPlayingText(gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+" in queue");
		DiscordBot.setChannelCaption(gather.getGuild() , gather.numPlayersInQueue()+"-in-q");
		
		
	}

}