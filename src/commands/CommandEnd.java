package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherGame;
import core.GatherObject;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;

/**
 * Admin only command for moving all players out of the red and blue voice channels into the general channel. Must be used in command channel. 
 * Calls GatherObject.movePlayersOutOfTeamRooms()
 * @author cameron
 * @see GatherObject#movePlayersOutOfTeamRooms()
 */
public class CommandEnd extends Command<Message, Member, Channel>
{
	public CommandEnd(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("end"), "Admin only - move users from team chat to general chat", "end gameID");
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

		if(splitMessage.length<2)
		{
			return member.getMention() + ", not enough arguments, command usage is !end gameID";
		}
		int gameId = -1;
		try
		{
			gameId = Integer.parseInt(splitMessage[1]);
		}
		catch (NumberFormatException|ArrayIndexOutOfBoundsException e)
		{
			return member.getMention() + ", an error occured parsing the game id, command usage is !end gameID";
		}
		GatherGame game = gather.getRunningGame(gameId);
		if(game == null)
		{
			return "No game found with the id "+gameId+"!";
		}
		else
		{
			this.reply(messageObject, "Moving players out of team rooms");
			gather.movePlayersOutOfTeamRooms(game);
		}
		return null;
	}
}