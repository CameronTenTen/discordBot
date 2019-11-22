package commands;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import core.DiscordBot;
import core.GatherDB;
import core.PlayerInfoObject;
import core.TokenCheckObject;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

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
public class CommandLink extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandLink(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("link"), "Link your KAG account to your discord account", "link <KAGName> <playerTokenHere>");
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		if(splitMessage.length<=1)
		{
			return user.mention()+", in order to link your Discord and KAG accounts provide your KAG username like this **!link KAGUsernameHere**, for more information use !linkhelp";
		}
		else if(splitMessage.length==2)
		{
			String submittedUsername = splitMessage[1];
			//check the username is small enough, if its too big they probably forgot the space between username and token
			if(submittedUsername.length()>20)
			{
				return user.mention()+", your username is too long! Did you forget a space somewhere in your message?";
			}
			//quick sanity check on their username before giving them the link
			PlayerInfoObject info = DiscordBot.getPlayerInfo(submittedUsername);
			if(info==null || info.username.equals(""))
			{
				return user.mention()+", an error occured checking your username, the supplied username was not valid or the kag2d api could not be accessed (https://api.kag2d.com/v1/player/"+submittedUsername+"/info)";
			}
			else if(info.gold==false)
			{
				//TODO: should remove this check due to f2p?
				return user.mention()+", the username you entered does not own the game! If you are a steam user you may have made separate forum and game accounts and should **use your game account**, if you **have not set your password for that account** there is a **button in the main menu** of the game to do so. \nIf you **do not want to setup your account** you can use the command **!linkserver KAGUsernameHere** instead";
			}
			else
			{
				return user.mention()+", please go to https://api.kag2d.com/v1/player/"+info.username+"/token/new to get a token, and then link using the command **!link "+info.username+" PlayerTokenHere**";
			}
		}
		else if(splitMessage.length>=3)
		{
			//this ideally how it would be done if users didn't make mistakes
			//String token = splitMessage[2];
			//but that is not the world we live in
			String token = "";
			//parse the message with quotes as an extra delimiter in case they added extra bits or copied part of/the whole json
			List<String> tokens = Arrays.asList(messageString.toString().split("\\s|\""));
			//use the longest sub string as the token
			//this is a safe assumption because the token is really long (while usernames are restricted to 20 characters, and the link command is also much shorter than that)
			//still a bit of a hack, but people who dont know how to read json are going to make a mistake copying the token 90% of the time, so it is necessary
			for(String str : tokens)
			{
				if(str.length()>token.length())
				{
					token=str;
				}
			}

			String submittedUsername = splitMessage[1];
			//check the right case for their name early rather than late
			PlayerInfoObject info = DiscordBot.getPlayerInfo(submittedUsername);
			if(info==null || info.username.equals(""))
			{
				//check if the argument looks like a discord id (probably means they were supposed to paste the link to ingame chat)
				try
				{
					//I have no idea what the minimum length of a discord id is
					//just guessing a reasonable length and putting a small sanity check here
					//doesnt matter too much, just tring to filter out anything that might be a discord id so we can help the user
					if(submittedUsername.length()>15)
					{
						Long.parseLong(submittedUsername);
						return user.mention()+", Could not find a kag username matching "+submittedUsername+", but that looks like a valid discord id.  Were you supposed to paste that message to ingame chat?";
					}
				}
				catch (Exception e)
				{
				}
				return user.mention()+", an error occured checking your username, the supplied username was not valid or the kag2d api could not be accessed (https://api.kag2d.com/v1/player/"+submittedUsername+"/info)";
			}
			else if(info.gold==false)
			{
				//TODO: should remove this check due to f2p?
				return user.mention()+", the username you entered does not own the game! If you are a steam user you may have made separate forum and game accounts and should use your game account, if you have not set your password for that account there is a button in the main menu of the game to do so.";
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
				int result = DiscordBot.database.linkAccounts(username, user.getLongID());
				Discord4J.LOGGER.info("account linking changed "+result+" lines in the sql database");
				if(result>=0)
				{
					this.reply(messageObject, "account successfully linked");
					if(!DiscordBot.database.checkValidLink(username, user.getLongID()))
					{
						this.reply(messageObject, "WARNING: problem with linked information detected, there maybe more than one entry for you. **Please share this error with someone that has database access.** (This should not prevent you playing in the short term, but may cause issues long term)");
					}
					//check if the player needs to be updated on any servers
					DiscordBot.playerChanged(user);
				}
				else return user.mention()+", an error occured linking your accounts, this message should not be displayed, you may be trying to link two accounts that are already linked seperatly. \nSomeone with database access may be needed to help link";
			}
			else
			{
				//tell them that the token is not good
				return user.mention()+", an error occured linking your accounts, the supplied token was not valid or the kag2d api could not be accessed";
			}
		}
		return null;
	}
}