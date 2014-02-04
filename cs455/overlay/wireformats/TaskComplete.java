package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskComplete implements Event {
	private int type = Protocol.TASK_COMPLETE;
	private String nodeIPAddress;
	private int nodePortNum;
	
	@Override
	public int getType() {
		return type;
		// TODO Auto-generated method stub

	}
	
	public TaskComplete(String IPAddresArg, int portNumArg){
		this.nodeIPAddress=IPAddresArg;
		this.nodePortNum = portNumArg;
	}
	
	public TaskComplete(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInStr = 
				new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = 
				new DataInputStream(new BufferedInputStream(baInStr));
		
		//type
		int msgType = din.readInt();
		if(msgType != type){
			System.out.println("ERROR: types do not match. Actual type: "+type+", passed type: "+msgType);
		}
		
		int IPLength = din.readInt();
		byte [] IPBytes = new byte[IPLength];
		din.readFully(IPBytes);
		this.nodeIPAddress = new String(IPBytes);
	
		this.nodePortNum = din.readInt();
		
		baInStr.close();
		din.close();
	}

	@Override
	public byte[] getByte() throws IOException {
		// TODO Auto-generated method stub
		byte[] marshalledBytes=null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(type);
		
		byte[] nodeIPAddBytes = nodeIPAddress.getBytes();
		int elementLength = nodeIPAddBytes.length;
		dout.writeInt(elementLength);
		dout.write(nodeIPAddBytes);
		
		dout.writeLong(nodePortNum);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
		
	}

}
