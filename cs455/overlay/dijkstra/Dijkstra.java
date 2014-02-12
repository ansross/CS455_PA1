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
		if(Protocol.DEBUG){
			//System.out.println("size of graph: "+graph.size());
			//System.out.println("source: "+sourceString);
			//System.out.println("target: "+targetString);
		}
		//TODO BE BETTER!!
		/*String [] tokens = sourceString.split(":");
		
		sourceString = tokens[0]+".cs.colostate.edu"+":"+tokens[1];*/
		for(GraphNode node: graph){
			//System.out.println("nodeID: "+node.getID());
			if(node.getID().equals(sourceString)){
				source = node;
			}
			if(node.getID().equals(targetString)){
				target = node;
			}
		}
		if(source == null || target == null){
			System.out.println("ERROR: could not find target and/or source in graph");
			System.out.println("source "+sourceString+" is null: " + (source==null));
			System.out.println("target "+targetString+ " is null: "+(target==null));
			return null;
		}
		
		Comparator<GraphNode> comp = new GraphNodeComparator();
		//PriorityQueue<GraphNode> PQ = new PriorityQueue<GraphNode>(graph.size(), comp);
		ArrayList<GraphNode> PQ = new ArrayList<GraphNode>(graph.size());
		ArrayList<GraphNode> explored = new ArrayList<GraphNode>(graph.size());
		graph.get(graph.indexOf(source)).setDistance(0);
		PQ.add(source);
		for(GraphNode vertex:graph ){
			if(!vertex.getID().equals(source.getID())){
				//as close to infinity as I'm getting
				
				vertex.setDistance(Integer.MAX_VALUE);
				vertex.setPrevious(null);
			}
			PQ.add(vertex);
			//System.out.println("adding node: "+vertex.getID());
		}
		
		while(!PQ.isEmpty()){
			//extract min
			GraphNode vertex = getMin(PQ);
			explored.add(vertex);
			//at target, done
			/*if(vertex.equals(target)){
				break;
			}*/
			//keep track of those we've relaxed
			if(Protocol.DEBUG){
				//System.out.println("pulled vertex distance: " + vertex.getDistance());
				if(!PQ.isEmpty()){
					//System.out.println("next vertex's distance: " + PQ.peek().getDistance());
				
					if(vertex.getDistance() > peek(PQ).getDistance()){
					//	System.out.println("Priority Queue Extract Min not right!");
					}
					if(vertex.getDistance() <= peek(PQ).getDistance()){
					//	System.out.println("Priority Queue Okay");
					}
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
				//if(!explored.contains(neighborN)){
					int alt = vertex.getDistance() + vertexNeighborWeights.get(neighborN.getID()).getWeight(); 
					if(alt < neighborN.getDistance()){
						if(Protocol.DEBUG){
							//System.out.println("needs to be relaxed");
						}
						neighborN.setDistance(alt);
						neighborN.setPrevious(vertex);
						updateMin(PQ, neighborN, alt);
						//decrease priority by removing from PQ and reinserting
						//PriorityQueue<GraphNode> PQTemp = new PriorityQueue<GraphNode>(PQ);
						/*Iterator<GraphNode> iter = PQTemp.iterator();
						while(iter.hasNext()){
							//GraphNode nxt = iter.next();
							//PQ.add(neighborN);
							if((iter.next()).equals(neighborN)){
								PQ.remove(iter);
								PQ.add(neighborN);
								break;
								//if(Protocol.DEBUG){
									//System.out.println("reinserted into PQ");
							//	}
							//}
							}
						}*/
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
		shortestPath.add(0, source.getID());
		return shortestPath;
	}
	
	private static GraphNode getMin(ArrayList<GraphNode> pq){
		int min = Integer.MAX_VALUE;
		GraphNode minNode = null;
		for(GraphNode gn: pq){
			if(gn.getDistance() < min){
				min = gn.getDistance();
				minNode=gn;
			}
		}
		pq.remove(minNode);
		return minNode;
	}
	
	private static GraphNode peek(ArrayList<GraphNode> pq){
		int min = Integer.MAX_VALUE;
		GraphNode minNode = null;
		for(GraphNode gn: pq){
			if(gn.getDistance() < min){
				min = gn.getDistance();
				minNode=gn;
			}
		}
		return minNode;
	}
	
	private static void updateMin(ArrayList<GraphNode> pq, GraphNode toChange, int newVal){
		for(GraphNode gn: pq){
			if(gn.getID().equals(toChange.getID())){
				gn.setDistance(newVal);
				break;
			}
		}
	}
	
}




