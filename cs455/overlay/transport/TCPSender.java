package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPSender {
	private Socket socket;
	private DataOutputStream dout;
	
	public TCPSender(Socket socketArg) throws IOException{
		this.socket = socketArg;
		dout = new DataOutputStream(socket.getOutputStream());
	}
	
	public void sendData(byte[] dataToSend) throws IOException{
	try{	int dataLength = dataToSend.length;
		dout.writeInt(dataLength);
		dout.write(dataToSend, 0, dataLength);
		dout.flush();}catch(IOException ioe){
			System.out.println("Sender IOE");
			ioe.printStackTrace();
		}
	}

}
