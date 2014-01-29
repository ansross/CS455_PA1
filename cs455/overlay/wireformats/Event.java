package cs455.overlay.wireformats;

import java.io.IOException;


//all message types implement Event
public interface Event {
	//SHOULDN'T BE VOID
	public int getType();
	public byte[] getByte() throws IOException;;
}
