package cs455.overlay.wireformats;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Protocol {
	public static final boolean DEBUG = false;
	public static final int DEREGISTER = 2000;
	public static final int LINK_WEIGHTS = 2001;
	public static final int MESSAGE = 2002;
	public static final int MESSAGING_NODES_LIST = 2003;
	public static final int REGISTER_REQUEST = 2004;
	public static final int REGISTER_RESPONSE = 2005;
	public static final int TASK_COMPLETE = 2006;
	public static final int TASK_INITIATE = 2007;
	public static final int TASK_SUMMARY_REQUEST = 2008;
	public static final int TASK_SUMMARY_RESPONSE = 2009;
	
	public static final int NUM_ROUNDS = 5000;
	
	//header 

}