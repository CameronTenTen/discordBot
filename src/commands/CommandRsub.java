package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherObject;
import core.PlayerObject;
import core.SubManager;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Command for requesting a substitute for a player. If the command is used with no arguments, a sub is requested for yourself. 
 * To add a sub request vote for someone else, the command must be used with a user mention of another player in the game.
 * <p>
 * When the substitution of a player gets enough votes, a sub request will be added. Each player may only vote to sub each other player once. 
 * <p>
 * If a player mentions themself in the message, the command behaves as if there was no arguments. Must be used in command channel. 
 * @author cameron
 * @see SubManager
 */
public class CommandRsub extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandRsub(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("rsub"), "Request a sub, supply no arguments to sub yourself, or specify another user with a mention", "rsub <@user>");
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
			return "You must be linked to request a sub" + user.getDisplayName(guild) + "! Use !link to get started or !linkhelp for more information";
		}

		List<IUser> mentions = messageObject.getMentions();
		if(splitMessage.length <= 1 || mentions.contains(user))
		{
			//player wants to sub out themselves
			int returnVal = gather.substitutions.addSubRequest(user, gather.getPlayersGame(user));
			switch(returnVal)
			{
			case -1:
				return "You cannot request a sub for yourself when you are **not in a game** " + user.getDisplayName(guild) + "!";
			case 0:
				return "A sub has **already** been requested for you " + user.getDisplayName(guild) + "!";
			case 1:
				Discord4J.LOGGER.info("sub requested for: "+user.getDisplayName(guild));
				return "**Sub request** added for " + user.mention() + " use **!sub "+gather.getPlayersGame(user).getGameID()+"** to sub into their place! ("+gather.getQueueRole().mention()+")";
			}
			//never gets to this return (the switch covers all return values), but the compiler thinks it might
			return null;
		}
		else
		{
			if(mentions.size()==0)
			{
				return "Incorrect command usage " + user.getDisplayName(guild) +"! usage is !rsub with no arguments if you want to request a sub for yourself or !rsub @user if you want to request a sub for someone else";
			}
			//only use the first mention
			int returnVal = gather.substitutions.addSubVote(mentions.get(0), user);
			switch(returnVal)
			{
			case -1:
			case -2:
			case -3:
				return "You and the player you are voting for must be in the **same game** " + user.getDisplayName(guild) + "!";
			case -4:
				return mentions.get(0).getDisplayName(guild) + " is **already** being subbed " + user.getDisplayName(guild) + "!";
			case -5:
				return "You have **already voted** to sub this player " + user.getDisplayName(guild) + "!";
			case 0:
				Discord4J.LOGGER.info("sub requested for: "+user.getDisplayName(guild));
				return "**Sub request** added for " + mentions.get(0) + " use **!sub "+gather.getPlayersGame(mentions.get(0)).getGameID()+"** to sub into their place! ("+gather.getQueueRole().mention()+")";
			}
			//gets here if returnVal is greater than 0 which means the sub vote was added and the number is the vote count
			return user.mention() + " your sub vote has been **counted** (" + returnVal +"/"+ gather.substitutions.getSubVotesRequired() +") "+mentions.get(0) + " can clear the vote using **!sub**";
		}
	}
}
