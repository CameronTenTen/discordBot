package commands;
import java.util.Arrays;

import core.DiscordBot;
import core.GatherDB;
import core.PlayerInfoObject;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;

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
 * @see DiscordBot#addLinkRequest(sx.blah.discord.handle.obj.Member, String)
 * @see DiscordBot#doLinkRequest(String, long)
 */
public class CommandLinkServer extends Command<Message, Member, Channel>
{
	public CommandLinkServer(Commands<Message, Member, Channel> commands)
	{
		super(commands, Arrays.asList("linkserver", "serverlink"), "Link your KAG account to your discord account by logging into a kag server", "linkserver <KAGName>");
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, Message messageObject, Member member, Channel channel)
	{
		if(splitMessage.length<=1)
		{
			return member.getMention()+", in order to link your Discord and KAG accounts provide your KAG username like this **!linkserver KAGUsernameHere**, for more information use !linkhelp";
		}
		else if(splitMessage.length==2)
		{
			String submittedUsername = splitMessage[1];
			//check the username is small enough, if its too big they probably forgot the space between username and token
			if(submittedUsername.length()>20)
			{
				return member.getMention()+", that username is too long to be valid!";
			}
			//quick sanity check on their username before giving them the link
			PlayerInfoObject info = DiscordBot.getPlayerInfo(submittedUsername);
			if(info==null || info.username.equals(""))
			{
				return member.getMention()+", an error occured checking your username, the supplied username was not valid or the kag2d api could not be accessed (https://api.kag2d.com/v1/player/"+submittedUsername+"/info)";
			}
			else if(info.gold==false)
			{
				//TODO: should remove this check due to f2p?
				return member.getMention()+", the username you entered does not own the game! If you are a steam user you may have made separate forum and game accounts and should use your game account. If you do not know your game account you can connect to the gather server to see it on the scoreboard";
			}
			else
			{
				int returnVal = DiscordBot.addLinkRequest(member, info.username);
				switch(returnVal)
				{
				case 1:
					return member.getMention()+", please connect to a gather KAG server and paste this message to ingame chat: !link "+member.getId();
				case 0:
					return member.getMention()+", your link request has been updated for a new kag username, please connect to a gather KAG server and paste this message to ingame chat: !link "+member.getId();
				}
			}
		}
		else if(splitMessage.length>=3)
		{
			return member.getMention()+", you have entered too many arguments for this command, perhaps you put an accidental space in your name, usage is **!linkserver KAGUsernameHere**";
		}
		return null;
	}
}