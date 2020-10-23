package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherDB;
import core.StatsObject;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;

/**Command for checking the stats of a player that are stored in the database. If no args are given, it finds the stats for the player who typed the command. 
 * If the command contains a mention, stats are retreived for that player. 
 * It is then assumed the argument given is a KAG name. If this is unsuccessful the bot attempts to find a discord user by that name or a discord nick by that name. 
 * @author cameron
 * @see GatherDB#getStats(long)
 * @see GatherDB#getStats(String)
 */
public class CommandStats extends Command<Message, Member, Channel>
{
	public CommandStats(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("stats", "playerstats"), "Check the stats of a player stored in the database", "stats <KAGName/@user>");
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
	{
		StatsObject stats;
		List<User> mentions = messageObject.getUserMentions().collectList().block();
		if(splitMessage.length==1)
		{
			//if they just did !stats without any argument, just get stats for them
			stats = DiscordBot.database.getStats(member.getId().asLong());
			if(stats==null)
			{
				return "Could not find stats for, if you want the stats of someone else then usage is "+this.getUsage();
			}
		}
		else if(!mentions.isEmpty())
		{
			stats = DiscordBot.database.getStats(mentions.get(0).getId().asLong());
		}
		else
		{
			stats = DiscordBot.database.getStats(splitMessage[1]);
			if(stats==null)
			{
				//if the username wasnt a kag name, maybe it was a discord username
				Guild guild = messageObject.getGuild().block();
				if (guild != null)
				{
					Member matchedMember = DiscordBot.findMemberByUsername(guild, splitMessage[1]);
					if(matchedMember != null)
					{
						stats = DiscordBot.database.getStats(matchedMember.getId().asLong());
					}
					else
					{
						//the argument isnt a recognised kag name or discord name, try nicks for this guild
						matchedMember = DiscordBot.findMemberByDisplayName(guild, splitMessage[1]);
						if(matchedMember != null)
						{
							stats = DiscordBot.database.getStats(matchedMember.getId().asLong());
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