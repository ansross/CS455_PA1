package cs455.overlay.dijkstra;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PriorityQueue;

import cs455.overlay.wireformats.LinkInfo;
import cs455.overlay.wireformats.Protocol;

public class Dijkstra {

	//given a graph, a starting and an ending vertex, 
	//@return arraylist of graphNodes constituting shortest path
	public static ArrayList<String> getShortestPath(ArrayList<GraphNode> graph, String sourceString, String targetString){
		GraphNode source = null;
		GraphNode target = null;
		for(GraphNode node: graph){
			if(node.getID().equals(sourceString)){
				source = node;
			}
			if(node.getID().equals(targetString)){
				target = node;
			}
		}
		if(source == null || target == null){
			System.out.println("ERROR: could not find target and/or source in graph");
			return null;
		}
		
		Comparator<GraphNode> comp = new GraphNodeComparator();
		PriorityQueue<GraphNode> PQ = new PriorityQueue<GraphNode>(graph.size(), comp);
		ArrayList<GraphNode> explored = new ArrayList<GraphNode>(graph.size());
		graph.get(graph.indexOf(source)).setDistance(0);
		for(GraphNode vertex:graph ){
			if(!vertex.equals(source)){
				//as close to infinity as I'm getting
				vertex.setDistance(Integer.MAX_VALUE);
				vertex.setPrevious(null);
			}
			PQ.add(vertex);
		}
		
		while(!PQ.isEmpty()){
			//extract min
			GraphNode vertex = PQ.remove();
			//at target, done
			if(vertex.equals(target)){
				break;
			}
			//keep track of those we've relaxed
			if(Protocol.DEBUG){
				System.out.println("pulled vertex distance: " + vertex.getDistance());
				System.out.println("next vertex's distance: " + PQ.peek().getDistance());
				if(vertex.getDistance() > PQ.peek().getDistance()){
					System.out.println("Priority Queue Extract Min not right!");
				}
			}
			Hashtable<String, LinkInfo> vertexNeighborWeights = vertex.getNeighborWeights();
			for(String neighborString: vertex.getNeighborIDs()){
				GraphNode neighborN = null;
				for(GraphNode n: graph){
					if(n.getID().equals(neighborString)){
						neighborN = n;
						break;
					}
				}
				//neighbor hasn't been removed from priority queue
				if(!explored.contains(neighborN)){
					int alt = vertex.getDistance() + vertexNeighborWeights.get(neighborN).getWeight(); 
					if(alt < neighborN.getDistance()){
						if(Protocol.DEBUG){
							System.out.println("needs to be relaxed");
						}
						neighborN.setDistance(alt);
						neighborN.setPrevious(vertex);
						//decrease priority by removing from PQ and reinserting
						Iterator<GraphNode> iter = PQ.iterator();
						while(iter.hasNext()){
							if((iter.next()).equals(neighborN)){
								PQ.remove(iter);
								PQ.add(neighborN);
								if(Protocol.DEBUG){
									System.out.println("reinserted into PQ");
								}
							}
						}
					}
				}
			}
		}
		//getting shortest path
		ArrayList<String> shortestPath = new ArrayList<String>();
		GraphNode currentNode = target;
		while(currentNode.getPrevious()!=null){
			shortestPath.add(0, currentNode.getID());
			currentNode = currentNode.getPrevious();
		}
		return shortestPath;
	}
}




