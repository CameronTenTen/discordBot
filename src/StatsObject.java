
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
		mmr = new MmrObject();
		//mmr=0;
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
	MmrObject mmr;
	//int mmr;
	
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

	/**Getter for new MMR calculations
	 * @return MMR as double
	 */
	public double getMmrDouble()
    {
		if (mmr.getMmr() == -1) {
			return calculateMmr(wins - ((desertions + 0.0) / 2), gamesplayed);
		}
		return mmr.getMmr();
	}

	/**Wrapper function to get MMR as 4 digit integer
	 * @return MMR as integer
	 */
	public int getMmrInteger()
    {
		if (mmr.getMmr() == -1) {
			return (int) (getMmrDouble() * 10000);
		}
		return (int) mmr.getMmr();
	}
	
	/**Function for calculating MMR using new algorithm
	 * @return MMR as double
	 * @param pos user's wins (optional desertion punishment)
	 * @param n user's total games
	 */
	public double calculateMmr(Double pos, int n)
	{
		if (pos <= 0 || n == 0)
		{
			return 0;
		}

		double z;
		double phat;
        
        z = 1.96;
		phat = 1 * pos / n;

		return (phat + z*z/(2*n) - z * Math.sqrt((phat*(1-phat)+z*z/(4*n))/n))/(1+z*z/n);

		// For SQL query: ((1*(CASE WHEN wins-(desertions/2)<0 THEN 0 ELSE wins-(desertions/2) END))/gamesplayed)+1.96*1.96/(2*gamesplayed)-1.96*SQRT(((1*(wins-(desertions/2))/gamesplayed)*(1-(1*(wins-(desertions/2))/gamesplayed))+1.96*1.96/(4*gamesplayed))/gamesplayed))/(1+1.96*1.96/gamesplayed)
	}
}
