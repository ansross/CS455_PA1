package cs455.overlay.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import cs455.overlay.wireformats.Protocol;

public class RoundThread extends Thread {
	private MessagingNode sourceNode;
	//private String targetName;
	//ArrayList<String> othernodes;

	public RoundThread(MessagingNode sourceArg){//, String targetArg){
		this.sourceNode = sourceArg;
		//this.targetName = targetArg;
	}

	public void run(){
		ArrayList<String> otherNodes = sourceNode.getOtherNodes();
		Random rand = new Random();
		for(int round = 0; round < Protocol.NUM_ROUNDS; ++round){
			String roundTarget = otherNodes.get(rand.nextInt(otherNodes.size()));
		
			if(round%100==0){
				System.out.println(" On round: "+round);
			}
			for(int msg = 0; msg < 5; ++msg){
				//System.out.println(	Thread.currentThread().getName()+ "on msg: "+msg);
				try {
					sourceNode.sendMessage(roundTarget);
					//System.out.println("sent message");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//sleep to allow time to receive 
			try {
				sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
		try {
			sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sourceNode.sendCompletionNotification();
			System.out.println("Sent completion");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
