package cs455.overlay.node;

public class nodeInformation {
	private String IPAddress;
	private int serverPortNum;
	private int registerSocketPortNum;
	public int getRegisterSocketPortNum() {
		return registerSocketPortNum;
	}

	private String hostName;
	
	public nodeInformation(String IPAddArg, int serverPortArg, String hostNameArg, int regSocketNum){
		IPAddress = IPAddArg;
		serverPortNum = serverPortArg;
		registerSocketPortNum = regSocketNum;
		hostName = hostNameArg;
		
	}
	
	public boolean equals(nodeInformation rhs){
		return IPAddress.equals(rhs.IPAddress) && this.serverPortNum==rhs.serverPortNum;
		
	}
	
	public int getServerPort(){
		return serverPortNum;
	}
	
	public String getHost(){
		return hostName;
	}
	
	public String getHostServerPort(){
		return hostName+":"+serverPortNum;
	}
	
	public String getHostRegPort(){
		return hostName+":"+registerSocketPortNum;
	}
	public boolean equals(MessagingNode rhs){
		return (this.IPAddress.equals(rhs.getIPAddress()) && this.serverPortNum==rhs.getPortNum());
	}
	
	public void print(){
		//System.out.println("IPAddress: "+IPAddress +"\tportNum: "+portNum);
		System.out.println(hostName+":"+serverPortNum);
	}

	
}
