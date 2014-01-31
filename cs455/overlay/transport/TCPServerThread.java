package cs455.overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import util.ResultSetter;
import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;

public class TCPServerThread extends Thread{
	private Node myNode;
	
	public TCPServerThread(Node node){
		myNode=node;
	}
	
	public TCPServerThread(){
	//	receivedEvents = new ArrayList<Event>();
	}
	

	public void run(){
		/*ResultSetter setterForServer = new ResultSetter() {
			public void addResult(Event result){
				receivedEvents.add(result);
			}
		};*/
		try( 
				ServerSocket serverSocket = new ServerSocket(15003);)
				{
				while(true){
					TCPReceiverThread rec = new TCPReceiverThread(myNode, serverSocket.accept());
					//rec.setResultSetter(setterForServer);
					rec.start();
					//every time give another event to node
					/*if(!receivedEvents.isEmpty()){
						setterForNode.addResult(receivedEvents.remove(0));
					}*/
				}
		} catch (IOException e){
			System.out.println("Exception caught when trying to listen on port or"
					+ "listening for a connection");
			System.out.println(e.getMessage());
		}
	}
	
}
