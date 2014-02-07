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
	private int serverPortNum;
	private String AssignedID;
	
	public RegisterRequest(String IPAddArg, int portNumArg, String idArg){
		IPAddress = IPAddArg;
		serverPortNum = portNumArg;
		System.out.println("Port Num " + serverPortNum);
		AssignedID = idArg;
	}
	
	public void print(){
		System.out.println("IPAdd: "+IPAddress + ", PortNum: "+serverPortNum +", id: "+AssignedID);
	}
	
	@Override
	public int getType() {
		return type;

	}
	
	public String getIPAddress(){
		return IPAddress;
	}
	
	public int getServerPortNum(){
		return serverPortNum;
	}
	
	public RegisterRequest(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInStr = 
				new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = 
				new DataInputStream(new BufferedInputStream(baInStr));
		
		//type
		int msgType = din.readInt();
		if(msgType != type){
			System.out.println("ERROR: types do not match. Actual type: "+type+", passed type: "+msgType);
		}
		
		//IPAddress
		int IPAddressLength = din.readInt();
		byte [] IPAddBytes = new byte[IPAddressLength];
		din.readFully(IPAddBytes);
		this.IPAddress = new String(IPAddBytes);
	
		//port number
		serverPortNum = din.readInt();
		
		//ID
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
		//type
		dout.writeInt(type);
		
		//IPAddress
		byte[] IPBytes = IPAddress.getBytes();
		int elementLength = IPBytes.length;
		dout.writeInt(elementLength);
		dout.write(IPBytes);
		
		//Port Number
		dout.writeInt(serverPortNum);
		
		//ID
		byte[] IDBytes = AssignedID.getBytes();
		elementLength = IDBytes.length;
		System.out.println("IdBytes length "+elementLength);
		dout.writeInt(elementLength);
		dout.write(IDBytes);
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
		// TODO Auto-generated method stub

	}

}
