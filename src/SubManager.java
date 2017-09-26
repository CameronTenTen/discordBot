import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sx.blah.discord.handle.obj.IUser;

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

	public int getSubVotesRequired() {
		return subVotesRequired;
	}

	public void setSubVotesRequired(int subVotesRequired) {
		this.subVotesRequired = subVotesRequired;
	}
	
	public boolean hasSubRequest()
	{
		return !subRequests.isEmpty();
	}
	
	public boolean hasSubRequest(PlayerObject playerToSub)
	{
		for(SubRequestObject req : subRequests)
		{
			if(req.playerToBeReplaced.equals(playerToSub)) return true;
		}
		return false;
	}
	
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
	
	public int addSubRequest(PlayerObject playerToBeSubbed, GatherGame game)
	{
		return addSubRequest(new SubRequestObject(playerToBeSubbed, game));
	}
	
	public int addSubRequest(IUser user, GatherGame game)
	{
		return addSubRequest(DiscordBot.players.getObject(user), game);
	}
	
	public void removeSubRequest(PlayerObject player)
	{
		for(SubRequestObject req : subRequests)
		{
			if(req.playerToBeReplaced.equals(player)) subRequests.remove(req);
		}
	}
	
	public void removeSubRequest(SubRequestObject obj)
	{
		subRequests.remove(obj);
	}
	
	private SubRequestObject getFirstSubRequest()
	{
		if(subRequests.isEmpty()) return null;
		return subRequests.remove(0);
	}
	
	private SubstitutionObject makeSub(SubRequestObject sub, PlayerObject playerTakingSpot)
{
		PlayerObject playerBeingReplaced = sub.playerToBeReplaced;
		sub.game.replacePlayer(playerBeingReplaced, playerTakingSpot);
		removeSubRequest(sub);
		return new SubstitutionObject(playerBeingReplaced, playerTakingSpot, sub.game);
	}
	
	public SubstitutionObject subPlayerIntoGame(PlayerObject player)
	{
		SubRequestObject sub = getFirstSubRequest();
		if(sub == null) return null;
		else return makeSub(sub, player);
	}
	
	
	
	private int addSubVote(PlayerObject playerVotedFor, PlayerObject playerVoting)
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
			//voted on in a game
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
	
	public int addSubVote(IUser votedFor, IUser playerVoting)
	{
		return addSubVote(DiscordBot.players.getObject(votedFor), DiscordBot.players.getObject(playerVoting));
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
	
	public void clearSubs()
	{
		subRequests.clear();
		subVotes.clear();
	}
	
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
	
	@Override
	public String toString()
	{
		if(subRequests.isEmpty())
			return "";
		else
			return subRequests.toString();
	}

}
