import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

/**Command for users to link their KAG usernames with their discord account without logging into their KAG account by directing them to the KAG server instead. Useful for people who don't know their KAG account details(steam users).
 * <p>
 * Typing !linkserver directs the player to type !linkserver KAGUsernameHere or !linkhelp for more information
 * <p>
 * Typing !linkserver with 1 argument will check if the string is small enough to be a username (20 characters)
 * It then checks for the username of the api and corrects the case of the characters if necessary (KAG usernames are defined with a case, but the same name with different capitalization is not allowed)
 * If the username is not registered or does not own the game the appropriate response will be returned. Otherwise, the player will be directed to a gather server to complete the link process. 
 * 
 * @author cameron
 * @see DiscordBot#getPlayerInfo(String)
 * @see DiscordBot#getCorrectCase(String)
 * @see GatherDB#linkAccounts(String, long)
 * @see DiscordBot#addLinkRequest(sx.blah.discord.handle.obj.IUser, String)
 * @see DiscordBot#doLinkRequest(String, long)
 */
public class CommandLinkServer implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @param args
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandLinkServer
	 */
	@Command(aliases = {"!linkserver", "!serverlink"}, description = "Link your KAG account to your discord account by logging into a kag server")
	public void onCommand(IMessage message, String[] args)
	{
		if(args.length<=0)
		{
			DiscordBot.reply(message,"in order to link your Discord and KAG accounts provide your KAG username like this **!linkserver KAGUsernameHere**, for more information use !linkhelp");
		}
		else if(args.length<=1)
		{
			//check the username is small enough, if its too big they probably forgot the space between username and token
			if(args[0].length()>20)
			{
				DiscordBot.reply(message,"that username is too long to be valid!");
				return;
			}
			//quick sanity check on their username before giving them the link
			PlayerInfoObject info = DiscordBot.getPlayerInfo(args[0]);
			if(info==null || info.username.equals(""))
			{
				DiscordBot.reply(message,"an error occured checking your username, the supplied username was not valid or the kag2d api could not be accessed (https://api.kag2d.com/v1/player/"+args[0]+"/info)");
				return;
			}
			else if(info.gold==false)
			{
				DiscordBot.reply(message,"the username you entered does not own the game! If you are a steam user you may have made seperate forum and game accounts and should use your game account. If you do not know your game account you can connect to the gather server to see it on the scoreboard");
				return;
			}
			else
			{
				int returnVal = DiscordBot.addLinkRequest(message.getAuthor(), info.username);
				switch(returnVal)
				{
				case 1:
					DiscordBot.reply(message,"please connect to a gather KAG server and paste this message in chat: !link "+message.getAuthor().getLongID());
					return;
				case 0:
					DiscordBot.reply(message,"your link request has been updated for a new kag username, please connect to a gather KAG server and paste this message to ingame chat: !link "+message.getAuthor().getLongID());
				}
			}
		}
		else if(args.length>=2)
		{
			DiscordBot.reply(message,"you have entered too many arguments for this command, perhaps you put an accidental space in your name, usage is **!linkserver KAGUsernameHere**");
		}
		return;
	}
}