import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sx.blah.discord.Discord4J;
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
	
	private Set<PlayerObject> redDeserted;
	private Set<PlayerObject> blueDeserted;
	private Set<PlayerObject> redSubbedIn;
	private Set<PlayerObject> blueSubbedIn;
	
	private Set<PlayerObject> scrambleVotes;
	
	private int scrambleVotesReq = 7;
	
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
		
		this.redDeserted = new HashSet<PlayerObject>();
		this.blueDeserted = new HashSet<PlayerObject>();
		this.redSubbedIn = new HashSet<PlayerObject>();
		this.blueSubbedIn = new HashSet<PlayerObject>();
		
		this.scrambleVotes = new HashSet<PlayerObject>();
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
			blueDeserted.add(playerBeingReplaced);
			blueSubbedIn.add(player);
		}
		else if(redIndex>=0 && redIndex<redPlayerList.size())
		{
			redPlayerList.set(redIndex, player);
			redDeserted.add(playerBeingReplaced);
			redSubbedIn.add(player);
		}
		//seems that the shuffle method (setting team arrays as subLists of the player array)
		//makes it so that replacing the player in the team list makes the replacement in the players one too
		//will keep this check in here in case something changes (maybe new shuffle function or match making will change this) 
		int index = players.indexOf(playerBeingReplaced);
		if(index >=0 && index<players.size()) players.set(index, player);
		updateTeamsOnServer();
		return;
	}
	
	public void saveResultToDB(int winningTeam, SubManager subObj)
	{
		DiscordBot.database.incrementGamesPlayed();
		for(PlayerObject p : bluePlayerList)
		{
			//if they subbed in or left they dont get a win/loss stat
			if(blueSubbedIn.contains(p) || subObj.hasSubRequest(p))
			{
				continue;
			}
			else if(winningTeam==0)
			{
				int val = DiscordBot.database.addWin(p.getKagName());
				Discord4J.LOGGER.info("Adding win for "+p.getKagName()+" "+val);
			}
			else if(winningTeam==1)
			{
				int val = DiscordBot.database.addLoss(p.getKagName());
				Discord4J.LOGGER.info("Adding loss for "+p.getKagName()+" "+val);
			}
			//TODO warning, draws not accounted for
		}
		for(PlayerObject p : redPlayerList)
		{
			//if they subbed in or left they dont get a win/loss stat
			if(redSubbedIn.contains(p) || subObj.hasSubRequest(p))
			{
				continue;
			}
			else if(winningTeam==1)
			{
				int val = DiscordBot.database.addWin(p.getKagName());
				Discord4J.LOGGER.info("Adding win for "+p.getKagName()+" "+val);
			}
			else if(winningTeam==0)
			{
				int val = DiscordBot.database.addLoss(p.getKagName());
				Discord4J.LOGGER.info("Adding loss for "+p.getKagName()+" "+val);
			}
			//TODO warning, draws not accounted for
		}
		for(PlayerObject p : blueDeserted)
		{
			if(winningTeam==0)
			{
				int val = DiscordBot.database.addDesertion(p.getKagName());
				Discord4J.LOGGER.info("Adding desertion for "+p.getKagName()+" "+val);
			}
			else if(winningTeam==1)
			{
				int val = DiscordBot.database.addDesertionLoss(p.getKagName());
				Discord4J.LOGGER.info("Adding desertion for "+p.getKagName()+" "+val);
			}
		}
		for(PlayerObject p : redDeserted)
		{
			if(winningTeam==1)
			{
				int val = DiscordBot.database.addDesertion(p.getKagName());
				Discord4J.LOGGER.info("Adding desertion for "+p.getKagName()+" "+val);
			}
			else if(winningTeam==0)
			{
				int val = DiscordBot.database.addDesertionLoss(p.getKagName());
				Discord4J.LOGGER.info("Adding desertion for "+p.getKagName()+" "+val);
			}
		}
		for(PlayerObject p : blueSubbedIn)
		{
			if(winningTeam==0)
			{
				int val = DiscordBot.database.addSubstitutionWin(p.getKagName());
				Discord4J.LOGGER.info("Adding substitution for "+p.getKagName()+" "+val);
			}
			else if(winningTeam==1)
			{
				int val = DiscordBot.database.addSubstitution(p.getKagName());
				Discord4J.LOGGER.info("Adding substitution for "+p.getKagName()+" "+val);
			}
		}
		for(PlayerObject p : redSubbedIn)
		{
			if(winningTeam==1)
			{
				int val = DiscordBot.database.addSubstitutionWin(p.getKagName());
				Discord4J.LOGGER.info("Adding substitution for "+p.getKagName()+" "+val);
			}
			else if(winningTeam==0)
			{
				int val = DiscordBot.database.addSubstitution(p.getKagName());
				Discord4J.LOGGER.info("Adding substitution for "+p.getKagName()+" "+val);
			}
		}
		
		List<PlayerObject> subs = subObj.getOpenSubs(this);
		if(subs!=null)
		{
			for(PlayerObject p : subs)
			{
				if(this.getPlayerTeam(p) == winningTeam || winningTeam == -1)
				{
					//if they were on the winning team or its a draw add a desertion
					DiscordBot.database.addDesertion(p.getKagName());
				}
				else if (winningTeam>=0 && this.getPlayerTeam(p) != winningTeam)
				{
					//if a team won and the player wasnt on it then they get a desertion loss
					DiscordBot.database.addDesertionLoss(p.getKagName());
				}
			}
		}
	}
	
	public void shuffleTeams()
	{
		Collections.shuffle(players);
		bluePlayerList = players.subList(0, players.size()/2);
		redPlayerList = players.subList(players.size()/2, players.size());
	}
	
	public int addScrambleVote(PlayerObject player)
	{
		if(!this.scrambleVotes.add(player))
		{
			//player already voted
			return -1;
		}
		if(this.scrambleVotes.size()>=this.scrambleVotesReq)
		{
			this.shuffleTeams();
			this.updateTeamsOnServer();
			this.scrambleVotes.clear();
			return 0;
		}
		return this.scrambleVotes.size();
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
	
	public int getScrambleVotesReq() {
		return scrambleVotesReq;
	}

	public void setScrambleVotesReq(int scrambleVotesReq) {
		this.scrambleVotesReq = scrambleVotesReq;
	}

	public int getNumScrambleVotes() {
		return this.scrambleVotes.size();
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
