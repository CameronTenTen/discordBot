package commands;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.DiscordBot;
import core.GatherGame;
import core.GatherObject;
import core.PlayerObject;
import core.SubManager;
import core.SubstitutionObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

/**Command for subbing into a game. Must be used in command channel. Player must be linked and not already playing a game.
 * <p>
 * prefers to sub a player back into their own spot in the game if one of the sub requests is for them. Otherwise subs the player into places in the order that the sub requests were made.
 * @author cameron
 * @see SubManager#subPlayerIntoGame(PlayerObject)
 */
public class CommandSub extends Command<Message, Member, Channel>
{
	static final Logger LOGGER = LoggerFactory.getLogger(CommandSub.class);

	public CommandSub(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("sub"), "Sub into the first open sub position", "sub gameID");
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
			return "You must be linked to sub into a game " + member.getDisplayName() + "! Use **!link KAGUsernameHere** to get started or **!linkhelp** for more information";
		}
		
		//check there is actually games running
		if(!gather.hasRunningGames())
		{
			return "There is currently **no games** running " + member.getDisplayName() + "!";
		}
		
		//check there isnt already a sub request for the player trying to sub in
		if(gather.substitutions.removeSubRequest(player))
		{
			return gather.fullUserString(member) + " has **subbed back** into their game!";
		}
		
		//check they arent already playing
		if(gather.isInGame(player))
		{
			//check if there is sub votes for this player already
			if(gather.substitutions.getNumSubVotesForPlayer(player)>0)
			{
				gather.substitutions.removeSubVotes(player);
				return "all current sub votes for you have been **cleared**!";
			}
			else
			{
				return "You cannot sub into a game when you are **already playing** "+member.getDisplayName() + "!";
			}
		}
		
		int gameId = -1;
		
		if(splitMessage.length<=1)
		{
			//if no args passed, and there is only one game to sub into, then we can assume they want to get into that game
			List<GatherGame> games = gather.getRunningGames();
			if(games.size()>1)
			{
				//you must specify which game you want to sub into
				return "You must specify the game you want to sub for "+member.getDisplayName()+"! usage is **!sub gameID**";
			}
			else if(games.size()==1)
			{
				gameId = games.get(0).getGameID();
			}
			else
			{
				//no games to sub into
				return "There are **no games** to sub for "+member.getDisplayName()+"!";
			}
		}
		else
		{
			try
			{
				gameId = Integer.parseInt(splitMessage[1]);
			}
			catch (NumberFormatException|ArrayIndexOutOfBoundsException e)
			{
				return "**Invalid** command format or number "+member.getDisplayName()+"! usage is **!sub gameID**";
			}
			
			//check the game exists
			if(gather.getRunningGame(gameId)==null)
			{
				return "There is **no current game** with that id " + member.getDisplayName() + "!";
			}
		}
		
		SubstitutionObject returnObj = gather.substitutions.subPlayerIntoGame(player, gameId);
		
		if(returnObj == null)
		{
			return "There are **no sub spaces** available for game #" + gameId + " " + member.getDisplayName() + "!";
		}
		else
		{
			int team = returnObj.game.getPlayerTeam(returnObj.playerSubbingIn);
			String teamString = "";
			if(team == 0)
			{
				teamString = "Blue";
			}
			else if (team == 1)
			{
				teamString = "Red";
			}
			else
			{
				teamString = "ERROR";
			}
			LOGGER.info(member.getDisplayName()+" has subbed into game #"+returnObj.game.getGameID()+" for "+returnObj.playerToBeReplaced.toString());
			DiscordBot.sendMessage(gather.getCommandChannel(), returnObj.playerSubbingIn.toString() + " has **replaced** " + returnObj.playerToBeReplaced.toString() + " in game #"+returnObj.game.getGameID()+" on **" + teamString + "** Team!");
			gather.remFromQueue(member);
			return null;
		}
	}
}