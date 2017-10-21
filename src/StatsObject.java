
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
	
	public float winRate()
	{
		float winRate;
		if((wins+losses+desertions)==0)
			winRate=0;
		else
			winRate = (((float)wins+(float)substitutionwins)/((float)gamesplayed+(float)desertionlosses+(float)substitutionwins))*100;
		
		return winRate;
	}
	
	public String winRateString()
	{
		if(winRate>0) return String.format("%.2f", this.winRate);
		else return String.format("%.2f", this.winRate());
	}
	
	@Override
	public String toString()
	{
		return "Games Played: "+gamesplayed+" Win Rate: "+this.winRateString()+"% Wins: "+wins+" Losses: "+losses+" Desertions: "+desertions+" Substitutions: "+substitutions+" Desertion losses: "+desertionlosses+" Substitution wins: "+substitutionwins+" Rank: "+mmr;
	}
}
