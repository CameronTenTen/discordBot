import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**
 * Admin only command for clearing all current sub requests. Must be used in command channel. 
 * Calls GatherObject.SubstitutionObject.ClearSubs()
 * @author cameron
 * @see SubstitutionObject#ClearSubs()
 */
public class CommandClearSubs implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandClearSubs
	 */
	@Command(aliases = {"!clearsubs"}, description = "Admin only - clear all current sub requests")
	public void onCommand(IMessage message, String[] args)
	{
		GatherObject gather = DiscordBot.getGatherObjectForChannel(message.getChannel());
		if(gather==null) return;
		
		if(!gather.isAdmin(message.getAuthor()))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Only **admins** can do that "+message.getAuthor().getNicknameForGuild(message.getGuild())+"!");
			return;
		
		}
		
		//check if there was any mentions in the message
		List<IUser> mentions = message.getMentions();
		if(mentions!=null && mentions.size()>0)
		{
			boolean doneSomething = false;
			for(IUser user : mentions)
			{
				if(gather.substitutions.removeSubRequest(user))
				{
					DiscordBot.sendMessage(gather.getCommandChannel(), "Sub request **cleared** for "+user.getDisplayName(gather.getGuild())+"!");
					doneSomething = true;
					continue;
				}
				if(gather.substitutions.removeSubVotes(user))
				{
					DiscordBot.sendMessage(gather.getCommandChannel(), "Sub votes **cleared** for "+user.getDisplayName(gather.getGuild())+"!");
					doneSomething = true;
					continue;
				}
			}
			//we get here if there was mentions in the message, and we have finished looping through them
			if(!doneSomething)
			{
				//want some kind of feedback if nothing else happened
				DiscordBot.sendMessage(gather.getCommandChannel(), "No sub request or votes found for that user!");
			}
			return;
		}
		
		//check if there was any other arguments in the message
		int gameId = -1;
		try
		{
			gameId = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException e)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "Error parsing supplied game id "+message.getAuthor().getDisplayName(message.getGuild()));
			return;
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			//got here if the message had no extra arguments
			gather.substitutions.clearSubs();
			DiscordBot.sendMessage(gather.getCommandChannel(), "Sub list **cleared**");
			return;
		}
		GatherGame game = gather.getRunningGame(gameId);
		if(game==null)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "No game found with that id "+message.getAuthor().getDisplayName(message.getGuild())+"!");
			return;
		}
		if(gather.substitutions.removeSubRequests(game))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "All sub requests cleared for game #"+gameId);
			return;
		}
		if(gather.substitutions.removeSubVotes(game))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "All sub votes cleared for game #"+gameId);
			return;
		}
	}
}