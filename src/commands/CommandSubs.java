package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import core.SubManager;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Command for checking the current sub requests. Must be used in command channel. 
 * @author cameron
 * @see SubManager#toString()
 */
public class CommandSubs extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandSubs(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("subs"), "Check current sub requests");
	}

	@Override
	public boolean isChannelValid(IChannel channel) {
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		else return true;
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return null;
		
		String currentSubs = gather.substitutions.toString();
		if(!currentSubs.isEmpty())
		{
			return "There is currently sub requests for: "+currentSubs;
		}
		else
		{
			return "No subs currently requested";
		}
	}
}