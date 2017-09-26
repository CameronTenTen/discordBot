import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CommandStats implements CommandExecutor
{
	@Command(aliases = {"!stats"}, description = "Check the stats of a player stored in the database")
	public void onCommand(IMessage message, String[] args)
	{
		
		StatsObject stats;
		List<IUser> mentions = message.getMentions();
		if(args.length==0)
		{
			//if they just did !stats without any argument, just get stats for them
			stats = DiscordBot.database.getStats(message.getAuthor().getLongID());
			if(stats==null)
			{
				DiscordBot.reply(message,"Could not find personal stats, if you want the stats of someone else then usage is !stats <KAGName/User>");
				return;
			}
		}
		else if(!mentions.isEmpty())
		{
			stats = DiscordBot.database.getStats(mentions.get(0).getLongID());
		}
		else
		{
			stats = DiscordBot.database.getStats(args[0]);
			if(stats==null)
			{
				//if the username wasnt a kag name, maybe it was a discord username
				//TODO this only check for their username, not their nick
				List<IUser> users = DiscordBot.client.getUsersByName(args[0],true);
				if(!users.isEmpty())
				{
					stats = DiscordBot.database.getStats(users.get(0).getLongID());
				}
			}
		}
		
		if(stats==null)
		{
			DiscordBot.reply(message, "could not find stats for that player, did you type their name correctly?");
			return;
		}
		
		DiscordBot.sendMessage(message.getChannel(), "Stats for "+stats.kagname+": \n"+stats.toString());
		return;
	}
}