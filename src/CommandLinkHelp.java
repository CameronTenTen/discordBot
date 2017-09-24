import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import sx.blah.discord.handle.obj.IMessage;

public class CommandLinkHelp implements CommandExecutor
{
	@Command(aliases = {"!linkhelp"}, description = "Link your KAG account to your discord account")
	public void onCommand(IMessage message, String[] args)
	{
		DiscordBot.sendMessage(message.getChannel(),"linking is the process of connecting your KAG and Discord accounts so that the bot knows who you are. \n"
				+ "This allows for the server to manage your team and record more detailed stats. \n"
				+ "You should **never submit your password to a site that you don't trust**, therefore linking is done by directing you to a kag2d.com login page. \n"
				+ "This page provides you with a public key/token after you login, you should then submit this token to the bot so that it knows who you are. \n"
				+ "This token is how the game verifies your username when connecting to public servers without sharing your password with these servers, and each token can only be used once. \n"
				+ "To get started with linking, type the command **!link KAGUsernameHere**. \n"
				+ "You can see the documentation about these tokens here https://developers.thd.vg/api/players.html#get--player-(username)-token-new");
	}
}