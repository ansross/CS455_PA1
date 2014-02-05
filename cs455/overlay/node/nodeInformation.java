package cs455.overlay.node;

public class nodeInformation {
	private String IPAddress;
	private int portNum;
	private String hostName;
	
	public nodeInformation(String IPAddArg, int portArg, String hostNameArg){
		IPAddress = IPAddArg;
		portNum = portArg;
		hostName = hostNameArg;
		
	}
	
	public boolean equals(nodeInformation rhs){
		return IPAddress.equals(rhs.IPAddress) && this.portNum==rhs.portNum;
		
	}
	
	public String getHostPort(){
		return hostName+":"+portNum;
	}
	
	public boolean equals(MessagingNode rhs){
		return (this.IPAddress.equals(rhs.getIPAddress()) && this.portNum==rhs.getPortNum());
	}
	
	public void print(){
		//System.out.println("IPAddress: "+IPAddress +"\tportNum: "+portNum);
		System.out.println(hostName+":"+portNum);
	}

	
}
