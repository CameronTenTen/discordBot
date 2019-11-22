package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherObject;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Admin only command for subbing a player from the game. Must be used in command channel. 
 * Gets a list of all the mentions in the command and subs those players from the game if they are in it. 
 * @author epsilon
 *
 */
public class CommandForceSub extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandForceSub(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("forcesub"), "Admin only - sub out a user from the game", "forcesub @user...");
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
		for(IUser mentionedUser : mentions)
		{
			if(1==gather.substitutions.addSubRequest(mentionedUser, gather.getPlayersGame(mentionedUser)))
			{
				Discord4J.LOGGER.info("sub requested for: "+mentionedUser.getDisplayName(guild));
				return "**Sub request** added for " + mentionedUser.mention() + " use **!sub "+gather.getPlayersGame(mentionedUser).getGameID()+"** to sub into their place! ("+gather.getQueueRole().mention()+")";
			}
		}
		return null;
	}
}