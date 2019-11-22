package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherDB;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**Command for showing player account info in the channel. Shows KAG username, Discord ID, Nick, Name and Discriminator as stored in the database. 
 * <p>
 * Useful for checking the link status of a player. 
 * @author cameron
 * @see GatherDB#getKagName(long)
 * @see GatherDB#getDiscordID(String)
 */
public class CommandPlayerInfo extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandPlayerInfo(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("playerinfo"), "Check the information of a player stored in the database", "playerinfo <@user/KAGName/discordName/discordNick>");
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		
		List<IUser> mentions = messageObject.getMentions();
		String kagnameToGetStatsFor = "";
		IUser userToGetStatsFor = null;
		if(splitMessage.length<=1)
		{
			//if they just did !playerinfo without any argument, just get stats for them
			userToGetStatsFor = user;
			kagnameToGetStatsFor = DiscordBot.database.getKagName(userToGetStatsFor.getLongID());
		}
		else if(!mentions.isEmpty())
		{
			userToGetStatsFor = mentions.get(0);
			kagnameToGetStatsFor = DiscordBot.database.getKagName(userToGetStatsFor.getLongID());
		}
		else
		{
			//first try to interpret the argument as a kagname
			long id = DiscordBot.database.getDiscordID(splitMessage[1]);
			userToGetStatsFor = DiscordBot.client.getUserByID(id);
			if(userToGetStatsFor!=null)
			{
				kagnameToGetStatsFor= splitMessage[1];
			}
			else if(userToGetStatsFor==null)
			{
				//if the username wasnt a kag name, maybe it was a discord username
				List<IUser> users = DiscordBot.client.getUsersByName(splitMessage[1],true);
				if(!users.isEmpty())
				{
					userToGetStatsFor = users.get(0);
					kagnameToGetStatsFor = DiscordBot.database.getKagName(userToGetStatsFor.getLongID());
				}
				else
				{
					//the argument isnt a recognised kag name or discord name, try nicks for this guild
					if(guild!=null)
					{
						users = guild.getUsersByName(splitMessage[1], true);
						if(!users.isEmpty())
						{
							userToGetStatsFor = users.get(0);
							kagnameToGetStatsFor = DiscordBot.database.getKagName(userToGetStatsFor.getLongID());
						}
					}
				}
			}
		}

		if(kagnameToGetStatsFor=="" || userToGetStatsFor==null)
		{
			return"Could not find a record of that player, either you typed their name incorrectly, or they are not linked";
		}
		if(guild != null)
		{
			return "**KAG username:** "+kagnameToGetStatsFor+" **Discord ID:** "+userToGetStatsFor.getLongID()+" **Nick:** "+userToGetStatsFor.getDisplayName(guild)+" **Name#Discriminator:** "+userToGetStatsFor.getName()+"#"+userToGetStatsFor.getDiscriminator();
		}
		else
		{
			return "**KAG username:** "+kagnameToGetStatsFor+" **Discord ID:** "+userToGetStatsFor.getLongID()+" **Name#Discriminator:** "+userToGetStatsFor.getName()+"#"+userToGetStatsFor.getDiscriminator();
		}
	}
}