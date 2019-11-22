package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**
 * Admin only command for moving all players out of the red and blue voice channels into the general channel. Must be used in command channel. 
 * Calls GatherObject.movePlayersOutOfTeamRooms()
 * @author cameron
 * @see GatherObject#movePlayersOutOfTeamRooms()
 */
public class CommandEnd extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandEnd(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("end"), "Admin only - move users from team chat to general chat");
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

		this.reply(messageObject, "Moving players out of team rooms");
		gather.movePlayersOutOfTeamRooms();
		return null;
	}
}