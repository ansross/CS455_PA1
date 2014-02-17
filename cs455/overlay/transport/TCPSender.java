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

	public synchronized void sendData(byte[] dataToSend) throws IOException{
		try{
			//System.out.println("send point 1");
			int dataLength = dataToSend.length;
			//System.out.println("send point 1.1");
			dout.writeInt(dataLength);
			//System.out.println("send point 1.2");
			//System.out.println("data Length "+dataLength);
			dout.write(dataToSend);//, 0, dataLength);
			//System.out.println("send point 1.3");
			dout.flush();
		}catch(IOException ioe){
			System.out.println("Sender IOE");
			ioe.printStackTrace();
		}
	}

}
