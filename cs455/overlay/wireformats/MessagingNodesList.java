package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cs455.overlay.node.MessagingNode;

public class MessagingNodesList implements Event {
	private int type;
	private long numPeerNodes;
	private ArrayList<MessagingNode> nodeInfos;  
	//TODO messaging node list!!

	@Override
	public int getType() {
		// TODO Auto-generated method stub

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
			byte[] nodeInfoBytes = nodeInfos.get(node).messageNodeInfo().getBytes();
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
