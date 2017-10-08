import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.google.gson.Gson;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandLinkServer implements CommandExecutor
{
	public PlayerInfoObject getPlayerInfo(String username)
	{
		String caseCheckURL = "https://api.kag2d.com/v1/player/"+username+"/info";
		try
		{
	        	URL url = new URL(caseCheckURL);
			InputStreamReader reader = new InputStreamReader(url.openStream());
			CaseCheckObject caseCheck = new Gson().fromJson(reader, CaseCheckObject.class);
			return caseCheck.playerInfo;
		}
		catch(IOException e)
		{
			//dont want to do anything here, this function is supposed to find some wrong usernames
		}
		return null;
	}
	
	public String getCorrectCase(String username)
	{
		PlayerInfoObject info = getPlayerInfo(username);
		if(info==null) return "";
		else return info.username;
	}
	
	@Command(aliases = {"!linkserver"}, description = "Link your KAG account to your discord account by logging into a kag server")
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
			PlayerInfoObject info = getPlayerInfo(args[0]);
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
				//TODO save the link request info
				int returnVal = DiscordBot.addLinkRequest(message.getAuthor(), info.username);
				switch(returnVal)
				{
				case 1:
					DiscordBot.reply(message,"please connect to a gather KAG server and paste this message in chat: !link "+message.getAuthor().getLongID());
					return;
				case 0:
					DiscordBot.reply(message,"your link request has been updated for a new kag username, please connect to a gather KAG server and paste this message in chat: !link "+message.getAuthor().getLongID());
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