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

public class CommandLink implements CommandExecutor
{
	@Command(aliases = {"!link"}, description = "Link your KAG account to your discord account")
	public void onCommand(IMessage message, String[] args)
	{
		if(args.length<=0)
		{
			DiscordBot.reply(message,"in order to link your Discord and KAG accounts provide your KAG username like this **!link KAGUsernameHere**, for more information use !linkhelp (Please note username is **Case Sensitive**)");
		}
		else if(args.length<=1)
		{
			DiscordBot.reply(message,"please go to https://api.kag2d.com/v1/player/"+args[0]+"/token/new to get a token, and then link using the command **!link "+args[0]+" PlayerTokenHere**");
		}
		else if(args.length>=2)
		{
			String token = args[1];
			//parse the message in case they added extra bits or copied part of/the whole json
			List<String> tokens = Arrays.asList(message.toString().split("[\" ]"));
			//use the longest sub string as the token
			for(String str : tokens)
			{
				if(str.length()>token.length())
				{
					token=str;
				}
			}

			String username = args[0];
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
				Discord4J.LOGGER.warn("IO exception caught when attempting to link accounts");
				e.printStackTrace();
			}
		        
		        if(tokenCheck.playerTokenStatus)
		        {
		        	//player token is good
				int result = DiscordBot.database.linkAccounts(username, message.getAuthor().getLongID());
				Discord4J.LOGGER.info("account linking changed "+result+" lines in the sql database");
				if(result>=0) DiscordBot.reply(message,"account sucessfully linked");
				else DiscordBot.reply(message,"an error occured linking your accounts, perhaps the command was typed incorrectly, or perhaps you are trying to link accounts that are already linked seperatly. \nSomeone with database access may be needed to help link");
		        }
		        else
		        {
		        	//tell them that the token is not good
				DiscordBot.reply(message,"an error occured linking your accounts, the supplied token was not valid or the kag2d api could not be accessed (Please note that username is **Case Sensitive**)");
		        }
		}
		return;
	}
}