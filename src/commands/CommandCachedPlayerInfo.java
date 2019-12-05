package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherDB;
import core.PlayerObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.Member;

/**Command for showing player account info in the channel. Shows KAG username, Discord ID, Nick, Name and Discriminator as stored in the database. 
 * <p>
 * Useful for checking the link status of a player. 
 * @author cameron
 * @see GatherDB#getKagName(long)
 * @see GatherDB#getDiscordID(String)
 */
public class CommandCachedPlayerInfo extends Command<Message, Member, Channel>
{
	public CommandCachedPlayerInfo(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("cachedplayerinfo"), "Check the information of a player stored in the bot cache", "cachedplayerinfo KAGName/@user");
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
	{
		DiscordBot.players.printMaps();

		List<User> mentions = messageObject.getUserMentions().collectList().block();
		PlayerObject player;
		if(splitMessage.length<=1)
		{
			//if they just did !playerinfo without any argument, just get stats for them
			player = DiscordBot.players.checkCache(member.getId());
		}
		else if(!mentions.isEmpty())
		{
			player = DiscordBot.players.checkCache(mentions.get(0).getId());
		}
		else
		{
			player = DiscordBot.players.checkCache(splitMessage[1]);
			if(player==null)
			{
				//if the username wasnt a kag name, try it as a discord id
				//TODO: this is not doing what the above comment says it is doing?
				player = DiscordBot.players.checkCache(splitMessage[1]);
			}
		}

		if(player==null)
		{
			return member.getMention()+", Could not find a record of that player, either you typed their name incorrectly, or they are not linked";
		}
		else
		{
			return "**KAG username:** "+player.getKagName()+" **Discord ID:** "+player.getDiscordid();
		}
	}
}