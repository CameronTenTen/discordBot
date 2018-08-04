import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;

/**Admin only command for adding random players from database to the queue. Useful for testing. Must be used in command channel. 
 * Retrieves a random list of players from the gather database and adds them to the queue. The number of players retrieved is equal to the queue size. 
 * @author epsilon
 *
 */
public class CommandRandomTeams implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandRandomTeams
	 */
	@Command(aliases = {"!randomteams","!randomiseteams","!randteams"}, description = "Admin only - get random players and add them to the queue")
	public void onCommand(IMessage message)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only admins can do that "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		
		}
		
		gather.clearQueue();		
		List<StatsObject> stats = DiscordBot.database.getRandomPlayers(gather.getMaxQueueSize());
		for(StatsObject stat : stats)
		{
			PlayerObject player = DiscordBot.players.getObject(stat.discordid);
			int addReturnVal = gather.addToQueue(player);
			switch(addReturnVal)
			{
				case 1:
					DiscordBot.sendMessage(gather.getCommandChannel(), gather.fullUserString(player.getDiscordUserInfo())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
					gather.setNotInterested(player.getDiscordUserInfo());
					Discord4J.LOGGER.info("Adding player to queue: "+player.getDiscordUserInfo().getDisplayName(message.getGuild()));
					continue;
				case 2:
					DiscordBot.sendMessage(gather.getCommandChannel(), gather.fullUserString(player.getDiscordUserInfo())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
					gather.setNotInterested(player.getDiscordUserInfo());
					Discord4J.LOGGER.info("Adding player to queue: "+player.getDiscordUserInfo().getDisplayName(message.getGuild()));
					gather.startGame();
					continue;
			}
		}
		return;
	}
}