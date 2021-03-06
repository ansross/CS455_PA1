package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Deregister implements Event {
	private int type=Protocol.DEREGISTER;
	private String nodeIPAddress;
	private int nodeServerPortNum;
	
	@Override
	public int getType() {
		return type;
		// TODO Auto-generated method stub

	}
	
	public String getIPAddress(){
		return nodeIPAddress;
	}
	
	public int getServerPortNum(){
		return nodeServerPortNum;
	}
	
	public Deregister(String nodeIPAddressArg, int nodeServerPortNumArg){
		nodeIPAddress = nodeIPAddressArg;
		nodeServerPortNum = nodeServerPortNumArg;
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
		
		nodeServerPortNum = din.readInt();
		
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
		
		byte[] nodeIPBytes = nodeIPAddress.getBytes();
		int elementLength = nodeIPBytes.length;
		dout.writeInt(elementLength);
		dout.write(nodeIPBytes);
		
		dout.writeInt(nodeServerPortNum);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
	}

}
