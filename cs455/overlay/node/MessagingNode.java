package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import util.Utilities;
import cs455.overlay.dijkstra.Dijkstra;
import cs455.overlay.dijkstra.GraphNode;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.transport.TCPServerThread;

public class MessagingNode implements Node {
	
	private Hashtable<String, Connection> establishedConnections;
	//msgNodes Send: register_requests, deregister requests, message, task_complete, task_summary_response
	//msgNodes recieve: link_weights, message, messaging_nodes_list, register_response, task_initiate, task_summary_request

	private ArrayList<Socket> mySockets;
	private Hashtable<String, String> serverNameToSocketName;
	//initialized when link weights received 
	private Hashtable<String, ArrayList<LinkInfo>> serverNametoLinkWeights;
	
	//store shortest path to each node in the overlay
	private Hashtable<String, ArrayList<String>> shortestPaths;
	
	private String IPAddress;
	private int portNum;
	private int numMessagesSent;
	private long sumMessagesSent;
	private int numMessagesRecieved;
	private long sumMessagesRecieved;
	private int numMessagesRelayed;
	private String hostName;
	private int serverSocketPortNum;
	private TCPServerThread server;
	
	public void setServerSocketPortNum(int portArg){
		this.serverSocketPortNum=portArg;
	}
	
	public String getHostServer(){
		return hostName+":"+serverSocketPortNum;
	}
	
	public ArrayList<String> getNeighbors(){
		ArrayList<String> neighbors = new ArrayList<String>();
		for(Socket socket: mySockets){
			neighbors.add(establishedConnections.get(Utilities.createKeyFromSocket(socket)).getNameFromServerSocket());
		}
		return neighbors;
	}
	//private TCPSender sender;
	
	public String getIPAddress(){
		return IPAddress;
	}
	
	public int getPortNum(){
		return portNum;
	}
	
	public MessagingNode(){
		shortestPaths = new Hashtable<String, ArrayList<String>>(); 
		serverNameToSocketName = new Hashtable<String, String>();
		mySockets = new ArrayList<Socket>();
		establishedConnections = new Hashtable<String, Connection>();
		numMessagesSent = 0;
		sumMessagesSent =0;
		numMessagesRecieved =0;
		sumMessagesRecieved = 0;
		numMessagesRelayed =0;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
			System.out.println("Host Name: " + hostName);
			new TCPServerThread(this).start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
	}
	
	public void setServerThread(TCPServerThread arg){
		this.server = arg;
	}
	
	public TCPServerThread getServerThread(){
		return server;
	}
	
	public static void main(String[] args) throws IOException{
		if(args.length != 2){
			System.err.println("Usage: "
					+ "java cs455.overlay.node.MessagingNode "
					+ "<registry-host> <registry-port>" );
			System.exit(1);
		}
		
		MessagingNode msgNode = new MessagingNode();
		//msgNode.setServerThread(new TCPServerThread(msgNode));
		//msgNode.getServerThread().start();
		
		String registryHostName = args[0];
		int registryPortNum = Integer.parseInt(args[1]);
		
		try (
				Socket socket = new Socket(registryHostName, registryPortNum);
		)
		{
			msgNode.attemptRegistration(socket);
			msgNode.getCommandlineInput();
			
		}catch(IOException e){
			System.out.println("IOException Message Node");
			System.out.println(e);
		}
		
		while(true){
			msgNode.getCommandlineInput();
		}

//msgNode.attemptRegistration(registryHostName, registryPortNum);
	}
	
	public void getCommandlineInput(){
		Scanner input = new Scanner(System.in);
		boolean exit = false;
		while(!exit){
			
			String command = input.next();
			System.out.println("Your Command: " + command +" does nothing!!");
			switch(command){
			case "print-shortest-path":
				printShortestPath();
				break;
			case "exit-overlay":
				exitOverlay();
				break;
				default:
					System.out.println("ERROR: Command \""+command+"\" not supported");
			}
		}
		input.close();
		
	}
	
	private void exitOverlay() {
		// TODO Auto-generated method stub
		
	}

	private void printShortestPath() {
		Enumeration<String> enumKey = shortestPaths.keys();
		while(enumKey.hasMoreElements()){
			String targetName = enumKey.nextElement();
			
			ArrayList<String> path = shortestPaths.get(targetName);
			if(Protocol.DEBUG){
				System.out.println("target name: "+targetName);
				System.out.println("path size: "+path.size());
			}
			if(path.size()>1){
				//System.out.print(this.getHostServer());
			}
			
			for(int i=0; i<path.size(); ++i){
				System.out.print(path.get(i));
				//if not last element
				if(i+1 != path.size()){
					int weight = -1;
					for(LinkInfo li: serverNametoLinkWeights.get(path.get(i))){
						if(li.getHostBPortB().equals(path.get(i))){
							weight = li.getWeight();
						}
						if(li.getHostAPortA().equals(path.get(i))){
							//System.out.println("ERROR PRINT SHORTEST EVERYTHING IS BACKWARD");
							weight = li.getWeight();
							break;
						}
					}
					System.out.print("--"+weight+"--");
				}
			}
			System.out.print('\n');
		}
		
	}
	
	private void calculateShortestPaths(){
		ArrayList<GraphNode> overlayGraph = makeGraph();
		Enumeration<String> enumKey = serverNametoLinkWeights.keys();
		while(enumKey.hasMoreElements()){
			String name = enumKey.nextElement();
			ArrayList<String> shortestPath = Dijkstra.getShortestPath(overlayGraph, this.getHostServer(), name);
			if(Protocol.DEBUG){
				System.out.println("shortest path size: "+shortestPath.size());
				System.out.println("name from graph: " + name);
			}
			shortestPaths.put(name, shortestPath);
				
			
		}
	}
	
	private ArrayList<GraphNode> makeGraph(){
		ArrayList<GraphNode> overlayGraph = new ArrayList<GraphNode>();
		//make a node for each node in overlay
		Enumeration<String> enumKey = serverNametoLinkWeights.keys();
		while(enumKey.hasMoreElements()){
			String name = enumKey.nextElement();
			ArrayList<String> neighbors = new ArrayList<String>();
			Hashtable<String, LinkInfo> neighborWeights = new Hashtable<String, LinkInfo>(); 
			getNeighborWeights(name, neighborWeights, neighbors);
			overlayGraph.add(new GraphNode(name,neighborWeights , neighbors));
		}
		return overlayGraph;
		
	}

	private void getNeighborWeights(String name, Hashtable<String, LinkInfo> neighborWeights,
			ArrayList<String> neighbors) {
		ArrayList<LinkInfo> nodeNeighborInfo = serverNametoLinkWeights.get(name);
		for(LinkInfo li: nodeNeighborInfo){
			if(li.getHostAPortA().equals(name)){
				System.out.println("get neighbor weights is the right direction");
			}
			neighborWeights.put(li.getHostBPortB(), li);
			neighbors.add(li.getHostBPortB());
		}
	}

	private void attemptRegistration(Socket socket){//
			//String registryHostName, int registryPortNum){
		try	
		{
			new Connection(this, socket);
			mySockets.add(socket);
			System.out.println("got Socket");
			System.out.println("port num "+socket.getLocalPort());
			RegisterRequest regReq = new RegisterRequest(InetAddress.getLocalHost().getHostName(), this.serverSocketPortNum, new String(this.hostName+":"+socket.getLocalPort()));
			establishedConnections.get(Utilities.createKeyFromSocket(socket)).getSender().sendData(regReq.getByte());
			//DELETE ME
			System.out.println("Request Sent");
			
		}catch(IOException e){
			System.out.println("IOExecption Message Node");
					System.out.println(e);
		}
	}
	
	

	@Override
	public void onEvent(Event event, Socket socket) {
		switch(event.getType()){
		case Protocol.LINK_WEIGHTS:
			System.out.println("Recieved link weights");
			saveLinkInfo((LinkWeights) event);
			break;
		case Protocol.MESSAGE:
			try {
				parseMessage((Message) event);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case Protocol.MESSAGING_NODES_LIST:
			System.out.println("got messaging nodes list");
			System.out.println("My name is: "+hostName+":"+serverSocketPortNum);
			System.out.println("I have "+((MessagingNodesList) event).getNumPeerNodes()+" peer nodes");
			((MessagingNodesList) event).printPeerNodes();
			setUpOverlay((MessagingNodesList)event);
			break;
		case Protocol.REGISTER_REQUEST:
			System.out.println("got response");
			registerPeerNode((RegisterRequest)event, socket);
			break;
		case Protocol.TASK_INITIATE:
			break;
		case Protocol.TASK_SUMMARY_REQUEST:
			break;
		case Protocol.TASK_SUMMARY_RESPONSE:
			break;
		}
		// TODO Auto-generated method stub

	}
	
	private void saveLinkInfo(LinkWeights event) {
		ArrayList<String> links = event.getLinks();
		serverNametoLinkWeights = new Hashtable<String, ArrayList<LinkInfo>>(links.size());
		for(String str: links){
			String delims = ":| ";
			String[] tokens = str.split(delims);
			for(String s: tokens){
				System.out.println("Token: "+s);
			}
			LinkInfo li = new LinkInfo(tokens[0], Integer.parseInt(tokens[1]), tokens[2], Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
			if(Protocol.DEBUG){
				System.out.println("added link: "+li.getHostBPortB());
			}
			if(serverNametoLinkWeights.containsKey(li.getHostAPortA())){
				serverNametoLinkWeights.get(li.getHostAPortA()).add(li);
			}
			else{
				serverNametoLinkWeights.put(li.getHostAPortA(), new ArrayList<LinkInfo>());
				serverNametoLinkWeights.get(li.getHostAPortA()).add(li);
			}
		}
		if(Protocol.DEBUG){
			System.out.println("Link weights: ");
			 Set<String> keySet = serverNametoLinkWeights.keySet();
			 // Obtain an Iterator for the entries Set
			 Iterator<String> it = keySet.iterator();
			 // Iterate through Hashtable entries
			 while(it.hasNext()){
				 String name = it.next();
				 System.out.println(name+": "+serverNametoLinkWeights.get(name));
			}
		}
		calculateShortestPaths();
		if(Protocol.DEBUG){
			System.out.println("shortest paths calculated");
		}
		// TODO Auto-generated method stub
		
	}

	private void parseMessage(Message msg) throws IOException{
		ArrayList<String> shortestPathIDs = msg.getShortestPathIDs();
		if(shortestPathIDs.get(shortestPathIDs.size()-1).equals(this.getHostServer())){
			keepMessage(msg);
		}
		else{
			relayMessage(msg);
		}
		
	}
	
	private void keepMessage(Message msg){
		numMessagesRecieved++;
		this.sumMessagesRecieved += msg.getMessage();
	}
	
	private void relayMessage(Message msg) throws IOException{
		numMessagesRelayed++;
		ArrayList<String> shortestPathIDs = msg.getShortestPathIDs();
		for(int i=0; i<shortestPathIDs.size(); ++i){
			if(shortestPathIDs.get(i).equals(this.getHostServer())){
				String destinationName = shortestPathIDs.get(i+1);
				String destinationLocalName = serverNameToSocketName.get(destinationName);
				establishedConnections.get(destinationLocalName).getSender().sendData(msg.getByte());
			}
		}
	}
	
	private void registerPeerNode(RegisterRequest event, Socket socket){
		serverNameToSocketName.put(event.getID(), Utilities.createKeyFromSocket(socket));
	}
	
	private void setUpOverlay(MessagingNodesList event) {
		ArrayList<String> peerNames = event.getNodeNames();
		for(int peer=0; peer<event.getNumPeerNodes(); peer++){
			//get port number and host name
			String delims = ":";
			String[] tokens = peerNames.get(peer).split(delims);
			String peerHostName = tokens[0];
			int peerPortNum = Integer.parseInt(tokens[1]);
			if(Protocol.DEBUG){
				System.out.println("PeerHost:PeerPort= "+peerHostName+":"+peerPortNum);
			}
			try{
				 Socket socket = new Socket(peerHostName, peerPortNum);
				//Connection newCon = 
				new Connection(this, socket);
				mySockets.add(socket);
				serverNameToSocketName.put(peerHostName+":"+peerPortNum, Utilities.createKeyFromSocket(socket));
				RegisterRequest regReq = new RegisterRequest(InetAddress.getLocalHost().getHostName(), socket.getLocalPort(), new String(this.hostName+":"+socket.getLocalPort()));
				establishedConnections.get(Utilities.createKeyFromSocket(socket)).getSender().sendData(regReq.getByte());
				System.out.println("Connected");
			}catch(IOException ioe){
				ioe.printStackTrace();
			}
			 
			//connect
			
			
		}
		// TODO Auto-generated method stub
		
	}
	
	public String messageNodeInfo(){
		return hostName + ":"+portNum;
	}

	@Override
	public void registerConnection(Connection connection) {
		establishedConnections.put(connection.getName(), connection);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deregisterConnection(Connection connection) {
		// TODO Auto-generated method stub
		
	}
	
	

}
