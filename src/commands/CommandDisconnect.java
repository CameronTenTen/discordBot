package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Admin command for making the discord bot to disconnect from one or all KAG servers. If the bot thinks it is already disconnected, it will say so, but still try to disconnect anyway. 
 * Must be used in command channel. 
 * @author cameron
 * @see GatherObject#disconnectKAGServers()
 * @see GatherObject#disconnectFromServer(serverId)
 */
public class CommandDisconnect extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandDisconnect(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("disconnect", "disconn", "discon"), "Admin only - disconnect from a kag server, or all kag servers", "disconnect serverID/ALL");
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
			return "Insufficient parameters, usage is: !disconnect serverID or !disconnect ALL";
		}
		else
		{
			String serverID = splitMessage[1];
			if("ALL".equalsIgnoreCase(serverID)) {
				this.reply(messageObject, "Disconnecting from all servers:");
				gather.disconnectKAGServers();
				return null;
			}
			else
			{
				if(!gather.disconnectFromServer(serverID))
				{
					return "Could not find server id \""+serverID+"\", usage is !disconnect serverID or !disconnect ALL";
				}
				return null;
			}
		}
	}
}