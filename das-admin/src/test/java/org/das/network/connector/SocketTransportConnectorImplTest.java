package org.das.network.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.das.constant.AppConstant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SocketTransportConnectorImplTest {

	private volatile AtomicInteger COUNTER = new AtomicInteger(1);
	
	private final SocketAddress address = new InetSocketAddress("127.0.0.1", 1445);
	
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
		long start1 = System.currentTimeMillis();
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
		Thread[] threads = new Thread[200];
		for(int i=0;i<threads.length;i++) {
			threads[i] = new Thread(getTask());
		}
		for(int i=0;i<threads.length;i++) {
			threads[i].start();
		}
		long end1 = System.currentTimeMillis();
		System.out.println("Thread1. Total millis " + (end1-start1));
	}
	
	private Runnable getTask() {
		return ()-> {
			String message = "{\"command\":\"analysis\"}-";
			int index = 1;
			boolean listen = true;
			try {
				Selector selector = Selector.open();
				SocketChannel channel = connect(selector);
				channel.register(selector, SelectionKey.OP_WRITE);
				while(listen) {
					try {
						int count = selector.select();
			            if(count > 0) {
			                for(Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
			                    SelectionKey key = i.next();
			                    i.remove();
			                    if(key.isReadable()) {
			                    	byte[] data = readMessage(channel);
			                    	if(data.length != 1) {
			                    		System.out.println("Received Message. " + new String(data, AppConstant.SOCKET_CHAR_SET));
			                    		if(index++ == 1000) listen = false;
			                    		else channel.register(selector, SelectionKey.OP_WRITE);
			                    	}
			                    }
			                    if(key.isWritable()) {
			                    	writeToChannel(message + COUNTER.getAndIncrement(), channel);
			                    	channel.register(selector, SelectionKey.OP_READ);
			                    	Thread.currentThread().sleep(200);
			                    }
			                }
			            }
					}catch(Exception e) {
						e.printStackTrace();
					}
					/* Working example
					writeToChannel(message + COUNTER.getAndIncrement(), channel);
					Thread.currentThread().sleep(20);
					channel.register(selector, SelectionKey.OP_READ);
					byte[] data = readMessage(channel);
					System.out.println("Received Message. " + new String(data, AppConstant.SOCKET_CHAR_SET));
					*/
				}
				channel.shutdownInput();
				channel.shutdownOutput();
				channel.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		};
	}
	
	private SocketChannel connect(Selector selector) throws IOException {
		SocketChannel channel = SocketChannel.open(address);
		channel.configureBlocking(false);
		return channel;
	}
	
	private byte[] readMessage(SocketChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(64);
		int state;
		byte[] data = {1};
		int position = 0;
		while( (state = channel.read(buffer)) > 0 ) {
			buffer.flip();
			byte[] d = Arrays.copyOf(buffer.array(), state);
			data = Arrays.copyOf(data, position + state);
			System.arraycopy(d, 0, data, position, state);
			position = position + state;
			buffer.clear();
		}
		return data;
	}
	
	private void writeToChannel(final String data, final SocketChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());		
		channel.write(buffer);
	}
	
	public static void main(String[] arg) {
		new SocketTransportConnectorImplTest().testSimpleMessage();
	}
	
}
