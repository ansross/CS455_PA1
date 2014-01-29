package cs455.overlay.wireformats	;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
		AssignedID = idArg;
	}
	
	public void print(){
		System.out.println("IPAdd: "+IPAddress + ", PortNum: "+PortNum +", id: "+AssignedID);
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
	
	public RegisterRequest(byte[] marshalledBytes) throws IOException{
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
		this.IPAddress = new String(IPAddBytes);
		
		PortNum = din.readInt();
		
		int IDLength = din.readInt();
		byte [] IDBytes = new byte[IDLength];
		din.readFully(IDBytes);
		this.AssignedID = new String(IDBytes);
		
		baInStr.close();
		din.close();
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
