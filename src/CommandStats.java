import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Command for checking the stats of a player that are stored in the database. If no args are given, it finds the stats for the player who typed the command. 
 * If the command contains a mention, stats are retreived for that player. 
 * It is then assumed the argument given is a KAG name. If this is unsuccessful the bot attempts to find a discord user by that name or a discord nick by that name. 
 * @author cameron
 * @see GatherDB#getStats(long)
 * @see GatherDB#getStats(String)
 */
public class CommandStats implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @param args
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandStats
	 */
	@Command(aliases = {"!stats","!playerstats"}, description = "Check the stats of a player stored in the database")
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
				else
				{
					//the argument isnt a recognised kag name or discord name, try nicks for this guild
					IGuild guild = message.getGuild();
					if(guild!=null)
					{
						users = guild.getUsersByName(args[0], true);
						if(!users.isEmpty())
						{
							stats = DiscordBot.database.getStats(users.get(0).getLongID());
						}
					}
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