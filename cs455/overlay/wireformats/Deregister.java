package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Deregister implements Event {
	private int type;
	private String nodeIPAddress;
	private int nodePortNum;
	
	
	public Deregister(String nodeIPAddressArg, int nodePortNumArg){
		type = Protocol.DEREGISTER;
		nodeIPAddress = nodeIPAddressArg;
		nodePortNum = nodePortNumArg;
	}
	
	public Deregister(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInStr = 
				new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = 
				new DataInputStream(new BufferedInputStream(baInStr));
		int msgType = din.readInt();
		if(msgType != type){
			System.out.println("ERROR: types do not match. Actual type: "+type+", passed type: "+msgType);
		}
		int IPAddressLength = din.readInt();
		byte [] IPAddBytes = new byte[IPAddressLength];
		din.readFully(IPAddBytes);
		this.nodeIPAddress = new String(IPAddBytes);
		
		nodePortNum = din.readInt();
		
		baInStr.close();
		din.close();
		
	}
	@Override
	public int getType() {
		return type;
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] getByte() throws IOException {
		// TODO Auto-generated method stub
		byte[] marshalledBytes=null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		
		byte[] nodeIPBytes = nodeIPAddress.getBytes();
		int elementLength = nodeIPBytes.length;
		dout.writeInt(elementLength);
		dout.write(nodeIPBytes);
		
		dout.writeInt(nodePortNum);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

}
