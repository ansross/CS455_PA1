package cs455.overlay.wireformats;

import cs455.overlay.node.MessagingNode;

public class Link {
	private MessagingNode A;
	private MessagingNode B;
	
	public Link(MessagingNode first, MessagingNode second){
		A = first;
		B =second;
	}
	
	public MessagingNode getFirstNode(){
		return A;
	}
	
	public MessagingNode getSecondNode(){
		return B;
	}
	
}
