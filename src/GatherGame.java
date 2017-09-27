import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class GatherGame
{
	//currently unused
	private int gameID;
	private int currentRound;
	private gameState currState;
	
	private List<PlayerObject> players;
	private List<PlayerObject> bluePlayerList;
	private List<PlayerObject> redPlayerList;
	private GatherServer server;
	
	private Set<PlayerObject> playersDeserted;
	private Set<PlayerObject> playersSubbedIn;
	
	enum gameState
	{
		pregame,
		roundInProgress,
		ended
	}
	
	GatherGame(int matchId, List<PlayerObject> players, List<PlayerObject> blueTeam, List<PlayerObject> redTeam, GatherServer server)
	{
		this.gameID = matchId;
		this.players = players;
		this.bluePlayerList = blueTeam;
		this.redPlayerList = redTeam;
		this.server = server;
		
		this.playersDeserted = new HashSet<PlayerObject>();
		this.playersSubbedIn = new HashSet<PlayerObject>();
	}
	
	public void sendTeamsToServer()
	{
		//set the teams for the kag server by doing these commands
		//string[] blue={'player1', 'player2', 'etc'}; getRules().set('blueTeam',blue);
		//string[] red={'player1', 'player2', 'etc'}; getRules().set('redTeam',red);
		//getRules().set_bool('teamsSet',true);
		String msg = "";
		if(!bluePlayerList.isEmpty())
		{
			msg+="string[] blue={'";
			for(PlayerObject p : bluePlayerList)
			{
				msg+=p.getKagName();
				msg+="', '";
			}
			msg=msg.substring(0, msg.length()-3);
			msg+="}; getRules().set('blueTeam',blue);";
		}
		else
		{
			msg="string[] blue={}; getRules().set('blueTeam',blue);";
		}
		server.sendMessage(msg);
		
		msg="";
		if(!redPlayerList.isEmpty())
		{
			msg+="string[] red={'";
			for(PlayerObject p : redPlayerList)
			{
				msg+=p.getKagName();
				msg+="', '";
			}
			msg=msg.substring(0, msg.length()-3);
			msg+="}; getRules().set('redTeam',red);";
		}
		else
		{
			msg="string[] red={}; getRules().set('redTeam',red);";
		}
		server.sendMessage(msg);
		msg="getRules().set_bool('teamsSet',true);";
		server.sendMessage(msg);
	}
	
	public void updateTeamsOnServer()
	{
		//update the teams for the kag server by doing these commands
		//string[] blue={'player1', 'player2', 'etc'}; getRules().set('blueTeam',blue);
		//string[] red={'player1', 'player2', 'etc'}; getRules().set('redTeam',red);
		//"getRules().set_bool('teamsUpdated',true);"
		String msg = "";
		if(!bluePlayerList.isEmpty())
		{
			msg+="string[] blue={'";
			for(PlayerObject p : bluePlayerList)
			{
				msg+=p.getKagName();
				msg+="', '";
			}
			msg=msg.substring(0, msg.length()-3);
			msg+="}; getRules().set('blueTeam',blue);";
		}
		else
		{
			msg="string[] blue={}; getRules().set('blueTeam',blue);";
		}
		server.sendMessage(msg);
		
		msg="";
		if(!redPlayerList.isEmpty())
		{
			msg+="string[] red={'";
			for(PlayerObject p : redPlayerList)
			{
				msg+=p.getKagName();
				msg+="', '";
			}
			msg=msg.substring(0, msg.length()-3);
			msg+="}; getRules().set('redTeam',red);";
		}
		else
		{
			msg="string[] red={}; getRules().set('redTeam',red);";
		}
		server.sendMessage(msg);
		msg="getRules().set_bool('teamsUpdated',true);";
		server.sendMessage(msg);
		
	}
	
	public void replacePlayer(PlayerObject playerBeingReplaced, PlayerObject player)
	{
		int blueIndex = bluePlayerList.indexOf(playerBeingReplaced);
		int redIndex = redPlayerList.indexOf(playerBeingReplaced);
		if(blueIndex>=0 && blueIndex<bluePlayerList.size())
		{
			bluePlayerList.set(blueIndex, player);
		}
		else if(redIndex>=0 && redIndex<redPlayerList.size())
		{
			redPlayerList.set(redIndex, player);
		}
		//seems that the shuffle method (setting team arrays as subLists of the player array)
		//makes it so that replacing the player in the team list makes the replacement in the players one too
		//will keep this check in here in case something changes (maybe new shuffle function or match making will change this) 
		int index = players.indexOf(playerBeingReplaced);
		if(index >=0 && index<players.size()) players.set(index, player);
		playersDeserted.add(playerBeingReplaced);
		playersSubbedIn.add(player);
		updateTeamsOnServer();
		return;
	}
	
	public void saveResultToDB(int winningTeam)
	{
		DiscordBot.database.incrementGamesPlayed();
		for(PlayerObject p : bluePlayerList)
		{
			//if they subbed in they dont get a win/loss stat
			if(playersSubbedIn.contains(p))
			{
				break;
			}
			if(winningTeam==0)
			{
				DiscordBot.database.addWin(p.getKagName());
			}
			else if(winningTeam==1)
			{
				DiscordBot.database.addLoss(p.getKagName());
			}
			//TODO warning, draws not accounted for
		}
		for(PlayerObject p : redPlayerList)
		{
			//if they subbed in they dont get a win/loss stat
			if(playersSubbedIn.contains(p))
			{
				break;
			}
			if(winningTeam==1)
			{
				DiscordBot.database.addWin(p.getKagName());
			}
			else if(winningTeam==0)
			{
				DiscordBot.database.addLoss(p.getKagName());
			}
			//TODO warning, draws not accounted for
		}
		for(PlayerObject p : playersDeserted)
		{
			DiscordBot.database.addDesertion(p.getKagName());
		}
		for(PlayerObject p : playersSubbedIn)
		{
			//only add a subbed in stat if they didnt also desert the game
			if(playersDeserted.contains(p)) continue;
			DiscordBot.database.addSubstitution(p.getKagName());
		}
	}
	
	public void shuffleTeams()
	{
		Collections.shuffle(players);
		bluePlayerList = players.subList(0, players.size()/2);
		redPlayerList = players.subList(players.size()/2, players.size());
	}
	
	public List<String> blueMentionList()
	{
		ArrayList<String> list = new ArrayList<String>();
		for(PlayerObject player : bluePlayerList)
		{
			list.add(player.getMentionString());
		}
		return list;
	}
	
	public List<String> redMentionList()
	{
		ArrayList<String> list = new ArrayList<String>();
		for(PlayerObject player : redPlayerList)
		{
			list.add(player.getMentionString());
		}
		return list;
	}

	public int getGameID() {
		return gameID;
	}

	public void setGameId(int gameID) {
		this.gameID = gameID;
	}
	
	public int getPlayerTeam(IUser user)
	{
		if(user==null) return -1;
		for(PlayerObject p : bluePlayerList)
		{
			if(user.equals(p.getDiscordUserInfo()))return 0;
		}
		for(PlayerObject p : redPlayerList)
		{
			if(user.equals(p.getDiscordUserInfo()))return 1;
		}
		return -1;
	}
	
	public int getPlayerTeam(PlayerObject player)
	{
		return getPlayerTeam(player.getDiscordUserInfo());
	}
	
	public boolean isPlayerPlaying(IUser user)
	{
		return getPlayerTeam(user)!=-1;
	}
	
	public boolean isPlayerPlaying(PlayerObject player)
	{
		return getPlayerTeam(player)!=-1;
	}

	public String getServerIp()
	{
		return server.getIp();
	}
	
	public int getServerPort()
	{
		return server.getPort();
	}

	public GatherServer getServer() {
		return server;
	}

	public void setServer(GatherServer server) {
		this.server = server;
	}

	public String toString(IGuild guild)
	{
		if(players.isEmpty()) return "";
		
		String blueString = "**Blue:** ";
		for(PlayerObject player : bluePlayerList)
		{
			blueString += player.getKagName()+" ("+player.getDiscordUserInfo().getDisplayName(guild)+") ";
		}
		String redString = "**Red:** ";
		for(PlayerObject player : redPlayerList)
		{
			redString += player.getKagName()+" ("+player.getDiscordUserInfo().getDisplayName(guild)+") ";
		}
		return blueString + "\n" + redString;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this.server.equals(((GatherGame)obj).server);
	}
	
	@Override
	public String toString()
	{
		if(players.isEmpty()) return "";
		
		String blueString = "**Blue:** ";
		for(PlayerObject player : bluePlayerList)
		{
			blueString += player.toString();
		}
		String redString = "**Red:** ";
		for(PlayerObject player : redPlayerList)
		{
			redString += player.toString();
		}
		return blueString + "\n" + redString;
	}
}
