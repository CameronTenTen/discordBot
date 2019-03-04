import java.util.HashMap;
import java.util.Map;

import sx.blah.discord.Discord4J;

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

	/**adds a new vote type with the specified required votes count, if the vote type already exists then the required count will be updated
	 * trying to add a vote for a vote type that has not yet been added will cause an error
	 * @param voteType the vote type string
	 * @param numVotesRequired the number of votes required for this vote type to pass
	 */
	public void addVoteType(String voteType, Integer numVotesRequired)
	{
		this.voteTypes.put(voteType, numVotesRequired);
	}

	public void removeVoteType(String voteType)
	{
		this.voteTypes.remove(voteType);
		this.currentVotes.remove(voteType);
	}

	public int getRequiredVotesCount(String voteType)
	{
		return this.voteTypes.get(voteType);
	}

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

	public int addVote(String voteType, PlayerObject player)
	{
		return addVote(voteType, player, null);
	}

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

	public int currentVoteCount(String voteType)
	{
		return currentVotes.get(voteType).size();
	}

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
