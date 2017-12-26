import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;

/**Command for subbing into a game. Must be used in command channel. Player must be linked and not already playing a game.
 * <p>
 * prefers to sub a player back into their own spot in the game if one of the sub requests is for them. Otherwise subs the player into places in the order that the sub requests were made.
 * @author cameron
 * @see SubManager#subPlayerIntoGame(PlayerObject)
 */
public class CommandSub implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandSub
	 */
	@Command(aliases = {"!sub"}, description = "sub into the first open sub position")
	public void onCommand(IMessage message, String[] args)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		PlayerObject player = DiscordBot.players.getObject(message.getAuthor());
		if(player==null)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "You must be linked to sub into a game " + message.getAuthor().getDisplayName(message.getGuild()) + "! Use **!link KAGUsernameHere** to get started or **!linkhelp** for more information");
			return;
		}
		
		//check there is actually games running
		if(!gather.hasRunningGames())
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "There is currently **no games** running " + message.getAuthor().getDisplayName(message.getGuild()) + "!");
		}
		
		//check there isnt already a sub request for the player trying to sub in
		if(gather.substitutions.removeSubRequest(player))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), message.getAuthor().getDisplayName(message.getGuild()) + " has **subbed back** into their game!");
			return;
		}
		
		//check they arent already playing
		if(gather.isInGame(player))
		{
			//check if there is sub votes for this player already
			if(gather.substitutions.getNumSubVotesForPlayer(player)>0)
			{
				gather.substitutions.removeSubVotes(player);
				DiscordBot.reply(message, "all current sub votes for you have been **cleared**!");
				return;
			}
			else
			{
				DiscordBot.sendMessage(gather.getCommandChannel(), "You cannot sub into a game when you are **already playing** "+message.getAuthor().getDisplayName(message.getGuild()) + "!");
				return;
			}
		}
		
		int gameId = -1;
		try
		{
			gameId = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException|ArrayIndexOutOfBoundsException e)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "**Invalid** command format or number "+message.getAuthor().getDisplayName(message.getGuild())+"! usage is **!sub gameID**");
			return;
		}
		
		//check the game exists
		if(gather.getRunningGame(gameId)==null)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "There is **no current game** with that id " + message.getAuthor().getDisplayName(message.getGuild()) + "!");
			return;
		}
		
		SubstitutionObject returnObj = gather.substitutions.subPlayerIntoGame(player, gameId);
		
		if(returnObj == null)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "There are **no sub spaces** available for game #" + gameId + " " + message.getAuthor().getDisplayName(message.getGuild()) + "!");
			return;
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
			Discord4J.LOGGER.info(message.getAuthor().getDisplayName(message.getGuild())+" has subbed into game #"+returnObj.game.getGameID()+" for "+returnObj.playerToBeReplaced.toString());
			DiscordBot.sendMessage(gather.getCommandChannel(), message.getAuthor().getDisplayName(message.getGuild()) + " has **replaced** " + returnObj.playerToBeReplaced.toString() + " in game #"+returnObj.game.getGameID()+" on **" + teamString + "** Team!");
			gather.remFromQueue(message.getAuthor());
		}
		return;
	}
}