import java.util.HashSet;
import java.util.Set;

public class SubVoteObject {
	
	private PlayerObject player = null;
	
	private Set<PlayerObject> votesForThisPlayer;
	
	SubVoteObject()
	{
		votesForThisPlayer = new HashSet<PlayerObject>();
	}
	
	SubVoteObject(PlayerObject playerVotedFor)
	{
		this();
		player = playerVotedFor;
		
	}
	
	SubVoteObject(PlayerObject playerVotedFor, PlayerObject voter)
	{
		this(playerVotedFor);
		votesForThisPlayer.add(voter);
	}
	
	public PlayerObject playerToBeSubbed()
	{
		return player;
	}
	
	public Set<PlayerObject> getCurrSubVotes()
	{
		return votesForThisPlayer;
	}
	
	public boolean isVotingForSub(PlayerObject player)
	{
		for(PlayerObject p : votesForThisPlayer)
		{
			if(p.equals(player))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean addSubVote(PlayerObject player)
	{
		return votesForThisPlayer.add(player);
	}
	
	public boolean removeSubVote(PlayerObject player)
	{
		return votesForThisPlayer.remove(player);
	}
	
	public int numVotes()
	{
		return votesForThisPlayer.size();
	}
}
