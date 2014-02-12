package cs455.overlay.wireformats;

public class LinkInfo {
	private String hostA;
	private String hostB;
	private int hostAServerPortNum;
	private int hostBServerPortNum;
	private int weight;
	
	public int getWeight(){
		return weight;
	}
	
	public String getHostBPortB(){
		return hostB+":"+hostBServerPortNum;
	}
	
	public String getHostAPortA(){
		return hostA+":"+hostAServerPortNum;
	}
	
	public void setWeight(int weight){
		if(weight < 0 || weight > 10){
			System.out.println("ERROR: WEIGHT " + weight + " OUT OF BOUNDS");
		}
		this.weight = weight;
	}
	
	public LinkInfo(String A, int portA, String B,  int portB, int weight){
		hostA = A;
		hostB = B;
		hostAServerPortNum = portA;
		hostBServerPortNum = portB;
		this.weight = weight;
	}
	
	public LinkInfo(String A,  int portA, String B, int portB){
		hostA = A;
		hostB = B;
		hostAServerPortNum = portA;
		hostBServerPortNum = portB;
		this.weight=-1;
	}
	
	public String getFullInfo(){
		return hostA+":"+hostAServerPortNum+" "+hostB+":"+hostBServerPortNum+" "+weight;
	}

}
