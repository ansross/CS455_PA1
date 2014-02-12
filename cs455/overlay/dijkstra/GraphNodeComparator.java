package cs455.overlay.dijkstra;

import java.util.Comparator;

public class GraphNodeComparator implements Comparator<GraphNode>{
	@Override
	public int compare(GraphNode lhs, GraphNode rhs){
		//TODO check this!!!
		return lhs.getDistance()-rhs.getDistance();
	}
	
	
}