import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	
	private List<PlayerObject> subRequests;
	private List<SubVoteObject> subVotes;
	
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
		subRequests = new ArrayList<PlayerObject>();
		subVotes = new ArrayList<SubVoteObject>();
	}
	
	public List<PlayerObject> getCurrentSubRequests()
	{
		return subRequests;
	}
	
	public boolean hasSubRequest(PlayerObject playerToSub)
	{
		return subRequests.contains(playerToSub);
	}
	
	public boolean addSubRequest(PlayerObject playerToSub)
	{
		if(hasSubRequest(playerToSub)) return false;
		return subRequests.add(playerToSub);
	}
	
	public void removeSubRequest(PlayerObject player)
	{
		subRequests.remove(player);
	}
	
	public boolean removeSubVotes(PlayerObject playerVotedFor)
	{
		for(SubVoteObject obj : subVotes)
		{
			if(obj.playerToBeSubbed().equals(playerVotedFor))
			{
				subVotes.remove(obj);
				return true;
			}
		}
		return false;
	}
	
	private SubVoteObject getSubVotesForPlayer(PlayerObject player)
	{
		for(SubVoteObject obj : subVotes)
		{
			if (obj.playerToBeSubbed().equals(player))
			{
				return obj;
			}
		}
		return null;
	}
	
	public int getNumSubVotesForPlayer(PlayerObject playerToBeSubbed)
	{
		SubVoteObject votes = getSubVotesForPlayer(playerToBeSubbed);
		return votes.numVotes();
	}
	
	public int addSubVote(PlayerObject playerVotedFor, PlayerObject playerVoting)
	{
		if(hasSubRequest(playerVotedFor)) return -1;
		SubVoteObject votes = getSubVotesForPlayer(playerVotedFor);
		if(votes != null)
		{
			if(!votes.addSubVote(playerVoting))
			{
				//gets here if failed to add the sub vote, which probably means the voter has already voted to sub this player
				return -2;
			}
			else
			{
				return votes.numVotes();
			}
		}
		else
		{
			subVotes.add(new SubVoteObject(playerVotedFor, playerVoting));
			return 1;
		}
	}
	
	public SubstitutionObject subPlayerIntoGame(PlayerObject player)
	{
		List<PlayerObject> subs = getCurrentSubRequests();
		if(subs.isEmpty())
		{
			//no spaces available
			return null;
		}
		else
		{
			PlayerObject playerBeingReplaced = subs.get(0);
			int team = getPlayerTeam(playerBeingReplaced);
			replacePlayer(playerBeingReplaced, player);
			removeSubRequest(playerBeingReplaced);
			return new SubstitutionObject(playerBeingReplaced, player, team);
		}
	}
	
	public SubstitutionObject subPlayerIntoGame(IUser user)
	{
		return subPlayerIntoGame(new PlayerObject(user));
	}
	
	public void replacePlayer(PlayerObject playerBeingReplaced, PlayerObject player)
	{
		System.out.println(players.toString());
		System.out.println(bluePlayerList.toString());
		System.out.println(redPlayerList.toString());
		int team = getPlayerTeam(playerBeingReplaced);
		if(team == 0)
		{
			bluePlayerList.set(bluePlayerList.indexOf(playerBeingReplaced), player);
		}
		else if(team == 1)
		{
			redPlayerList.set(redPlayerList.indexOf(playerBeingReplaced), player);
		}
		else
		{
			return;
		}
		//only gets here to change player array if they were on a team
		System.out.println(players.toString());
		System.out.println(bluePlayerList.toString());
		System.out.println(redPlayerList.toString());
		int index = players.indexOf(playerBeingReplaced);
		System.out.println(index);
		if(index >=0) players.set(index, player);
		return;
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
