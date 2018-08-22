package org.das.network.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SocketTransportConnectorImplTest {

	//private TransportListener socketListener = new SocketTransportConnectorImpl();
	
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
		String message = "{\"message\":\"100\"}";
		int i=0;
		long start = System.currentTimeMillis();
		while(i++ < 10)
			connect(message + i);
		
		long end = System.currentTimeMillis();
		System.out.println("Total millis " + (end-start));
	}
	
	private boolean connect(final String data) {
		boolean dataSent= false;
		SocketAddress address = new InetSocketAddress("127.0.0.1", 1445);
		ByteBuffer readBuff = ByteBuffer.allocate(64);
		SocketChannel channel=null;
		try {
			System.out.println("Sent message " + data);
			channel = SocketChannel.open(address);
			channel.configureBlocking(true);
			channel.write(ByteBuffer.wrap(data.getBytes()));
			channel.read(readBuff);
			System.out.println("Received message " + new String(readBuff.array()));
			dataSent = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				channel.close();
			}catch(IOException e){}
		}
		return dataSent;
	}
	
	public static void main(String[] arg) {
		new SocketTransportConnectorImplTest().testSimpleMessage();
	}
	
}
