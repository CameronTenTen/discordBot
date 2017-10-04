
public class StatsObject {
	StatsObject()
	{
		kagname = "";
		discordid = 0L;
		gamesPlayed = 0;
		wins = 0;
		losses = 0;
		draws = 0;
		desertions = 0;
		substitutions = 0;
		winRate = -1.0f;
	}
	String kagname;
	long discordid;
	int gamesPlayed;
	int wins;
	int losses;
	int draws;
	int desertions;
	int substitutions;
	float winRate;
	
	public float winRate()
	{
		float winRate;
		if((wins+losses+desertions)==0)
			winRate=0;
		else
			winRate = ((float)wins/((float)wins+(float)losses+(float)desertions))*100;
		
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
		return "Games Played: "+gamesPlayed+" Win Rate: "+this.winRateString()+"% Wins: "+wins+" Losses: "+losses+" Desertions: "+desertions+" Substitutions: "+substitutions;
	}
}
