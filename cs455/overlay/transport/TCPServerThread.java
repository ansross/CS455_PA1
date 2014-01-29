package cs455.overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Node;

public class TCPServerThread extends Thread{
	
	private Socket socket = null;
	private Node myNode;
	
	public TCPServerThread(Node node){
		myNode=node;
	}
	
	
	public TCPServerThread(Socket socket){
		//???????? http://docs.oracle.com/javase/tutorial/networking/sockets/examples/KKMultiServerThread.java
		this.socket=socket;
	}
	
	public void run(){
		try( 
				ServerSocket serverSocket = new ServerSocket(0);)
				{
				while(true){
					new TCPReceiverThread(serverSocket.accept()).start();
				}
		} catch (IOException e){
			System.out.println("Exception caught when trying to listen on port or"
					+ "listening for a connection");
			System.out.println(e.getMessage());
		}
	}
	
}
