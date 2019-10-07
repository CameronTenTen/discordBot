import java.util.HashMap;
import java.util.Map;

import sx.blah.discord.Discord4J;

/**Manages a list of votes for different vote types.
 * <p>
 * Vote types are added to the object, then votes can be added for each vote type, one vote per player.
 * <p>
 * When vote types are added a required votes count is also specified so that the vote manager can track whether the vote has passed, and clear the votes when it happnes.
 * <p>
 * Additional vote information can also be included with each vote.
 * @author cameron
 *
 */
public class VoteManager {

	//map of all the registered vote types, and the number of votes required to pass each one
	Map<String, Integer> voteTypes;
	//map of vote name (command), to map of players who have voted, and their "vote info" string
	Map<String, Map<PlayerObject, String>> currentVotes;

	VoteManager()
	{
		this.currentVotes = new HashMap<String, Map<PlayerObject, String>>();
		this.voteTypes = new HashMap<String, Integer>();
	}

	/**adds a new vote type with the specified required votes count, if the vote type already exists then the required count will be updated.
	 * <p>
	 * Setting a required count of null will prevent the vote manager from tracking the vote count and clearing the votes when a vote is fulfilled.
	 * <p>
	 * Trying to add a vote for a vote type that has not yet been added via this function will cause an error.
	 * @param voteType the vote type string
	 * @param numVotesRequired the number of votes required for this vote type to pass
	 */
	public void addVoteType(String voteType, Integer numVotesRequired)
	{
		this.voteTypes.put(voteType, numVotesRequired);
	}

	/**removes a vote type so that it can no longer be voted for, also removes all current votes for that type
	 * @param voteType the vote type string
	 */
	public void removeVoteType(String voteType)
	{
		this.voteTypes.remove(voteType);
		this.currentVotes.remove(voteType);
	}

	/**get the number of votes required for the specified vote type
	 * @param voteType the vote type string
	 * @return the number of votes required for this type
	 */
	public int getRequiredVotesCount(String voteType)
	{
		return this.voteTypes.get(voteType);
	}

	/**Add a vote for the specified vote type and player object, with extra vote info. 
	 * <p>
	 * If the vote type doesn't exist, there is a problem with the code that is using the vote manager, it should always initialise vote types.
	 * @param voteType the type of vote to add
	 * @param player the player who is making this vote
	 * @param voteInfo any additional vote info
	 * @return the current number of votes recorded for this vote type, 0 if the required votes have been reached, -1 if the player had already voted, -2 if the vote type doesnt exist
	 */
	public int addVote(String voteType, PlayerObject player, String voteInfo)
	{
		if(!voteTypes.containsKey(voteType))
		{
			Discord4J.LOGGER.error("Bot attempted to add a vote for a vote type that doesnt exist! someone did some bad programming!: "+voteType+" with stack trace: "+Thread.currentThread().getStackTrace());
			return -2;
		}
		Map<PlayerObject, String> votes = currentVotes.get(voteType);
		if(votes == null)
		{
			votes = new HashMap<PlayerObject, String>();
			currentVotes.put(voteType, votes);
		}
		//must check with contains key - the put cant tell us anything because the value could be null
		boolean hadKey = votes.containsKey(player);
		votes.put(player, voteInfo);
		if(hadKey)
		{
			return -1;
		}
		//if the required votes is null, that means we shouldn't track it for this vote type
		Integer numVotesRequired = voteTypes.get(voteType);
		if(numVotesRequired != null && votes.size() >= numVotesRequired)
		{
			this.clearAllVotes(voteType);
			return 0;
		}
		else
		{
			return votes.size();
		}
	}

	/**Wrapper for addVote() function that defaults vote info to null
	 * <p>
	 * the vote info is generally not needed for simple votes
	 * <p>
	 * TODO: find a use case for vote info, it hasnt been used yet but I think it will be useful
	 * @param voteType the type of vote to add
	 * @param player the player who is making this vote
	 * @return 
	 * @see VoteManager#addVote(String, PlayerObject, String, boolean)
	 */
	public int addVote(String voteType, PlayerObject player)
	{
		return addVote(voteType, player, null);
	}

	/**Remove a vote for the specified vote type and player object
	 * @param voteType the vote type to remove a vote for
	 * @param player the player to remove the vote for
	 * @return the updated count of votes for this type, or -2 if there is no record of that vote type, or -1 if the player did not have a vote of that type
	 */
	public int removeVote(String voteType, PlayerObject player)
	{
		Map<PlayerObject, String> votes = currentVotes.get(voteType);
		if(votes == null) {
			if(!voteTypes.containsKey(voteType))
			{
				return -2;
			}
		}
		else if(votes.containsKey(player))
		{
			votes.remove(player);
			return votes.size();
		}
		return -1;
	}

	/**The current number of votes counted for the specified vote count
	 * @param voteType the vote type string
	 * @return the the current number of votes
	 */
	public int currentVoteCount(String voteType)
	{
		return currentVotes.get(voteType).size();
	}

	/**Remove all current votes for the specified vote type
	 * @param voteType the vote type string
	 * @return true if some votes were removed, false if there was no votes already
	 */
	public boolean clearAllVotes(String voteType)
	{
		Map<PlayerObject, String> votes = currentVotes.remove(voteType);
		if(votes != null && votes.size() > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
