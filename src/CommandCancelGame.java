import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Command players can use to cancel a game if something went wrong. Must be used in command channel. 
 * Admins can also use this command to cancel a game they are not in. 
 * Calls GatherObject.clearGames()
 * @author cameron
 * @see GatherObject#clearGames()
 */
public class CommandCancelGame implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandClearGames
	 */
	@Command(aliases = {"!cancelgame", "!cancel"}, description = "Vote to cancel the game you are currently playing")
	public void onCommand(IMessage message, String[] args)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		PlayerObject player = DiscordBot.players.getOrCreatePlayerObject(message.getAuthor());
		if(player==null)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "You must be linked to do that " + message.getAuthor().getDisplayName(message.getGuild()) + "! Use **!link KAGUsernameHere** to get started or **!linkhelp** for more information");
			return;
		}
		if(args.length<=0)
		{
			gather.addCancelVote(player);
			return;
		}
		else
		{
			int matchId = -1;
			try
			{
				matchId = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException|ArrayIndexOutOfBoundsException e)
			{
				e.printStackTrace();
				DiscordBot.sendMessage(gather.getCommandChannel(), "An error occured parsing the game id, did you type the command correctly "+message.getAuthor().getDisplayName(message.getGuild())+"?");
				return;
			}
			
			if(gather.endGame(matchId, -2))
			{
				DiscordBot.sendMessage(gather.getCommandChannel(), "Game #"+matchId+" has been canceled!", true);
				Discord4J.LOGGER.info("Game cancelled: "+matchId);
				return;
			}
			else
			{
				DiscordBot.sendMessage(gather.getCommandChannel(), "An error occured ending the game, does that match exist "+message.getAuthor().getDisplayName(message.getGuild())+"?");
				return;
			}
		}
	}
}