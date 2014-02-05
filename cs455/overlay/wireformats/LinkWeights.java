package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class LinkWeights implements Event {
	private int type;
	private long numOfLinks;
	private ArrayList<Link> links;
	
	public LinkWeights(long numLinksArg, ArrayList<Link> linkArg){
		type = Protocol.LINK_WEIGHTS;
		numOfLinks = numLinksArg;
		links = new ArrayList<Link>();
		
		//TODO!!!
//		for(Link l: linkArg){
//			links.add(l.clone());
//			
//		}
	}

	public LinkWeights(byte[] marshalledBytes) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getType() {
		return type;
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] getByte() throws IOException {
		byte[] marshalledBytes=null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

		dout.writeInt(type);
		
		dout.writeLong(numOfLinks);
		
		//for assining radom weights
		Random rand = new Random();
		
		for(int i=0; i<numOfLinks; ++i){
			//for each link write hostnameA:portNumA hostnameB:portNumB weight
			byte[] nodeAInfoBytes = links.get(i).
					getFirstNode().messageNodeInfo().getBytes();
			int elementLength = nodeAInfoBytes.length;
			dout.writeInt(elementLength);
			dout.write(nodeAInfoBytes);
			
			byte[] nodeBInfoBytes = links.get(i).getSecondNode().
					messageNodeInfo().getBytes();
			elementLength = nodeBInfoBytes.length;
			dout.writeInt(elementLength);
			dout.write(nodeBInfoBytes);
			
			//write randomly assigned weight between 1 and 10
			//rand.nextInt(10) produces from [0,10)
			dout.writeInt(rand.nextInt(10)+1);			
		}
		
		dout.flush();
		marshalledBytes = baOutputStream.toByteArray();
		
		baOutputStream.close();
		dout.close();
		return marshalledBytes;
		// TODO Auto-generated method stub

	}

}
