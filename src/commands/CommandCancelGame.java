package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherObject;
import core.PlayerObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**
 * Command players can use to cancel a game if something went wrong. Must be used in command channel. 
 * Admins can also use this command to cancel a game they are not in. 
 * Calls GatherObject.clearGames()
 * @author cameron
 * @see GatherObject#clearGames()
 */
public class CommandCancelGame extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandCancelGame(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("cancelgame", "cancel"), "Vote to cancel the game you are currently playing");
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

		PlayerObject player = DiscordBot.players.getOrCreatePlayerObject(user);
		if(player==null)
		{
			return "You must be linked to do that " + user.getDisplayName(guild) + "! Use **!link KAGUsernameHere** to get started or **!linkhelp** for more information";
		}
		gather.addCancelVote(player);
		return null;
	}
}