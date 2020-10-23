package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherObject;
import core.PlayerObjectManager;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;

/**Admin command for clearing the current player cache of all unused player objects. Must be used in command channel. 
 * @author cameron
 * @see PlayerObjectManager#clearPlayerCache()
 */
public class CommandClearPlayerCache extends Command<Message, Member, Channel>
{
	public CommandClearPlayerCache(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("clearplayercache", "clear_player_cache"), "Admin only - clear current player cache of all unused player objects");
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

		List<User> mentions = messageObject.getUserMentions().collectList().block();
		if(!mentions.isEmpty())
		{
			for(User mention : mentions)
			{
				String playerString = gather.playerString(DiscordBot.players.getIfExists(mention.getId()));
				if(DiscordBot.players.clearPlayerCache(mention.getId()))
				{
					return "Removed user from cache: " + playerString;
				}
				else
				{
					return "Failed to remove user from cache: " + playerString;
				}
			}
			//the code never reaches this point but the compiler thinks it might
			return null;
		}
		else
		{
			DiscordBot.players.clearPlayerCache(null);
			return "Cleared Cache: " + DiscordBot.players.listPlayerCache();
		}
	}
}