import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public interface GenericReactCallbackObject {
	public abstract void execute(IUser user, IGuild guild);
}
