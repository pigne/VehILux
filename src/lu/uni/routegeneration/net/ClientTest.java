/**
 *
 * Copyright (c) 2010 University of Luxembourg
 *
 * @file ClientTest.java
 * @date Jun 10, 2011
 *
 * @author Yoann Pigné
 *
 */
package lu.uni.routegeneration.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * 
 */
public class ClientTest {

	public ClientTest() throws UnknownHostException, IOException, InterruptedException{
		Socket s;
		System.out.println("Client: started.");
		
		s = new Socket("localhost", 4444);
		
		System.out.println("Client: connected.");
		
		BufferedOutputStream out =new BufferedOutputStream(s.getOutputStream());
		
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		
		System.out.println("Client: Wait a bit.");
		
		Thread.sleep(1000);
		
		System.out.println("Client: start sending.");
		
		
		while(true){
			System.out.println("Client: sending");

			int params[]={1,2,3,4,5,6};
			
			int len = params.length;
			
			
			ByteBuffer bb = ByteBuffer.allocate(1+1+2+len*4);
			
			bb.put((byte)RGServer.CMD_FITNESS);
			//out.write(bb);
			
			bb.put(((byte)RGServer.TYPE_INT_ARRAY ));
			//out.write(bb);
			//System.out.println("Client : sending table size ="+len);
			bb.putShort((short)len);
			//out.write(bb);
			
			//bb= ByteBuffer.allocate(4*len);
			for(int i =0; i<len; i++){
				bb.putInt(params[i]);
			}
			
			bb.flip();
			
			out.write(bb.array());
			
			
			out.flush();
			
			
			// response
			bb = ByteBuffer.allocate(4);

			byte[] data=new byte[4];
			
			in.read(data,0,4);
			bb.put(data);
			bb.flip();
			//bb.put(data);
			int l = bb.getInt();
		    
			System.out.println("Client: Server fitness="+l);
			//Thread.sleep(1000);
		}
	}
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		new ClientTest();
	}
}
