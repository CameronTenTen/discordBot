import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IMessage;

/**Command for users to link their KAG usernames with their discord account. 
 * <p>
 * Typing !link directs the player to type !link KAGUsernameHere or !linkhelp for more information
 * <p>
 * Typing !link with 1 argument will check if the string is small enough to be a username (20 characters)
 * It then checks for the username of the api and corrects the case of the characters if necessary (KAG usernames are defined with a case, but the same name with different capitalization is not allowed)
 * If the username is not registered or does not own the game the appropriate response will be returned. Otherwise, the player will be given a link to a place in the api where they can login for a token. 
 * <p>
 * Typing !link with 2 arguments takes the second string as a kag username, and splits the message at any quotes or spaces, taking the longest string as a token.
 * The same checks on username validity are made as with the 1 argument version, then the validity of the token is checked. 
 * If the token is valid, the players details are linked in the connected sql database. 
 * 
 * @author cameron
 * @see https://developers.thd.vg/api/players.html
 * @see https://api.kag2d.com/v1/player/username/info
 * @see https://api.kag2d.com/v1/player/username/token/new
 * @see DiscordBot#getPlayerInfo(String)
 * @see DiscordBot#getCorrectCase(String)
 * @see GatherDB#linkAccounts(String, long)
 */
public class CommandLink implements CommandExecutor
{
	/**The function that is called when the command is used
	 * @param message
	 * @see https://github.com/BtoBastian/sdcf4j
	 * @see #CommandLink
	 */
	@Command(aliases = {"!link"}, description = "Link your KAG account to your discord account")
	public void onCommand(IMessage message)
	{
		String[] input = message.getContent().split("\\s+");
		String[] args = Arrays.copyOfRange(input, 1, input.length);
		if(args.length<=0)
		{
			DiscordBot.reply(message,"in order to link your Discord and KAG accounts provide your KAG username like this **!link KAGUsernameHere**, for more information use !linkhelp");
		}
		else if(args.length<=1)
		{
			//check the username is small enough, if its too big they probably forgot the space between username and token
			if(args[0].length()>20)
			{
				DiscordBot.reply(message,"your username is too long! Did you forget a space somewhere?");
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
				DiscordBot.reply(message,"the username you entered does not own the game! If you are a steam user you may have made seperate forum and game accounts and should **use your game account**, if you **have not set your password for that account** there is a **button in the main menu** of the game to do so. \nIf you **do not want to setup your account** you can use the command **!linkserver KAGUsernameHere** instead");
				return;
			}
			else
			{
				DiscordBot.reply(message,"please go to https://api.kag2d.com/v1/player/"+info.username+"/token/new to get a token, and then link using the command **!link "+info.username+" PlayerTokenHere**");
			}
		}
		else if(args.length>=2)
		{
			//this ideally how it would be done if users didn't make mistakes
			//String token = args[1];
			//but that is not the world we live in
			String token = "";
			//parse the message in case they added extra bits or copied part of/the whole json
			List<String> tokens = Arrays.asList(message.toString().split("\\s|\""));
			//use the longest sub string as the token
			//this is a safe assumption because the token is really long(usernames are restricted to 20 characters, and the link command is also shorter than that)
			//still a bit of a hack, but people who dont know how to read json are going to make a mistake copying the token 90% of the time, so it is necessary
			for(String str : tokens)
			{
				if(str.length()>token.length())
				{
					token=str;
				}
			}

			//check the right case for their name early rather than late
			PlayerInfoObject info = DiscordBot.getPlayerInfo(args[0]);
			if(info==null || info.username.equals(""))
			{
				DiscordBot.reply(message,"an error occured checking your username, the supplied username was not valid or the kag2d api could not be accessed (https://api.kag2d.com/v1/player/"+args[0]+"/info)");
				return;
			}
			else if(info.gold==false)
			{
				DiscordBot.reply(message,"the username you entered does not own the game! If you are a steam user you may have made seperate forum and game accounts and should use your game account, if you have not set your password for that account there is a button in the main menu of the game to do so.");
				return;
			}
			String username = info.username;
			
			TokenCheckObject tokenCheck = new TokenCheckObject();
			String urlString = "https://api.kag2d.com/v1/player/"+(username)+"/token/"+token;

			//check token is valid
			try
			{
				//IO exception is thrown when incorrect token is used
				URL url = new URL(urlString);
				InputStreamReader reader = new InputStreamReader(url.openStream());
				tokenCheck = new Gson().fromJson(reader, TokenCheckObject.class);
			}
			catch(IOException e)
			{
				Discord4J.LOGGER.warn("IO exception caught when attempting to link accounts: "+e.getMessage());
			}
			
			if(tokenCheck.playerTokenStatus)
			{
				//player token is good
				int result = DiscordBot.database.linkAccounts(username, message.getAuthor().getLongID());
				Discord4J.LOGGER.info("account linking changed "+result+" lines in the sql database");
				if(result>=0)
				{
					DiscordBot.reply(message,"account successfully linked");
					if(!DiscordBot.database.checkValidLink(username, message.getAuthor().getLongID()))
					{
						DiscordBot.reply(message,"WARNING: problem with linked information detected, there maybe more than one entry for you. **Please share this error with someone that has database access.** (This should not prevent you playing in the short term, but may cause issues long term)");
					}
					//check if the player needs to be updated on any servers
					DiscordBot.playerChanged(message.getAuthor());
				}
				else DiscordBot.reply(message,"an error occured linking your accounts, this message should not be displayed, you may be trying to link two accounts that are already linked seperatly. \nSomeone with database access may be needed to help link");
			}
			else
			{
				//tell them that the token is not good
				DiscordBot.reply(message,"an error occured linking your accounts, the supplied token was not valid or the kag2d api could not be accessed");
			}
		}
		return;
	}
}