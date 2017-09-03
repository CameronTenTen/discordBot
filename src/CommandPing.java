import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

public class CommandPing implements CommandExecutor
{
	@Command(aliases = {"!ping"}, description = "Pong!")
	public String onCommand(String command, String[] args) {
		return "Pong!";
	}
}