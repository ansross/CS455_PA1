package cs455.overlay.dijkstra;

import java.util.ArrayList;
import java.util.Hashtable;

import cs455.overlay.wireformats.LinkInfo;


public class GraphNode {
	private String ID;
	private int distance;
	private GraphNode previous;
	Hashtable <String, LinkInfo> neighborWeights;
	ArrayList<String> neighborIDs;
	//ArrayList<LinkWeights> neighborWeights;
	
	public GraphNode(String ID, Hashtable<String, LinkInfo> neighWeightArg, ArrayList<String> neighArg){
		this.ID = ID;
		this.neighborWeights = new Hashtable<String, LinkInfo>(neighWeightArg);
		this.neighborIDs = new ArrayList<String>(neighArg);
	}
	
	public GraphNode(String ID){
		
		this.ID = ID;
		this.neighborIDs = new ArrayList<String>();
		this.neighborWeights = new Hashtable<String, LinkInfo>();
	}
	
	public void addNeighborWithWeight(String neighbor, LinkInfo neighInfo){
		this.neighborIDs.add(neighbor);
		this.neighborWeights.put(neighbor, neighInfo);
	}
	
	public String getID(){
		return ID;
	}
	
	public ArrayList<String> getNeighborIDs(){
		return neighborIDs;
	}
	
	public boolean equals(GraphNode rhs){
		return ID.equals(ID);
	}
	
	public int getDistance(){
		return distance;
	}

	public void setDistance(int value) {
		distance=value;
		// TODO Auto-generated method stub
		
	}

	public void setPrevious(GraphNode nodeArg) {
		previous = nodeArg;
		// TODO Auto-generated method stub
		
	}

	public Hashtable<String, LinkInfo> getNeighborWeights() {
		// TODO Auto-generated method stub
		return neighborWeights;
	}

	public GraphNode getPrevious() {
		// TODO Auto-generated method stub
		return previous;
	}
	
}
