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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.das.constant.AppConstant;

public class SocketTransportConnectorImplTest {

	private volatile AtomicInteger COUNTER = new AtomicInteger(1);
	
	private final SocketAddress address = new InetSocketAddress("10.142.0.30", 61000);
	
	public void testSimpleMessage() {
		Thread[] threads = new Thread[3000];
		for(int i=0;i<threads.length;i++) {
			threads[i] = new Thread(getTask2());
		}
		for(int i=0;i<threads.length;i++) {
			threads[i].start();
		}
	}
	
	private Runnable getTask2() {
		return ()-> {
			String message = "{\"command\":\"analysis\"}-";
			boolean listen = true;
			Selector selector = null;
			SocketChannel channel = null;
			try {
				selector = Selector.open();
				channel = connect();
				if(Objects.isNull(channel) || !channel.isConnected()) {
					Thread.sleep(500);
					channel = connect();
				}
				channel.register(selector, SelectionKey.OP_WRITE);
				int index = 0;
				int limit = 10;
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
				                    		System.out.println("Received Message. " + new String(data, AppConstant.SOCKET_CHAR_SET));
				                    		/*
				                    		listen = false;
				                    		key.cancel();				                    		
				                    		break;*/
				                    		if(++index == limit) {
				                    			listen = false;
				                    			key.cancel();
				                    			break;
				                    		} else {
				                    			channel.register(selector, SelectionKey.OP_WRITE);
				                    		}
				                    	}
				                    }
				                    if(key.isWritable()) {
				                    	/*if(index == limit) {
				                    		listen = false;
				                    		writeToChannel("quit", channel);
				                    		key.cancel();
				                    		break;
				                    	} else {*/
				                    	writeToChannel(message + COUNTER.getAndIncrement(), channel);
				                    	Thread.sleep(100);
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
			}catch(Exception e) {
				e.printStackTrace();
			} finally {
				System.out.println("Stopped push new message and close channel.");
				try {
					selector.close();
					if(channel.isOpen()) channel.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
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
