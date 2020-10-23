package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherDB;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;

/**Command for showing player account info in the channel. Shows KAG username, Discord ID, Nick, Name and Discriminator as stored in the database.
 * <p>
 * Useful for checking the link status of a player.
 * @author cameron
 * @see GatherDB#getKagName(long)
 * @see GatherDB#getDiscordID(String)
 */
public class CommandPlayerInfo extends Command<Message, Member, Channel>
{
	public CommandPlayerInfo(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("playerinfo"), "Check the information of a player stored in the database", "playerinfo <@user/KAGName/discordName/discordNick>");
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
	{
		List<User> mentions = messageObject.getUserMentions().collectList().block();
		String kagnameToGetInfoFor = "";
		User userToGetInfoFor = null;
		if(splitMessage.length<=1)
		{
			//if they just did !playerinfo without any argument, just get stats for them
			userToGetInfoFor = member;
			kagnameToGetInfoFor = DiscordBot.database.getKagName(userToGetInfoFor.getId().asLong());
		}
		else if(!mentions.isEmpty())
		{
			userToGetInfoFor = mentions.get(0);
			kagnameToGetInfoFor = DiscordBot.database.getKagName(userToGetInfoFor.getId().asLong());
		}
		else
		{
			//first try to interpret the argument as a kagname
			long id = DiscordBot.database.getDiscordID(splitMessage[1]);
			userToGetInfoFor = DiscordBot.client.getUserById(Snowflake.of(id)).block();
			if(userToGetInfoFor!=null)
			{
				kagnameToGetInfoFor= splitMessage[1];
			}
			else if(userToGetInfoFor==null)
			{
				//if the username wasnt a kag name, maybe it was a discord username
				Guild guild = messageObject.getGuild().block();
				if (guild != null)
				{
					userToGetInfoFor = DiscordBot.findMemberByUsername(guild, splitMessage[1]);
					if(userToGetInfoFor != null)
					{
						kagnameToGetInfoFor = DiscordBot.database.getKagName(userToGetInfoFor.getId().asLong());
					}
					else
					{
						//the argument isnt a recognised kag name or discord name, try nicks for this guild
						userToGetInfoFor = DiscordBot.findMemberByDisplayName(guild, splitMessage[1]);
						if(userToGetInfoFor != null)
						{
							kagnameToGetInfoFor = DiscordBot.database.getKagName(userToGetInfoFor.getId().asLong());
						}
					}
				}
			}
		}

		if(kagnameToGetInfoFor=="" || userToGetInfoFor==null)
		{
			return"Could not find a record of that player, either you typed their name incorrectly, or they are not linked";
		}
		return "**KAG username:** "+kagnameToGetInfoFor+" **Discord ID:** "+userToGetInfoFor.getId().asString()+" **Name#Discriminator:** "+userToGetInfoFor.getUsername()+"#"+userToGetInfoFor.getDiscriminator();
	}
}
