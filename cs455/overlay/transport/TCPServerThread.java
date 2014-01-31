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
	int serverSocketPortNum;
	
	//for setting the registry port num
	public TCPServerThread(Node node, int portNumArg){
		myNode=node;
		serverSocketPortNum = portNumArg;
	}
	
	//for setting other port nums, they will be picked automatically
	public TCPServerThread(Node node){
		myNode=node;
		serverSocketPortNum = 0;
	}
	
	public TCPServerThread(){
	//	receivedEvents = new ArrayList<Event>();
	}
	

	public void run(){
		try( 
				ServerSocket serverSocket = new ServerSocket(this.serverSocketPortNum);)
				{
				while(true){
					new TCPReceiverThread(myNode, serverSocket.accept()).start();
				}
		} catch (IOException e){
			System.out.println("Exception caught when trying to listen on port or"
					+ "listening for a connection");
			System.out.println(e.getMessage());
		}
	}
	
}
