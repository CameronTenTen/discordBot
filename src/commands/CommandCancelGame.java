package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import core.PlayerObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

/**
 * Command players can use to cancel a game if something went wrong. Must be used in command channel. 
 * Admins can also use this command to cancel a game they are not in. 
 * Calls GatherObject.clearGames()
 * @author cameron
 * @see GatherObject#clearGames()
 */
public class CommandCancelGame extends Command<Message, Member, Channel>
{
	public CommandCancelGame(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("cancelgame", "cancel"), "Vote to cancel the game you are currently playing");
	}

	@Override
	public boolean isChannelValid(Channel channel) {
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return false;
		else return true;
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(channel);
		if(gather==null) return null;

		PlayerObject player = DiscordBot.players.getOrCreatePlayerObject(member);
		if(player==null)
		{
			return "You must be linked to do that " + member.getDisplayName() + "! Use **!link KAGUsernameHere** to get started or **!linkhelp** for more information";
		}
		gather.addCancelVote(player);
		return null;
	}
}