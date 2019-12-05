package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherGame;
import core.GatherObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;

/**Admin only command for sending all players in the gather general voice channel into their team rooms. Must be used in command channel. 
 * @author cameron
 *@see GatherObject#movePlayersIntoTeamRooms()
 */
public class CommandStart extends Command<Message, Member, Channel>
{
	public CommandStart(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("start"), "Admin only - move users from general chat to team rooms", "start gameID");
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
			return member.getMention() + ", not enough arguments, command usage is !start gameID";
		}
		int gameId = -1;
		try
		{
			gameId = Integer.parseInt(splitMessage[1]);
		}
		catch (NumberFormatException|ArrayIndexOutOfBoundsException e)
		{
			return member.getMention() + ", an error occured parsing the game id, command usage is !start gameID";
		}
		GatherGame game = gather.getRunningGame(gameId);
		if(game == null)
		{
			return "No game found with the id "+gameId+"!";
		}
		else
		{
			this.reply(messageObject, "Moving players in to team rooms");
			gather.movePlayersIntoTeamRooms(game);
		}
		return null;
	}
}