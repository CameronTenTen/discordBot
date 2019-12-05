package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

/**Admin command for making the discord bot connect to one or all disconnected KAG servers(only tries if it knows that it is disconnected). Must be used in command channel. 
 * @author cameron
 * @see GatherObject#connectKAGServers()
 * @see GatherObject#connectToServer(serverId)
 */
public class CommandConnect extends Command<Message, Member, Channel>
{
	public CommandConnect(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("connect","conn","con"), "Admin only - connect to a kag server, or all kag servers", "connect serverID/ALL");
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