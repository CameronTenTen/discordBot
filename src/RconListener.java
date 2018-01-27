
/**Listener object to be added to the KagServerChecker. Gets messages passed to it when they are received from the server. 
 * @author cameron
 *
 */
public class RconListener
{
	/**The function that is called when a message is received. 
	 * @param msg the message received from the server
	 * @param ip the ip address of the server
	 * @param port the port of the server
	 */
	public void messageReceived(String msg, String ip, int port)
	{
		//remove time stamp
		int index = msg.indexOf(" ");
		msg = msg.substring(index+1);
		
		if(msg.startsWith("[Gather]"))
		{
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

				//just to make sure there isnt some strange cases of the end game detecting a full queue while someone is adding
				synchronized(gather)
				{
					gather.endGame(ip, port, Integer.parseInt(gatherMsg));
				}
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
				String username = "";
				try
				{
					String idString = args[1].trim();
					id = Long.parseLong(idString);
					username = args[2];
					int returnVal = DiscordBot.doLinkRequest(username, id);
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
						gather.getServer(ip, port).say("The username of the KAG account you are currently connected with is "+username);
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
			else if(gatherMsg.startsWith("BUILDINGTIMEENDED"))
			{
				gather.setBuildingTimeEnded(ip, port);
			}
			else if(gatherMsg.startsWith("ROUNDSTARTED"))
			{
				gather.setRoundStarted(ip, port);
			}
		}
		else if(msg.startsWith("[Tickets]"))
		{
			GatherObject gather = DiscordBot.getGatherObjectForServer(ip, port);
			if(gather==null) return;
			String[] args = msg.split("\\s");
			int team=Integer.parseInt(args[1]);
			int tickets=Integer.parseInt(args[2]);
			gather.updateTickets(ip, port, team, tickets);
		}
		
		
	}
}
