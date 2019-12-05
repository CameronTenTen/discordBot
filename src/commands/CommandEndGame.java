package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherGame;
import core.GatherObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

/**Admin only command for ending the current game. Must be used in command channel. 
 * <p>
 * Parses the first argument as the match id, and the second as the team to credit the win to. Valid values are red, blue, draw or cancel.
 * @author cameron
 *
 */
public class CommandEndGame extends Command<Message, Member, Channel>
{
	public CommandEndGame(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("endgame", "givewin"), "Admin only - end the specified game, crediting the win to a particular team, or no team", "endgame gameID red/blue/draw/cancel");
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

		if(splitMessage.length<3)
		{
			return member.getMention() + ", not enough arguments, command usage is !endgame gameID red/blue/draw/cancel!";
		}
		int gameId = -1;
		try
		{
			gameId = Integer.parseInt(splitMessage[1]);
		}
		catch (NumberFormatException|ArrayIndexOutOfBoundsException e)
		{
			return member.getMention() + ", an error occured parsing the game id, command usage is !endgame gameID red/blue/draw/cancel!";
		}
		GatherGame game = gather.getRunningGame(gameId);
		if(game == null)
		{
			return "No game found with the id "+gameId+"!";
		}
		else
		{
			String team = splitMessage[2].toLowerCase();
			switch(team)
			{
				case "blue":
					gather.endGame(game, 0);
					break;
				case "red":
					gather.endGame(game, 1);
					break;
				case "draw":
					gather.endGame(game, -1);
					break;
				case "cancel":
					gather.endGame(game, -2);
					break;
				default:
					return member.getMention() + ", team did not match any known values! (red/blue/draw/cancel)";
			}
		}
		return null;
	}
}