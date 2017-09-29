
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
		
		
	}
}
