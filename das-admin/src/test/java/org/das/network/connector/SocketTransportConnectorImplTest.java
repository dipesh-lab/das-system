package org.das.network.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SocketTransportConnectorImplTest {

	private final AtomicInteger COUNTER = new AtomicInteger(0);
	
	@Before
	public void setUp() {
		//socketListener.init("localhost", 1445);
		//socketListener.startConnector();
	}
	
	@After
	public void afterTest() {
		//socketListener.stopListener();
	}
	
	@Test
	public void testSimpleMessage() {
		/*Thread thread1 = new Thread(()-> {
			long start1 = System.currentTimeMillis();
			String message = "{\"command\":\"analysis\"}1-";
			int i=0;			
			while(i++ < 10000)
				connect(message + i);
			long end1 = System.currentTimeMillis();
			System.out.println("Thread1. Total millis " + (end1-start1));
		});
		thread1.start();*/
		
		Thread[] threads = new Thread[100];
		for(int i=0;i<threads.length;i++) {
			threads[i] = new Thread(getTask());
		}
		for(int i=0;i<threads.length;i++) {
			threads[i].start();
		}
	}
	
	private Runnable getTask() {
		return ()-> {
			long start1 = System.currentTimeMillis();
			String message = "{\"command\":\"analysis\"}1-";
			int i=0;			
			while(i++ < 150)
				connect(message + i);
			long end1 = System.currentTimeMillis();
			System.out.println("Thread1. Total millis " + (end1-start1));
		};
	}
	
	private void connect(final String data) {
		SocketAddress address = new InetSocketAddress("127.0.0.1", 1445);
		ByteBuffer readBuff = ByteBuffer.allocate(48);
		SocketChannel channel=null;
		try {
			channel = SocketChannel.open(address);
			channel.write(ByteBuffer.wrap(data.getBytes()));
			channel.read(readBuff);			
			COUNTER.incrementAndGet();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				channel.socket().close();
				channel.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		System.out.println("Received message " + new String(readBuff.array()));
	}
	
	public static void main(String[] arg) {
		new SocketTransportConnectorImplTest().testSimpleMessage();
	}
	
}
