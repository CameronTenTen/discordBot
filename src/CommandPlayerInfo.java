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
public class CommandPlayerInfo implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @param args
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandPlayerInfo
	 */
	@Command(aliases = {"!playerinfo"}, description = "Check the information of a player stored in the database")
	public void onCommand(IMessage message, String[] args)
	{
		
		List<IUser> mentions = message.getMentions();
		String kagname = "";
		IUser user = null;
		if(args.length==0)
		{
			//if they just did !playerinfo without any argument, just get stats for them
			user = message.getAuthor();
			kagname = DiscordBot.database.getKagName(user.getLongID());
		}
		else if(!mentions.isEmpty())
		{
			user = mentions.get(0);
			kagname = DiscordBot.database.getKagName(user.getLongID());
		}
		else
		{
			long id = DiscordBot.database.getDiscordID(args[0]);
			user = DiscordBot.client.getUserByID(id);
			if(user!=null)
			{
				kagname= args[0];
			}
			else if(user==null)
			{
				//if the username wasnt a kag name, maybe it was a discord username
				//TODO this only check for their username, not their nick
				List<IUser> users = DiscordBot.client.getUsersByName(args[0],true);
				if(!users.isEmpty())
				{
					user = users.get(0);
					kagname = DiscordBot.database.getKagName(user.getLongID());
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
							user = users.get(0);
							kagname = DiscordBot.database.getKagName(user.getLongID());
						}
					}
				}
			}
		}

		if(kagname=="" || user==null)
		{
			DiscordBot.reply(message,"Could not find a record of that player, either you typed their name incorrectly, or they are not linked");
			return;
		}
		IGuild guild = message.getGuild();
		if(guild != null)
		{
			DiscordBot.sendMessage(message.getChannel(), "**KAG username:** "+kagname+" **Discord ID:** "+user.getLongID()+" **Nick:** "+user.getDisplayName(guild)+" **Name#Discriminator:** "+user.getName()+"#"+user.getDiscriminator());
		}
		else
		{
			DiscordBot.sendMessage(message.getChannel(), "**KAG username:** "+kagname+" **Discord ID:** "+user.getLongID()+" **Name#Discriminator:** "+user.getName()+"#"+user.getDiscriminator());
		}
		return;
	}
}