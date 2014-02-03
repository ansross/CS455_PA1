package cs455.overlay.node;

public class nodeInformation {
	private String IPAddress;
	private int portNum;
	
	public nodeInformation(String IPAddArg, int portArg){
		IPAddress = IPAddArg;
		portNum = portArg;
	}
	
	public boolean equals(nodeInformation rhs){
		return IPAddress.equals(rhs.IPAddress) && this.portNum==rhs.portNum;
		
	}
	
	public boolean equals(MessagingNode rhs){
		return (this.IPAddress.equals(rhs.getIPAddress()) && this.portNum==rhs.getPortNum());
	}
	
	public void print(){
		System.out.println("IPAddress: "+IPAddress +"\tportNum: "+portNum);
	}

	
}
