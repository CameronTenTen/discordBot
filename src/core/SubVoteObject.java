package core;
import java.util.HashSet;
import java.util.Set;

/**Tracks all the sub votes for one player
 * @author cameron
 *
 */
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
	
	/**Getter for the player to be subbed. 
	 * @return the PlayerObject this SubVoteObject is for
	 */
	public PlayerObject playerToBeSubbed()
	{
		return player;
	}
	
	/**Getter for all the players currently voting to sub this player. 
	 * @return a set of PlayerObject
	 */
	public Set<PlayerObject> getCurrSubVotes()
	{
		return votesForThisPlayer;
	}
	
	/**Check if a player is already voting to sub this player. 
	 * @param player the PlayerObject to find
	 * @return true if the player is found in the set of votes for this player, false otherwise
	 */
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
	
	/**Add a sub vote for this player. 
	 * @param player the player voting for a sub 
	 * @return true if the sub vote was added, false otherwise
	 */
	public boolean addSubVote(PlayerObject player)
	{
		return votesForThisPlayer.add(player);
	}
	
	/**Remove a sub vote for a player. 
	 * @param player the player that should no longer be voting for a sub
	 * @return true if the sub vote was removed, false otherwise
	 */
	public boolean removeSubVote(PlayerObject player)
	{
		return votesForThisPlayer.remove(player);
	}
	
	/**Getter for the number of sub votes currently counted for this player. 
	 * @return the current number of sub votes
	 */
	public int numVotes()
	{
		return votesForThisPlayer.size();
	}

	/**Getter for the game this player is playing in.
	 * @return the GatherGame object for this player
	 */
	public GatherGame getGame() {
		return game;
	}

	/**Setter for the game this player is playing in. 
	 * @param game the GatherGame object this player is in. 
	 */
	public void setGame(GatherGame game) {
		this.game = game;
	}
}
