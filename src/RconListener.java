
public class RconListener
{
	public void messageReceived(String msg, String ip, int port)
	{
		//remove time stamp
		int index = msg.indexOf(" ");
		msg = msg.substring(index+1);
		
		System.out.println(msg);
		if(!msg.startsWith("[Gather]")) return;
		
		//trim off the [Gather]
		index = msg.indexOf(" ");
		msg = msg.substring(index+1);
		
		GatherObject gather = DiscordBot.getGatherObjectForGuild(DiscordBot.getGuildForServer(ip, port));
		System.out.println(msg);
		if(msg.startsWith("SAY"))
		{
			index = msg.indexOf(" ");
			msg = msg.substring(index+1);
			
			//separate player and message
			index = msg.indexOf(" ");
			String player = msg.substring(0,index);
			index = msg.indexOf(" ");
			msg = msg.substring(index+1);
			
			DiscordBot.bot.sendMessage(gather.getCommandChannel(), "<"+player+"> "+msg);
		}
		else if(msg.startsWith("GAMEOVER"))
		{
			index = msg.indexOf(" ");
			msg = msg.substring(index+1);
			
			gather.endGame(ip, port, Integer.parseInt(msg));
		}
		
		
	}
}
