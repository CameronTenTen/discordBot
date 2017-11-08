
/**Object for holding a players stats when they are retreived from the database
 * @author cameron
 *
 */
public class StatsObject {
	StatsObject()
	{
		kagname = "";
		discordid = 0L;
		gamesplayed = 0;
		wins = 0;
		losses = 0;
		draws = 0;
		desertions = 0;
		substitutions = 0;
		desertionlosses = 0;
		substitutionwins = 0;
		winRate = -1.0f;
		mmr=0;
	}
	String kagname;
	long discordid;
	int gamesplayed;
	int wins;
	int losses;
	int draws;
	int desertions;
	int substitutions;
	int desertionlosses;
	int substitutionwins;
	float winRate;
	int mmr;
	
	/**Calculates the win rate of the player based on ((wins+subwins)/(gamesplayed+desertionlosses+subwins))*100. This is no longer used, win rate is calculated by the database query instead. 
	 * @return the calculated win percentage, or 0 if the player has 0 wins, 0 losses, and 0 desertions
	 */
	public float winRate()
	{
		float winRate;
		if((wins+losses+desertions)==0)
			winRate=0;
		else
			winRate = (((float)wins+(float)substitutionwins)/((float)gamesplayed+(float)desertionlosses+(float)substitutionwins))*100;
		
		return winRate;
	}
	
	/**Gets the win percent of this player as a string 
	 * @return a string of the win rate to two decimal places
	 */
	public String winRateString()
	{
		if(winRate>0) return String.format("%.2f", this.winRate);
		else return String.format("%.2f", this.winRate());
	}
	
	/**Helper for getting a players stats in a readable form. 
	 * @return a string containing all the players stats in a readable form
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Games Played: "+gamesplayed+" Win Rate: "+this.winRateString()+"% Wins: "+wins+" Losses: "+losses+" Desertions: "+desertions+" Substitutions: "+substitutions+" Desertion losses: "+desertionlosses+" Substitution wins: "+substitutionwins+" Rank: "+mmr;
	}
}
