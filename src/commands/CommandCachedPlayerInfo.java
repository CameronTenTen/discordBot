package commands;
import java.util.Arrays;
import java.util.List;

import core.DiscordBot;
import core.GatherDB;
import core.PlayerObject;
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
public class CommandCachedPlayerInfo extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandCachedPlayerInfo(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("cachedplayerinfo"), "Check the information of a player stored in the bot cache", "cachedplayerinfo KAGName/@user");
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		DiscordBot.players.printMaps();

		List<IUser> mentions = messageObject.getMentions();
		PlayerObject player;
		if(splitMessage.length<=1)
		{
			//if they just did !playerinfo without any argument, just get stats for them
			player = DiscordBot.players.checkCache(user.getLongID());
		}
		else if(!mentions.isEmpty())
		{
			player = DiscordBot.players.checkCache(mentions.get(0).getLongID());
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
			return user.mention()+", Could not find a record of that player, either you typed their name incorrectly, or they are not linked";
		}
		else
		{
			return "**KAG username:** "+player.getKagName()+" **Discord ID:** "+player.getDiscordid();
		}
	}
}