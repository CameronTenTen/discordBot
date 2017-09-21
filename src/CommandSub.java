import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;

public class CommandSub implements CommandExecutor
{
	@Command(aliases = {"!sub"}, description = "sub into the first open sub position")
	public void onCommand(IMessage message, String[] args)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;
		
		PlayerObject player = DiscordBot.players.getObject(message.getAuthor());
		if(player==null)
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "You must be linked to sub into a game " + message.getAuthor().getDisplayName(message.getGuild()) + "! Use !link to get started or !linkhelp for more information");
			return;
		}
		
		SubstitutionObject returnObj = gather.substitutions.subPlayerIntoGame(player);
		
		if(returnObj == null)
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "There are **no sub spaces** available " + message.getAuthor().getDisplayName(message.getGuild()) + "!");
		}
		else
		{
			int team = returnObj.game.getPlayerTeam(returnObj.playerToBeReplaced);
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
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), message.getAuthor().getDisplayName(message.getGuild()) + " has **replaced** " + returnObj.playerToBeReplaced.toString() + " on **" + teamString + "** Team!");
			gather.remFromQueue(message.getAuthor());
		}
		return;
	}
}