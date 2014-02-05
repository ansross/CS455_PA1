package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cs455.overlay.node.MessagingNode;

public class MessagingNodesList implements Event {
	private int type=Protocol.MESSAGING_NODES_LIST;
	private long numPeerNodes;
	private ArrayList<String> nodeNames;  
	//TODO messaging node list!!

	public long getNumPeerNodes(){
		return numPeerNodes;
	}
	public MessagingNodesList(long numPeersArg, ArrayList<String> nodeNamesArg){
		this.numPeerNodes = numPeersArg;
		nodeNames = new ArrayList<String>(nodeNamesArg);
		System.out.println("Type ="+type);
	}
	
	public MessagingNodesList(byte[] marshalledBytes) throws IOException{
		ByteArrayInputStream baInStr = 
				new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = 
				new DataInputStream(new BufferedInputStream(baInStr));
		
		//type
		int msgType = din.readInt();
		if(msgType != type){
			System.out.println("ERROR: types do not match. Actual type: "+type+", passed type: "+msgType);
		}
		
		this.numPeerNodes = din.readLong();
		
		this.nodeNames = new ArrayList<String>();
		for(int i=0; i<numPeerNodes; ++i){
			int nodeNameLength = din.readInt();
			byte [] nodeNameBytes = new byte[nodeNameLength];
			din.readFully(nodeNameBytes);
			this.nodeNames.add(new String(nodeNameBytes));
		}
		baInStr.close();
		din.close();
	}
	
	@Override
	public int getType() {
		return type;	// TODO Auto-generated method stub

	}
	


	@Override
	public byte[] getByte() throws IOException {
		byte[] marshalledBytes=null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

		dout.writeInt(type);
		
		dout.writeLong(numPeerNodes);
		
		for(int node=0; node<numPeerNodes; ++node){
			//for each node in the peer nodes list, print hostname:port
			byte[] nodeInfoBytes = nodeNames.get(node).getBytes();
			int elementLength = nodeInfoBytes.length;
			dout.writeInt(elementLength);
			dout.write(nodeInfoBytes);
		}
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
		// TODO Auto-generated method stub

	}

}
