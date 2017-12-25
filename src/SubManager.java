import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sx.blah.discord.handle.obj.IUser;

/**Manages all of the substitutions for one GatherObject/queue
 * @author cameron
 *
 */
public class SubManager {
	
	GatherObject gather;
	
	List<SubRequestObject> subRequests;
	List<SubVoteObject> subVotes;
	
	//not possible for this to be less than 1 with current implementation (see add sub vote code)
	private int subVotesRequired = 3;
	
	SubManager(GatherObject gatherObj)
	{
		gather = gatherObj;
		subRequests = new ArrayList<SubRequestObject>();
		subVotes = new ArrayList<SubVoteObject>();
	}

	/**Getter for the total number of sub votes needed to sub a player
	 * @return the required number of sub votes
	 */
	public int getSubVotesRequired() {
		return subVotesRequired;
	}

	/**Setter for the total number of sub votes needed to sub a player
	 * @param subVotesRequired the new number of sub votes required
	 */
	public void setSubVotesRequired(int subVotesRequired) {
		this.subVotesRequired = subVotesRequired;
	}
	
	/**Checks if there are any sub requests
	 * @return true if subRequests is not empty, false otherwise
	 */
	public boolean hasSubRequest()
	{
		return !subRequests.isEmpty();
	}
	
	/**Checks if there is a sub request for a specific game
	 * @return true if subRequests contains a request with the game id, false otherwise
	 */
	public boolean hasSubRequest(int gameId)
	{
		if(getFirstSubRequest(gameId) != null) return true;
		else return false;
	}
	
	/**Checks if there is a sub request for a specific player
	 * @param playerToSub the player to check
	 * @return true if they are found in the list of sub requests, false otherwise
	 */
	public boolean hasSubRequest(PlayerObject playerToSub)
	{
		for(SubRequestObject req : subRequests)
		{
			if(req.playerToBeReplaced.equals(playerToSub)) return true;
		}
		return false;
	}
	
	/**Add a sub request for a player
	 * @param subObj a SubRequestObject specifing who should be subbed and the game they are in
	 * @return -1 if the game is null, 0 if a sub request already exists for this player, 1 if the sub request was added.
	 */
	public int addSubRequest(SubRequestObject subObj)
	{
		if(subObj.game==null)
		{
			//player isnt playing a game
			return -1;
		}
		else
		{
			if(subRequests.contains(subObj)) return 0;
			
			subRequests.add(subObj);
			removeSubVotes(subObj.playerToBeReplaced);
			return 1;
		}
	}
	
	/**Wrapper function for adding a sub request for a player. Takes the PlayerObject of the player to be subbed and the GatherGame object of the game they are playing in. 
	 * @param playerToBeSubbed the PlayerObject of the player to be subbed
	 * @param game the GatherGame object of the game they are playing in
	 * @return -1 if game is null, 0 if there is already a sub, 1 if the sub request was added
	 * @see #addSubRequest(SubRequestObject)
	 */
	public int addSubRequest(PlayerObject playerToBeSubbed, GatherGame game)
	{
		return addSubRequest(new SubRequestObject(playerToBeSubbed, game));
	}

	/**Wrapper function for adding a sub request for a player. Takes the Discord user object of the player to be subbed and the GatherGame object of the game they are playing in. 
	 * @param user the Discord user object of the player to be subbed
	 * @param game the GatherGame object of the game they are playing in
	 * @return -1 if game is null, 0 if there is already a sub, 1 if the sub request was added
	 * @see #addSubRequest(SubRequestObject)
	 */
	public int addSubRequest(IUser user, GatherGame game)
	{
		return addSubRequest(DiscordBot.players.getObject(user), game);
	}
	
	/**Remove the a players sub request. 
	 * @param player the player that no longer needs to be subbed
	 * @return true if they were removed, false otherwise
	 */
	public boolean removeSubRequest(PlayerObject player)
	{
		if(player==null) return false;
		for(SubRequestObject req : subRequests)
		{
			if(req.playerToBeReplaced.equals(player)) return subRequests.remove(req);
		}
		return false;
	}
	
	/**Wrapper function for removing a sub request from a discord user object. 
	 * @param user the user that no longer needs to be subbed
	 * @return true if they were removed, false otherwise
	 */
	public boolean removeSubRequest(IUser user)
	{
		return removeSubRequest(DiscordBot.players.getObject(user));
	}
	
	/**Wrapper function for removing a sub request using a SubRequestObject. 
	 * @param obj the SubRequestObject representing the player and game
	 * @return true if they were removed, false otherwise
	 * @see #removeSubRequest(PlayerObject)
	 */
	public boolean removeSubRequest(SubRequestObject obj)
	{
		return removeSubRequest(obj.playerToBeReplaced);
	}
	
	/**Get the oldest open sub request
	 * @return null if there are no sub request, otherwise the first request in the list.
	 */
	private SubRequestObject getFirstSubRequest()
	{
		if(subRequests.isEmpty()) return null;
		return subRequests.remove(0);
	}
	
	/**Make a substitution. Replaces the player in the game, removes the request, and returns a SubstitutionObject of the substitution. 
	 * @param sub the SubRequestObject for the request being filled
	 * @param playerTakingSpot the PlayerObject of the player taking the postion
	 * @return the SubstitutionObject with info on the player replaced, the player that took their spot, and the game they are in
	 */
	private SubstitutionObject makeSub(SubRequestObject sub, PlayerObject playerTakingSpot)
	{
		PlayerObject playerBeingReplaced = sub.playerToBeReplaced;
		sub.game.replacePlayer(playerBeingReplaced, playerTakingSpot);
		removeSubRequest(sub);
		return new SubstitutionObject(playerBeingReplaced, playerTakingSpot, sub.game);
	}
	
	/**Wrapper for making a substitution that is called when someone wants to sub into a game. Takes the PlayerObject and checks if there is any subs are needed. 
	 * @param player the player that is subbing in
	 * @return null if no subs needed, otherwise the SubstitutionObject describing the substitution that was made
	 */
	public SubstitutionObject subPlayerIntoGame(PlayerObject player)
	{
		SubRequestObject sub = getFirstSubRequest();
		if(sub == null) return null;
		else return makeSub(sub, player);
	}
	
	
	
	/**Adds the record of a substitution vote for a player. First checks the players are in the same game, then checks if there is not already a sub request for the player. 
	 * Then checks for existing sub votes, adding this one if there is not already an identical vote. If enough votes have been made, they are removed and a sub request is made. 
	 * @param playerVotedFor the player that is being voted to be subbed out
	 * @param playerVoting the player voting to sub someone
	 * @return -1 if the voter is not in a game, -2 if the voted is not in a game, -3 if the players are in different games, -4 if this player already has a sub request, -5 if the voter has already voted to sub them, 
	 * 0 if sufficient votes have been made to request a sub, or a number greater than 0 representing the total number of sub votes that have been made for the player. 
	 */
	public int addSubVote(PlayerObject playerVotedFor, PlayerObject playerVoting)
	{
		GatherGame voterGame = gather.getPlayersGame(playerVoting);
		GatherGame votedGame = gather.getPlayersGame(playerVotedFor);
		if(voterGame == null)
		{
			//voter not in game
			return -1;
		}
		if(votedGame == null)
		{
			//voted on not in a game
			return -2;
		}
		if(!votedGame.equals(voterGame))
		{
			//voter and voted must be in same game
			return -3;
		}
		
		if(hasSubRequest(playerVotedFor)) return -4;
		SubVoteObject votes = getSubVotesForPlayer(playerVotedFor);
		if(votes != null)
		{
			if(!votes.addSubVote(playerVoting))
			{
				//gets here if failed to add the sub vote, which probably means the voter has already voted to sub this player
				return -5;
			}
			else
			{
				if(votes.numVotes() >= getSubVotesRequired())
				{
					removeSubVotes(playerVotedFor);
					addSubRequest(playerVotedFor, votedGame);
					//votes filled and sub request added
					return 0;
				}
				return votes.numVotes();
			}
		}
		else
		{
			subVotes.add(new SubVoteObject(playerVotedFor, votedGame, playerVoting));
			return 1;
		}
	}
	
	/**Wrapper function for adding a sub vote from Discord User objects. 
	 * @param votedFor the user object of the player to sub
	 * @param playerVoting the user object of the player voting for a sub
	  * @return -1 if the voter is not in a game, -2 if the voted is not in a game, -3 if the players are in different games, -4 if this player already has a sub request, -5 if the voter has already voted to sub them, 
	 * 0 if sufficient votes have been made to request a sub, or a number greater than 0 representing the total number of sub votes that have been made for the player. 
	 * @see #addSubVote(PlayerObject, PlayerObject)
	 */
	public int addSubVote(IUser votedFor, IUser playerVoting)
	{
		return addSubVote(DiscordBot.players.getObject(votedFor), DiscordBot.players.getObject(playerVoting));
	}

	/**Remove all the sub votes for a player. 
	 * @param playerVotedFor the player to remove sub votes for
	 * @return true if votes were removed, false if the player wasn't found
	 */
	public boolean removeSubVotes(PlayerObject playerVotedFor)
	{
		if(playerVotedFor==null) return false;
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

	/**Wrapper function for removing all the sub votes for a player from discord user object. 
	 * @param playerVotedFor the player to remove sub votes for
	 * @return true if votes were removed, false if the player wasn't found
	 */
	public boolean removeSubVotes(IUser playerVotedFor)
	{
		return removeSubVotes(DiscordBot.players.getObject(playerVotedFor));
	}
	
	/**Wrapper function for removing all sub votes for a game. 
	 * @param game the game to clear sub votes for
	 * @return true if any votes were removed, false otherwise
	 */
	public boolean removeSubVotes(GatherGame game)
	{
		boolean returnVal = false;
		if(game==null) return returnVal;
		for(SubVoteObject obj : subVotes)
		{
			if(obj.getGame().equals(game))
			{
				subVotes.remove(obj);
				returnVal = true;
			}
		}
		return returnVal;
	}
	
	/**Getter for the SubVoteObject of a player. 
	 * @param player the player to find the SubVoteObject of
	 * @return the SubVoteObject if it was found, null otherwise
	 */
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
	
	/**Count the current number of sub votes for this player. 
	 * @param playerToBeSubbed the player to look for
	 * @return the number of votes
	 */
	public int getNumSubVotesForPlayer(PlayerObject playerToBeSubbed)
	{
		SubVoteObject votes = getSubVotesForPlayer(playerToBeSubbed);
		if(votes==null) return 0;
		else return votes.numVotes();
	}
	
	/**Helper function for getting a shallow copy of the currently open sub requests for a game. (the List is new but the PlayerObjects are not(should not copy the PlayerObject's because then they would be unmanaged by the PlayerObjectManager))
	 * @param game the game to find
	 * @return a List of PlayerObject for all the currently requested subs of this game. 
	 */
	public List<PlayerObject> getOpenSubs(GatherGame game)
	{
		List<PlayerObject> returnList = new ArrayList<PlayerObject>();
		for(SubRequestObject obj : subRequests)
		{
			if(obj.game.equals(game)) returnList.add(obj.playerToBeReplaced);
		}
		if(returnList.isEmpty()) return null;
		else return returnList;
	}
	
	/**Clear all sub request and sub votes. 
	 */
	public void clearSubs()
	{
		subRequests.clear();
		subVotes.clear();
	}
	
	/**Clear all the sub requests and sub votes for one game. 
	 * @param game the game to be cleared
	 */
	public void clearGame(GatherGame game)
	{
		Iterator<SubRequestObject> i = subRequests.iterator();
		while (i.hasNext())
		{
			SubRequestObject obj = i.next();
			if(obj.game.equals(game))
			{
				i.remove();
			}
		}
		Iterator<SubVoteObject> it = subVotes.iterator();
		while (it.hasNext())
		{
			SubVoteObject obj = it.next();
			if(obj.getGame().equals(game))
			{
				it.remove();
			}
		}
	}
	
	/**All of the current sub request in a string. 
	 * @return a blank string if there is no subs, or List.toString() of the sub requests otherwise. 
	 * @see java.lang.Object#toString()
	 * @see List#toString()
	 */
	@Override
	public String toString()
	{
		if(subRequests.isEmpty())
			return "";
		else
			return subRequests.toString();
	}

}
