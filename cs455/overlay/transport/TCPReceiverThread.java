package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

public class TCPReceiverThread extends Thread{
	private Socket socket;
	private DataInputStream din;
	private Node threadsNode;
	
	public TCPReceiverThread(Node node, Socket socket) throws IOException{
		this.socket = socket;
		din = new DataInputStream(socket.getInputStream());
		this.threadsNode = node;
	}
	
	public void run(){
		int dataLength;
		while(socket != null){
			try{
				dataLength = din.readInt();
				
				byte[] data = new byte[dataLength];
				din.readFully(data, 0, dataLength);
				Event event = EventFactory.getEvent(data); 
				
			} catch (SocketException se){
				System.out.println(se.getMessage());
				break;
			} catch (IOException ioe){
				System.out.println(ioe.getMessage());
				break;
			}
		}
	}

}
