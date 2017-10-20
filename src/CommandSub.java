import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;

public class CommandSub implements CommandExecutor
{
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
		
		//check there isnt already a sub request for the player trying to sub in
		if(gather.substitutions.removeSubRequest(player))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), message.getAuthor().getDisplayName(message.getGuild()) + " has **subbed back** into their game!");
			return;
		}
		
		if(!gather.substitutions.hasSubRequest())
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "There are **no sub spaces** available " + message.getAuthor().getDisplayName(message.getGuild()) + "!");
			return;
		}
		
		//check they arent already playing
		if(gather.isInGame(player))
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "You cannot sub into a game when you are **already playing** "+message.getAuthor().getDisplayName(message.getGuild()) + "!");
			return;
		}
		
		SubstitutionObject returnObj = gather.substitutions.subPlayerIntoGame(player);
		
		if(returnObj == null)
		{
			DiscordBot.sendMessage(gather.getCommandChannel(), "There are **no sub spaces** available " + message.getAuthor().getDisplayName(message.getGuild()) + "!");
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
			Discord4J.LOGGER.info(message.getAuthor().getDisplayName(message.getGuild())+" has subbed into a game for "+returnObj.playerToBeReplaced.toString());
			DiscordBot.sendMessage(gather.getCommandChannel(), message.getAuthor().getDisplayName(message.getGuild()) + " has **replaced** " + returnObj.playerToBeReplaced.toString() + " on **" + teamString + "** Team!");
			gather.remFromQueue(message.getAuthor());
		}
		return;
	}
}