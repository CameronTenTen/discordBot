
public class RconListener
{
	public void messageReceived(String msg, String ip, int port)
	{
		//remove time stamp
		int index = msg.indexOf(" ");
		msg = msg.substring(index+1);
		
		if(!msg.startsWith("[Gather]")) return;
		
		//trim off the [Gather]
		index = msg.indexOf(" ");
		String gatherMsg = msg.substring(index+1);
		
		GatherObject gather = DiscordBot.getGatherObjectForServer(ip, port);
		System.out.println(gatherMsg);
		if(gatherMsg.startsWith("SAY"))
		{
			index = gatherMsg.indexOf(" ");
			gatherMsg = gatherMsg.substring(index+1);
			
			//separate player and message
			index = gatherMsg.indexOf(" ");
			String player = gatherMsg.substring(0,index);
			index = gatherMsg.indexOf(" ");
			gatherMsg = gatherMsg.substring(index+1);
			
			DiscordBot.sendMessage(gather.getCommandChannel(), "<"+player+"> "+gatherMsg);
		}
		else if(gatherMsg.startsWith("GAMEOVER"))
		{
			index = gatherMsg.indexOf(" ");
			gatherMsg = gatherMsg.substring(index+1);
			
			gather.endGame(ip, port, Integer.parseInt(gatherMsg));
		}
		else if(gatherMsg.startsWith("RSUB"))
		{
			index = gatherMsg.indexOf(" ");
			gatherMsg = gatherMsg.substring(index+1);
			
			gather.addSubRequest(gatherMsg, ip, port);
		}
		else if(gatherMsg.startsWith("SUBVOTE"))
		{
			String[] args = gatherMsg.split("\\s");
			gather.addSubVote(args[1], args[2], ip, port);
		}
		else if(gatherMsg.startsWith("LINK"))
		{
			String[] args = gatherMsg.split("\\s");
			long id = -1;
			try
			{
				id = Long.parseLong(args[1]);
				int returnVal = DiscordBot.doLinkRequest(args[2], id);
				switch(returnVal)
				{
				case 1:
					gather.getServer(ip, port).say("Accounts linked successfully, you can now join the queue in discord");
					DiscordBot.sendMessage(gather.getCommandChannel(), DiscordBot.client.getUserByID(id).mention()+" has sucessfully linked");
					break;
				case -1:
					gather.getServer(ip, port).say("There is no existing link request for that discord user");
					break;
				case -2:
					gather.getServer(ip, port).say("The existing discord request for that discord user uses a different kag username, you should make a link request for the correct username in discord or login with the correct KAG account");
					break;
				case -3:
					gather.getServer(ip, port).say("Could not find a user for that discord id, did you type it correctly?");
					break;
				}
			}
			catch (NumberFormatException e)
			{
				gather.getServer(ip, port).say("An error occured reading the supplied discord id, did you type it correctly?");
			}
		}
		
		
	}
}
