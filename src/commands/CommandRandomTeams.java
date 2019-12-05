package commands;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.DiscordBot;
import core.GatherObject;
import core.PlayerObject;
import core.StatsObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

/**Admin only command for adding random players from database to the queue. Useful for testing. Must be used in command channel. 
 * Retrieves a random list of players from the gather database and adds them to the queue. The number of players retrieved is equal to the queue size. 
 * @author epsilon
 *
 */
public class CommandRandomTeams extends Command<Message, Member, Channel>
{
	static final Logger LOGGER = LoggerFactory.getLogger(CommandRandomTeams.class);

	public CommandRandomTeams(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("randomteams", "randomiseteams", "randteams"), "Admin only - get random players and add them to the queue", "usage");
	}

	@Override
	public boolean isChannelValid(Channel channel) {
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		else return true;
	}

	@Override
	public boolean hasPermission(Member member, Channel channel)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		return gather.isAdmin(member);
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return null;

		gather.clearQueue();		
		List<StatsObject> stats = DiscordBot.database.getRandomPlayers(gather.getMaxQueueSize());
		for(StatsObject stat : stats)
		{
			PlayerObject player = DiscordBot.players.getOrCreatePlayerObject(member);
			int addReturnVal = gather.addToQueue(player);
			switch(addReturnVal)
			{
				case 1:
					DiscordBot.sendMessage(gather.getCommandChannel(), gather.fullUserString(player.getDiscordUserInfo())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
					LOGGER.info("Adding player to queue: "+player.getDiscordUserInfo().getDisplayName());
					continue;
				case 2:
					DiscordBot.sendMessage(gather.getCommandChannel(), gather.fullUserString(player.getDiscordUserInfo())+" **added** to the queue! ("+gather.numPlayersInQueue()+"/"+gather.getMaxQueueSize()+")");
					LOGGER.info("Adding player to queue: "+player.getDiscordUserInfo().getDisplayName());
					gather.startGame();
					continue;
			}
		}
		return null;
	}
}