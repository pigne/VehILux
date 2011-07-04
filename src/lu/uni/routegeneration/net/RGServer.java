/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file ServerTest.java
 * @date Jun 10, 2011
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import lu.uni.routegeneration.generation.RouteGeneration;

/**
 * 
 * 
 * <h2>Protocol</h2>
 * 
 * <h3>global behavior</h3>
 * <ul>
 * <li>Once launched, the server waits for incoming connections through port
 * number <code>port</code>.</li>
 * 
 * <li>Once connected to a client the server waits for commands. If an unknown
 * command is received, the connection with the client is dropped and the
 * servers starts waiting for a new client to connect.</li>
 * <li>Once a valid command is received the server waits the parameters
 * specifics to this command before responding. The response type depends on the
 * command.</li>
 * 
 * <li>Once the server is done answering a received command with its parameters,
 * it waits for a new command from the client. The server will answer the
 * clients command until a <code>CMD_END</code> is received or until an
 * erroneous message is received.</li>
 * </ul>
 * 
 * <h3>Data types</h3>
 * 
 * <p>
 * Before sending a value (integer, double, string, table...) one have to
 * specify its type (and if applicable, its length) to the server. Value types
 * are defined to allow the server to recognize the type of a value. When
 * applicable (strings, tables, raw data) types are followed by a length. This
 * length is always coded with a 16-bits signed short and usually represents the
 * number of elements (for arrays).
 * </p>
 * 
 * <ul>
 * <li><code>TYPE_INT</code>: Announces an integer. Followed by an 32-bit signed
 * integer.</li>
 * 
 * <li><code>TYPE_DOUBLE</code>: Announces a double. Followed by an 64-bit
 * double precision floating point number.</li>
 * 
 * <li><code>TYPE_INT_ARRAY</code>: Announces an array of integers. Followed by
 * first, a 16-bit short that indicates the length <b>in number of elements</b>
 * of this array, and then, the actual sequence of integers.</li>
 * 
 * <li><code>TYPE_DOUBLE_ARRAY</code>: Announces an array of doubles. Followed
 * by first, a 16-bit short that indicates the length <b>in number of
 * elements</b> of this array, and then, the actual sequence of doubles.</li>
 * 
 * <li><code>TYPE_STRING</code>: Announces an array of characters. Followed by
 * first, a 16-bits short for the size <b>in bytes</b> (not in number of
 * characters) of the string, then by the <b>unicode</b> string itself.</li>
 * 
 * <li><code>TYPE_RAW</code>: Announces raw data, good for serialization.
 * Followed by first, a 16-bits integer indicating the length in bytes of the
 * dataset, and then the data itself.</li>
 * 
 * <li><code>TYPE_COMPOUND</code>: Announces a compound data set, where arrays
 * contain other arrays mixed with native types. Each data piece in this case,
 * has to announce it's type (and length if applicable). May be useless because
 * hard to decode...</li>
 * 
 * <li><code>TYPE_ARRAY</code>: Announces an undefined-type array. Followed by
 * first, a 16-bits integer indicating the number of elements, and then, the
 * elements themselves. The elements themselves have to give their types.
 * <b>Should only be used in conjunction with <code>TYPE_COMPOUND</code></b>.</li>
 * </ul>
 * 
 * 
 * <h3>Command-specific protocols</h3>
 * 
 * <p>
 * Each command is awaited by the server to be followed by defined parameters.
 * </p>
 * 
 * <ul>
 * <li><code>CMD_FITNESS</code>: Call for the fitness function to be computed.
 * Is awaited to be followed by a value. The type of the value is free and can
 * be one of the existing ones.</li>
 * </ul>
 * 
 */

public class RGServer {

	public static int CMD_GETVERSION = 0x00;
	public static int CMD_START = 0x01;
	public static int CMD_END = 0x02;

	public static int CMD_FITNESS = 0x10;
	public static int CMD_PARAMS_NAMES = 0x11;
	public static int CMD_PARAMS_BOUNDARIES = 0x10;

	// GS commands idea...
	public static int CMD_STEP = 0x20;
	public static int CMD_ADD_GRAPH = 0x21;
	public static int CMD_CHANGE_GRAPH = 0x22;
	public static int CMD_DELETE_GRAPH = 0x23;
	public static int CMD_ADD_NODE = 0x24;
	public static int CMD_CHANGE_NODE = 0x25;
	public static int CMD_DELETE_NODE = 0x26;
	public static int CMD_ADD_EDGE = 0x27;
	public static int CMD_CHANGE_EDGE = 0x28;
	public static int CMD_DELETE_EDGE = 0x29;

	// Values types

	// Followed by an 32-bit signed integer
	public static int TYPE_INT = 0x50;
	// An array of integers. Followed by first, a 16-bits integer for the number
	// of integerss and then, a list of 32-bit signed integers
	public static int TYPE_INT_ARRAY = 0x51;
	// Followed by a double precision 64-bits floating point number
	public static int TYPE_DOUBLE = 0x52;
	// Array of double. Followed by first, a 16-bits integer for the number of
	// doubles and then, a list of 64-bit doubles
	public static int TYPE_DOUBLE_ARRAY = 0x53;
	// Followed by an 64-bit signed integer
	public static int TYPE_LONG = 0x54;
	// An array of longs. Followed by first, a 16-bits integer for the number of
	// longs and then, a list of 62-bit signed integers
	public static int TYPE_LONG_ARRAY = 0x55;
	// Array of characters. Followed by first, a 16-bits integer for the size in
	// bytes (not in number of characters) of the string, then by the unicode
	// string
	public static int TYPE_STRING = 0x56;
	// Raw data, good for serialization. Followed by first, a 16-bits integer
	// indicating the length in bytes of the dataset, and then the data itself.
	public static int TYPE_RAW = 0x57;
	// Compound data where arrays contain other arrays mixed with native types.
	// Each data piece in this case, has to announce it's type (and length if
	// applicable). May be useless because hard to decode...
	public static int TYPE_COMPOUND = 0x58;
	// Used with TYPE_COMPOUND. An undefined type array. Followed by first, a
	// 16-bits integer indicating the number of elements, and then, the elements
	// themselves. The elements themselves have to give their types.
	public static int TYPE_ARRAY = 0x58;
	
	
	
	public static int port = 4444;

	public RGServer(RouteGeneration rg, int port) throws IOException, InterruptedException {

		System.out.println("RGServer: started.");

		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("RGServer: Could not listen on port: " + port);
			System.exit(-1);
		}

		Socket clientSocket = null;
		while (true) {
			try {
				System.out.println("Server: Listenning " + port);
				clientSocket = serverSocket.accept();

			} catch (IOException e) {
				System.out.println("Server: Accept failed: " + port);
				System.exit(-1);
			}

			System.out.println("Server: Connected");

			serveClient(clientSocket);

		}

	}

	/**
	 * @param clientSocket
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private void serveClient(Socket clientSocket) throws IOException, InterruptedException {
		OutputStream out = clientSocket.getOutputStream();
		BufferedInputStream in = new BufferedInputStream(clientSocket
				.getInputStream());
		String inputLine;
		int cmd=0;
		int data_type;
		try{
		while ((cmd = in.read() ) != -1) {
			if (CMD_FITNESS == cmd) {
				System.out.println("Server: Received comamd: "+Constants.CMD_FITNESS);


				// has to be a TYPE_INT_ARRAY
				data_type = (byte) in.read();
				if( ! (TYPE_INT_ARRAY == data_type)){
					System.out.println("Server:  We need "+Constants.TYPE_INT_ARRAY+ " here. Got "+data_type);
					System.out.printf("%bd %n",data_type);
				}
				
				int params[]=null;
				if( (params= readIntArray(in)) == null){
					System.out.println("Server:  problem 2");
					return;
				}
				
			

				int res = fitness(params);

				System.out.println("Server:  sending answer");
				byte[] data = ByteBuffer.allocate(4).putInt(res).array();
				out.write(data, 0, 4);
				out.flush();

				// reinit stuff

			} else if(cmd == CMD_PARAMS_NAMES){
				
			} else if(cmd == CMD_PARAMS_BOUNDARIES){
				
			} else if (cmd == -1) {
				System.out.println("Server:  Client disappeared");
				return;
			} else if (Constants.CMD_END.equals(cmd)) {
				System.out.println("Server:  Client ended the conenction");
				return;
			} else {
				System.out.println("Server: Weired cmd from client:" + cmd + " instead of "+Constants.CMD_FITNESS.code());
				return;
			}
		}
		}catch(SocketException s){
			System.out.println("Server: Client violently exited.");
			return;}
		System.out.println(cmd);

	}


	public String readString(InputStream in) throws IOException{
		byte[] data = new byte[2];
		
		
		if (in.read(data, 0, 2) != 2) {
			return null;
		}
		int len = 0;
		len |= data[0] & 0xFF;
		len <<= 8;
		len |= data[1] & 0xFF;

		data = new byte[len];
		if (in.read(data, 0, len) != len) {
			return null;
		}
		return new String(data, Charset.forName("UTF-8"));
	}
	
	/**
	 * @param in
	 * @return
	 * @throws IOException 
	 */
	private int[] readIntArray(InputStream in) throws IOException {
		byte[] data = new byte[2];
		
		if (in.read(data, 0, 2) != 2) {
			System.out.println("readIntArray : could not read length of array (short)");
			return null;
		}
		
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.put(data);
		bb.flip();
		short len = bb.getShort();
		

		data = new byte[len*4];
		if (in.read(data, 0, len*4) != len*4) {
			System.out.println("readIntArray : could not read array");
			return null;
		}

		bb = ByteBuffer.allocate(4*len);
		bb.put(data);
		bb.flip();
		int[] res = new int[len];
		for(int i=0;i<len;i++){
			
		res[i]= bb.getInt();
		System.out.print(res[i]+",");
		}
		System.out.println();
	    return res;		 
		 		 
	}

	
	
	
	private double[] readDoubleArray(InputStream in) throws IOException {
		byte[] data = new byte[2];
		
		if (in.read(data, 0, 2) != 2) {
			System.out.println("readDoubleArray : could not read length of array (short)");
			return null;
		}
		
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.put(data);
		bb.flip();
		short len = bb.getShort();
		

		data = new byte[len*8];
		if (in.read(data, 0, len*8) != len*8) {
			System.out.println("readDoubleArray : could not read array");
			return null;
		}

		bb = ByteBuffer.allocate(8*len);
		bb.put(data);
		bb.flip();
		double[] res = new double[len];
		for(int i=0;i<len;i++){
			
		res[i]= bb.getDouble();
		System.out.print(res[i]+",");
		}
		System.out.println();
	    return res;		 
		 		 
	}

	
	/**
	 * @param clientSocket
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private int fitness(int[] params) throws IOException, InterruptedException {

		// do compute fitness

		Thread.sleep(2000);
		// return an int result

		return (int) (Math.random() * 1000000);
	}



}
