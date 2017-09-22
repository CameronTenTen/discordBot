import java.io.IOException;
import sx.blah.discord.Discord4J;

public class GatherServer
{
	private String ip;
	private int port;
	private String rconPassword;				//sv_rconpassword
	private String serverPassword;				//sv_password
	private String serverLink;				//kag://127.0.0.1:50301/password
	private KagServerChecker serverCheckObject;
	private boolean isInUse;
	
	private Thread rconThread;
	
	GatherServer(String ip, int port, String rconPassword, String serverPassword, String serverLink)
	{
		this.ip=ip;
		this.port=port;
		this.rconPassword=rconPassword;
		this.serverPassword=serverPassword;
		this.serverLink=serverLink;
		this.isInUse=false;
	}
	
	public void connect()
	{
		try {
			serverCheckObject = new KagServerChecker(ip, port, "physics");
			serverCheckObject.addListener(new RconListener());
			rconThread = new Thread(serverCheckObject);
			rconThread.start();
			Discord4J.LOGGER.info("Connected to KAG server: "+ip+":"+port+"/"+serverPassword);
		} catch (IOException e) {
			Discord4J.LOGGER.error("An error occured connecting to the gather kag server("+e.getMessage()+"): "+ip+":"+port+"/"+serverPassword);
		}
	}
	
	public void disconnect()
	{
		if(rconThread != null)
		{
			rconThread.interrupt();
		}
	}
	
	public void sendMessage(String msg)
	{
		serverCheckObject.sendMessage(msg);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return ((GatherServer)obj).ip.equals(this.ip) && ((GatherServer)obj).port == this.port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServerPassword() {
		return serverPassword;
	}

	public void setServerPassword(String serverPassword) {
		this.serverPassword = serverPassword;
	}

	public String getServerLink() {
		return serverLink;
	}

	public void setServerLink(String serverLink) {
		this.serverLink = serverLink;
	}

	public boolean isInUse() {
		return isInUse;
	}

	public void setInUse(boolean isInUse) {
		this.isInUse = isInUse;
	}
}
