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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.das.constant.AppConstant;

public class ClientChannelThread implements Runnable {

	private final String hostAddress;
	private final int port;
	private final BlockingQueue<String> messageQueue;
	private boolean connected = false;
	
	public ClientChannelThread(final String hostAddress, final int port) {
		this.hostAddress = hostAddress;
		this.port = port;
		messageQueue = new LinkedBlockingQueue<String>();
	}
	
	@Override
	public void run() {
		SocketChannel channel = null;
		Selector selector = null;
		try {
			selector = Selector.open();
			channel = connect();
			channel.register(selector, SelectionKey.OP_WRITE);
			int index = 0;
			int limit = 10;
			while(connected) {
				try {
					int count = selector.select();
		            if(count > 0) {
		                for(Iterator<SelectionKey> i = selector.selectedKeys().iterator(); 
		                									connected && i.hasNext();) {
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
			                    			connected = false;
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
			                    	writeToChannel("DONE", channel);
			                    	Thread.sleep(100);
			                    	channel.register(selector, SelectionKey.OP_READ);
			                    	//}
			                    }
		                    }
		                }
		            }
				}catch(Exception e) {
					connected = false;
					e.printStackTrace();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private SocketChannel connect() throws IOException {
		SocketAddress address = new InetSocketAddress(hostAddress, port);
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

}