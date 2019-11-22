package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherDB;
import core.StatsObject;
import sx.blah.discord.handle.obj.IChannel;
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
public class CommandStats extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandStats(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("stats", "playerstats"), "Check the stats of a player stored in the database", "stats <KAGName/@user>");
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		StatsObject stats;
		List<IUser> mentions = messageObject.getMentions();
		if(splitMessage.length==1)
		{
			//if they just did !stats without any argument, just get stats for them
			stats = DiscordBot.database.getStats(user.getLongID());
			if(stats==null)
			{
				return "Could not find stats for, if you want the stats of someone else then usage is "+this.getUsage();
			}
		}
		else if(!mentions.isEmpty())
		{
			stats = DiscordBot.database.getStats(mentions.get(0).getLongID());
		}
		else
		{
			stats = DiscordBot.database.getStats(splitMessage[1]);
			if(stats==null)
			{
				//if the username wasnt a kag name, maybe it was a discord username
				//TODO this only check for their username, not their nick
				List<IUser> users = DiscordBot.client.getUsersByName(splitMessage[1],true);
				if(!users.isEmpty())
				{
					stats = DiscordBot.database.getStats(users.get(0).getLongID());
				}
				else
				{
					//the argument isnt a recognised kag name or discord name, try nicks for this guild
					if(guild!=null)
					{
						users = guild.getUsersByName(splitMessage[1], true);
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
			return "could not find stats for that player, did you type their name correctly?";
		}
		
		return "Stats for "+stats.kagname+": \n"+stats.toString();
	}
}