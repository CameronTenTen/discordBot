import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;

/**Listener for when the discord bot is ready for various setup code. 
 * @author cameron
 * @see https://jitpack.io/com/github/austinv11/Discord4j/2.8.4/javadoc/sx/blah/discord/api/events/IListener.html
 */
public class ReadyEventListener implements IListener<ReadyEvent>
{
	/**The function that is called when the bot is ready
	 * @param event
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #ReadyEventListener
	 */
	@Override
	public void handle(ReadyEvent event)
	{
		/*List<IGuild> guilds = event.getClient().getGuilds();
		if(guilds != null && guilds.size()>0)
		{
			for(IGuild guild : guilds)
			{
				DiscordBot.addGuild(guild);
			}
		}*/
		try {
			Gson gson = new Gson();
			JsonReader reader;
			reader = new JsonReader(new FileReader("servers.json"));
			GatherObject obj = gson.fromJson(reader, GatherObject.class);
			obj.setDiscordObjects();
			DiscordBot.gatherObjects.add(obj);
			//TODO: this step is unneccessary if the config can specify a case insensitive duplicate checked key map
			obj.initialiseServers();
			obj.connectKAGServers(true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		//just get the first gather object for now to set the playing text
		//TODO: playing text wont really work if there was ever multiple servers
		Iterator<GatherObject> itr = DiscordBot.gatherObjects.iterator();
		GatherObject gather = itr.next();
		if(gather == null) return;
		gather.clearQueueRole();
		gather.updateChannelCaption();
	}

}