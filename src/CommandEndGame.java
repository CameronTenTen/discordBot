import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Admin only command for ending the current game. Must be used in command channel. 
 * <p>
 * Parses the first argument as the match id, and the second as the team to credit the win to. Valid values are red, blue, draw or cancel.
 * @author cameron
 *
 */
public class CommandEndGame implements CommandExecutor
{

	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandEndGame
	 */
	@Command(aliases = {"!endgame", "!givewin"}, description = "Admin only - end the specified game, crediting the win to a particular team, or no team")
	public void onCommand(IMessage message, String[] args)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that" + " "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		
		}
		if(args.length<2)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), message.getAuthor().mention() + ", not enough arguments, command usage is !endgame matchid red/blue/draw/cancel!");
			return;
		}
		int matchId = -1;
		try
		{
			matchId = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException|ArrayIndexOutOfBoundsException e)
		{
			e.printStackTrace();
			DiscordBot.sendMessage(gather.getCommandChannel(), message.getAuthor().mention() + ", an error occured parsing the game id, command usage is !endgame matchid red/blue/draw/cancel!");
			return;
		}
		GatherGame game = gather.getRunningGame(matchId);
		if(game == null)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "No game found with the id "+matchId+"!");
			return;
		}
		else
		{
			String team = args[1].toLowerCase();
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
					DiscordBot.sendMessage(gather.getCommandChannel(), message.getAuthor().mention() + ", team did not match any known values! (red/blue/draw/cancel)");
					break;
			}
		}
		return;
	}
}