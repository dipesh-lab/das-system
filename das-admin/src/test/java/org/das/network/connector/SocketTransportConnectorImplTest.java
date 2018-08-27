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

public class SocketTransportConnectorImplTest {

	private volatile AtomicInteger COUNTER = new AtomicInteger(1);
	
	private final SocketAddress address = new InetSocketAddress("10.142.0.30", 61000);
	
	public void testSimpleMessage() {
		Thread[] threads = new Thread[2000];
		for(int i=0;i<threads.length;i++) {
			threads[i] = new Thread(getTask2());
		}
		for(int i=0;i<threads.length;i++) {
			try {
				Thread.currentThread().sleep(10);
			}catch(InterruptedException e){}
			threads[i].start();
		}
	}
	
	private Runnable getTask2() {
		return ()-> {
			String message = "{\"command\":\"analysis\"}-";
			int index = COUNTER.getAndIncrement();
			boolean listen = true;
			try {
				Selector selector = Selector.open();
				SocketChannel channel = connect();
				channel.register(selector, SelectionKey.OP_WRITE);
				while(listen) {
					try {
						int count = selector.select();
			            if(count > 0) {
			                for(Iterator<SelectionKey> i = selector.selectedKeys().iterator(); 
			                									listen && i.hasNext();) {
			                    SelectionKey key = i.next();
			                    i.remove();
			                    if(key.isValid()) {
				                    if(key.isReadable()) {
				                    	byte[] data = readMessage(channel);
				                    	if(data.length != 1) {
				                    		System.out.println("READ for NO : " + index);
				                    		System.out.println("Received Message. " + new String(data, "ISO-8859-1"));
				                    		listen = false;
				                    		key.cancel();
				                    		break;
				                    		/*if(index++ == limit) {
				                    			listen = false;
				                    			break;
				                    		} else {
				                    			channel.register(selector, SelectionKey.OP_WRITE);
				                    		}*/
				                    	}
				                    }
				                    if(key.isWritable()) {
				                    	/*if(index == limit) {
				                    		listen = false;
				                    		writeToChannel("quit", channel);
				                    		break;
				                    	} else {*/
				                    	System.out.println("WRITE for NO : " + index);
				                    	writeToChannel(message + index, channel);
				                    	Thread.sleep(50);
				                    	channel.register(selector, SelectionKey.OP_READ);
				                    	//}
				                    }
			                    }
			                }
			            }
					}catch(Exception e) {
						listen = false;
						e.printStackTrace();
					}
				}
				System.out.println("Stopped push new message and close channel. NO : " + index);
				selector.close();
				channel.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		};
	}
	
	private SocketChannel connect() throws IOException {
		SocketChannel channel = SocketChannel.open(address);		
		channel.configureBlocking(false);
		//channel.connect(address);
		//channel.finishConnect();
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
