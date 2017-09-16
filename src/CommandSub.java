import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandSub implements CommandExecutor
{
	@Command(aliases = {"!sub"}, description = "sub into the first open sub position")
	public void onCommand(IMessage message, String[] args)
	{
		if(message.getGuild() == null) return;
		GatherObject gather = DiscordBot.getGatherObjectForGuild(message.getGuild());
		if(message.getChannel() != gather.getCommandChannel()) return;
		
		SubstitutionObject returnObj = gather.subPlayerIntoGame(message.getAuthor());
		
		if(returnObj == null)
		{
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "There are **no sub spaces** available " + message.getAuthor().getDisplayName(message.getGuild()) + "!");
		}
		else
		{
			String teamString = "";
			if(returnObj.team == 0)
			{
				teamString = "Blue";
			}
			else if (returnObj.team == 1)
			{
				teamString = "Red";
			}
			else
			{
				teamString = "ERROR";
			}
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), message.getAuthor().getDisplayName(message.getGuild()) + " has **replaced** " + returnObj.playerBeingReplaced.toString() + " on **" + teamString + "** Team!");
			gather.remFromQueue(new PlayerObject(message.getAuthor()));
		}
		return;
	}
}