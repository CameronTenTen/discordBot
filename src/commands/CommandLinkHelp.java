package commands;
import java.util.Arrays;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/** Prints a linking help message
 * @author cameron
 *
 */
public class CommandLinkHelp extends Command<IMessage, IUser, IChannel, IGuild>
{
	public CommandLinkHelp(Commands<IMessage, IUser, IChannel, IGuild> commands)
	{
		super(commands, Arrays.asList("linkhelp"), "Link your KAG account to your discord account");
	}

	@Override
	public String onCommand(String[] splitMessage, String messageString, IMessage messageObject, IUser user, IChannel channel, IGuild guild)
	{
		return "linking is the process of connecting your KAG and Discord accounts so that the bot knows who you are. \n"
		                + "This allows for the server to manage your team and record more detailed stats. \n"
		                + "You should **never submit your password to a site that you don't trust**, therefore linking is done by directing you to a kag2d.com login page. \n"
		                + "This page provides you with a public key/token after you login, you should then submit this token to the bot so that it knows who you are. \n"
		                + "This token is how the game verifies your username when connecting to public servers without sharing your password with these servers, and each token can only be used once. \n"
		                + "You can see the documentation about these tokens here https://developers.thd.vg/api/players.html#get--player-(username)-token-new"
		                + "To get started with linking, type the command **!link KAGUsernameHere** (this is the reccomended method of linking). \n"
		                + "Alternatively, if you do not know your password and do not want to set one, you can use the command **!linkserver KAGUsernameHere** (you will then need to join a gather server to complete the process). \n";
	}
}