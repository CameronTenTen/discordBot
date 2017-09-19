import java.util.HashSet;
import java.util.Set;

public class SubVoteObject {
	
	private PlayerObject player = null;
	
	private GatherGame game = null;
	
	private Set<PlayerObject> votesForThisPlayer;
	
	private SubVoteObject()
	{
		votesForThisPlayer = new HashSet<PlayerObject>();
	}
	
	private SubVoteObject(PlayerObject playerVotedFor)
	{
		this();
		this.player = playerVotedFor;
	}
	
	private SubVoteObject(PlayerObject playerVotedFor, GatherGame game)
	{
		this(playerVotedFor);
		this.setGame(game);
	}
	
	SubVoteObject(PlayerObject playerVotedFor, GatherGame game, PlayerObject voter)
	{
		this(playerVotedFor, game);
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

	public GatherGame getGame() {
		return game;
	}

	public void setGame(GatherGame game) {
		this.game = game;
	}
}
