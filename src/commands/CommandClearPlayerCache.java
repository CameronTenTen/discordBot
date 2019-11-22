package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherObject;
import core.PlayerObjectManager;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Admin command for clearing the current player cache of all unused player objects. Must be used in command channel. 
 * @author cameron
 * @see PlayerObjectManager#clearPlayerCache()
 */
public class CommandClearPlayerCache extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandClearPlayerCache(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("clearPlayerCache", "clear_player_cache"), "Admin only - clear current player cache of all unused player objects");
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

		List<IUser> mentions = messageObject.getMentions();
		if(!mentions.isEmpty())
		{
			for(IUser mention : mentions)
			{
				if(DiscordBot.players.clearPlayerCache(mention.getLongID()))
				{
					return "Removed user from cache: " + gather.fullUserString(mention);
				}
				else
				{
					return "Failed to remove user from cache: " + gather.fullUserString(mention);
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