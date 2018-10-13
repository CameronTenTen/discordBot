import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Admin only command for ending the current game. Must be used in command channel. 
 * Parses the first argument as an int, if any error occurs doing this it gives a vague error.
 * <p>
 * 0 is blue team, 1 is red team, -1 is a draw, -2 will wipe the game with no results recorded
 * @author cameron
 *
 */
public class CommandGiveWin implements CommandExecutor
{

	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandGiveWin
	 */
	@Command(aliases = {"!givewin"}, description = "Admin only - give win in the current game to a particular team")
	public void onCommand(IMessage message, String[] args)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;

		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that" + " "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		
		}
		int team = -2;
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
		try
		{
			team = Integer.parseInt(args[1]);
		}
		catch (NumberFormatException|ArrayIndexOutOfBoundsException e)
		{
			e.printStackTrace();
			DiscordBot.sendMessage(gather.getCommandChannel(), "An error occured parsing the team number, did you type the command correctly "+message.getAuthor().getDisplayName(message.getGuild())+"?");
			return;
		}
		
		if(!gather.endGame(matchId, team))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "An error occured setting win, did you type the command correctly "+message.getAuthor().getDisplayName(message.getGuild())+"?");
		}
		
		return;
	}
}