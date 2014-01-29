package cs455.overlay.wireformats	;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterRequest implements Event {
	private int type = Protocol.REGISTER_REQUEST;
	private String IPAddress;
	private int PortNum;
	private String AssignedID;
	
	public RegisterRequest(String IPAddArg, int portNumArg, String idArg){
		IPAddress = IPAddArg;
		PortNum = portNumArg;
		idArg = AssignedID;
	}
	
	@Override
	public int getType() {
		return type;

	}
	
	public String getIPAddress(){
		return IPAddress;
	}
	
	public int getPortNum(){
		return PortNum;
	}

	@Override
	public byte[] getByte() throws IOException {
		byte[] marshalledBytes=null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		dout.writeInt(type);
		
		byte[] IPBytes = IPAddress.getBytes();
		int elementLength = IPBytes.length;
		dout.writeInt(elementLength);
		dout.write(IPBytes);
		
		dout.writeInt(PortNum);
		
		byte[] IDBytes = AssignedID.getBytes();
		elementLength = IDBytes.length;
		dout.write(elementLength);
		dout.write(IDBytes);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
		// TODO Auto-generated method stub

	}

}
