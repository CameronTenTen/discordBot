package commands;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.DiscordBot;
import core.GatherObject;
import core.PlayerObject;
import core.SubManager;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;

/**Command for requesting a substitute for a player. If the command is used with no arguments, a sub is requested for yourself. 
 * To add a sub request vote for someone else, the command must be used with a user mention of another player in the game.
 * <p>
 * When the substitution of a player gets enough votes, a sub request will be added. Each player may only vote to sub each other player once. 
 * <p>
 * If a player mentions themself in the message, the command behaves as if there was no arguments. Must be used in command channel. 
 * @author cameron
 * @see SubManager
 */
public class CommandRsub extends Command<Message, Member, Channel>
{
	static final Logger LOGGER = LoggerFactory.getLogger(CommandRsub.class);

	public CommandRsub(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("rsub"), "Request a sub, supply no arguments to sub yourself, or specify another user with a mention", "rsub <@user>");
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
			return "You must be linked to request a sub" + member.getDisplayName() + "! Use !link to get started or !linkhelp for more information";
		}

		List<User> mentions = messageObject.getUserMentions().collectList().block();
		if(splitMessage.length <= 1 || mentions.contains(member))
		{
			//player wants to sub out themselves
			int returnVal = gather.substitutions.addSubRequest(member, gather.getPlayersGame(member));
			switch(returnVal)
			{
			case -1:
				return "You cannot request a sub for yourself when you are **not in a game** " + member.getDisplayName() + "!";
			case 0:
				return "A sub has **already** been requested for you " + member.getDisplayName() + "!";
			case 1:
				LOGGER.info("sub requested for: "+member.getDisplayName());
				return "**Sub request** added for " + member.getMention() + " use **!sub "+gather.getPlayersGame(member).getGameID()+"** to sub into their place! ("+gather.getQueueRole().getMention()+")";
			}
			//never gets to this return (the switch covers all return values), but the compiler thinks it might
			return null;
		}
		else
		{
			if(mentions.size()==0)
			{
				return "Incorrect command usage " + member.getDisplayName() +"! usage is !rsub with no arguments if you want to request a sub for yourself or !rsub @user if you want to request a sub for someone else";
			}
			//only use the first mention
			int returnVal = gather.substitutions.addSubVote(mentions.get(0), member);
			switch(returnVal)
			{
			case -1:
			case -2:
			case -3:
				return "You and the player you are voting for must be in the **same game** " + member.getDisplayName() + "!";
			case -4:
				return DiscordBot.players.getIfExists(mentions.get(0)) + " is **already** being subbed " + member.getDisplayName() + "!";
			case -5:
				return "You have **already voted** to sub this player " + member.getDisplayName() + "!";
			case 0:
				LOGGER.info("sub requested for: "+member.getDisplayName());
				return "**Sub request** added for " + mentions.get(0) + " use **!sub "+gather.getPlayersGame(mentions.get(0)).getGameID()+"** to sub into their place! ("+gather.getQueueRole().getMention()+")";
			}
			//gets here if returnVal is greater than 0 which means the sub vote was added and the number is the vote count
			return member.getMention() + " your sub vote has been **counted** (" + returnVal +"/"+ gather.substitutions.getSubVotesRequired() +") "+mentions.get(0) + " can clear the vote using **!sub**";
		}
	}
}
