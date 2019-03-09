import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
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
public class CommandCachedPlayerInfo implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @param args
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandPlayerInfo
	 */
	@Command(aliases = {"!cachedplayerinfo"}, description = "Check the information of a player stored in the bot cache")
	public void onCommand(IMessage message, String[] args)
	{
		DiscordBot.players.printMaps();

		List<IUser> mentions = message.getMentions();
		PlayerObject player;
		if(args.length==0)
		{
			//if they just did !playerinfo without any argument, just get stats for them
			player = DiscordBot.players.getObject(message.getAuthor().getLongID());
		}
		else if(!mentions.isEmpty())
		{
			player = DiscordBot.players.getObject(mentions.get(0).getLongID());
		}
		else
		{
			player = DiscordBot.players.getObject(args[0]);
			if(player==null)
			{
				//if the username wasnt a kag name, try it as a discord id
				player = DiscordBot.players.getObject(args[0]);
			}
		}

		if(player==null)
		{
			DiscordBot.reply(message,"Could not find a record of that player, either you typed their name incorrectly, or they are not linked");
			return;
		}
		else
		{
			DiscordBot.sendMessage(message.getChannel(), "**KAG username:** "+player.getKagName()+" **Discord ID:** "+player.getDiscordid());
		}
		return;
	}
}