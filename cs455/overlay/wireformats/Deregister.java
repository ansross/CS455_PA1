package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
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
	@Override
	public int getType() {
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
