package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import util.ResultSetter;
import cs455.overlay.node.Node;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

public class TCPReceiverThread extends Thread{
	//to have a way to get the event from the receiver thread
	//private ResultSetter setter;
	private Socket socket;
	private DataInputStream din;
	private Node myNode;
	
	public void setResultSetter(ResultSetter setter){
		//this.setter = setter;
	}
	
	public TCPReceiverThread(Node node, Socket socket) throws IOException{
		System.out.println("Reciever thread made");
		this.socket = socket;
		din = new DataInputStream(socket.getInputStream());
		this.myNode = node;
	}
	
	public void run(){
		System.out.println("Thread running");
		int dataLength;
		boolean EOFExcept = false;
		while(socket != null){
			//dataLength = din.readInt();
			try{
				System.out.println("listening");
				dataLength = din.readInt();
				System.out.println("read DataLength");
				
				byte[] data = new byte[dataLength];
				System.out.println("1");
				din.readFully(data, 0, dataLength);
				System.out.println("2");
				//if(dataLength>1){
					System.out.println("2.5");
					Event event = EventFactory.getEvent(data);
					System.out.println("3");
					myNode.onEvent(event, socket);
					//setter.addResult(event);
				//}
				
			}catch(EOFException eof){
				System.out.println("EOF Exception");
				System.out.println(eof.getMessage());
				eof.printStackTrace();
				break;
			}
			catch (SocketException se){
				System.out.println("Socket Exception");
				System.out.println(se.getMessage());
				break;
			} catch (IOException ioe){
				System.out.println("IOException.");
				System.out.println("Message: "+ ioe.getMessage());
				System.out.println(ioe.getCause());
				ioe.printStackTrace();
				
				break;
			}
		}
		System.out.println("here");
		System.out.println("Socket!=null: ");
		System.out.println(socket!=null);
	}

}
