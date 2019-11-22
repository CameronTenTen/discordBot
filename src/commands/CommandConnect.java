package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Admin command for making the discord bot connect to one or all disconnected KAG servers(only tries if it knows that it is disconnected). Must be used in command channel. 
 * @author cameron
 * @see GatherObject#connectKAGServers()
 * @see GatherObject#connectToServer(serverId)
 */
public class CommandConnect extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandConnect(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("connect","conn","con"), "Admin only - connect to a kag server, or all kag servers", "connect serverID/ALL");
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

		if(splitMessage.length<=1)
		{
			return "Insufficient parameters, usage is: !connect serverID or !connect ALL";
		}
		else
		{
			String serverID = splitMessage[1];
			if("ALL".equalsIgnoreCase(serverID)) {
				this.reply(messageObject, "Connecting to all servers:");
				gather.connectKAGServers(false);
				return null;
			}
			else
			{
				if(!gather.connectToServer(serverID))
				{
					return "Could not find server id \""+serverID+"\", usage is !connect serverID or !connect ALL";
				}
				return null;
			}
		}
	}
}