package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherObject;
import core.PlayerObject;
import core.StatsObject;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Admin only command for adding random players from database to the queue. Useful for testing. Must be used in command channel. 
 * Retrieves a random list of players from the gather database and adds them to the queue. The number of players retrieved is equal to the queue size. 
 * @author epsilon
 *
 */
public class CommandRandomTeams extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandRandomTeams(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("randomteams", "randomiseteams", "randteams"), "Admin only - get random players and add them to the queue", "usage");
	}

	@Override
	public boolean isChannelValid(IChannel channel) {
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		else return true;
	}

	@Override
	public boolean hasPermission(IUser user, IChannel channel, IGuild guild)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		return gather.isAdmin(user);
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return null;

		gather.clearQueue();		
		List<StatsObject> stats = DiscordBot.database.getRandomPlayers(gather.getMaxQueueSize());
		for(StatsObject stat : stats)
		{
			PlayerObject player = DiscordBot.players.getOrCreatePlayerObject(stat.discordid);
			int addReturnVal = gather.addToQueue(player);
			switch(addReturnVal)
			{
				case 1:
					DiscordBot.sendMessage(gather.getCommandChannel(), gather.fullUserString(player.getDiscordUserInfo())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
					Discord4J.LOGGER.info("Adding player to queue: "+player.getDiscordUserInfo().getDisplayName(guild));
					continue;
				case 2:
					DiscordBot.sendMessage(gather.getCommandChannel(), gather.fullUserString(player.getDiscordUserInfo())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
					Discord4J.LOGGER.info("Adding player to queue: "+player.getDiscordUserInfo().getDisplayName(guild));
					gather.startGame();
					continue;
			}
		}
		return null;
	}
}