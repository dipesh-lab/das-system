package org.das.client.processor;

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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.das.client.constant.ClientAppConstant;

public class MultipleDataChannel implements Runnable {

	private final String name;
	private final String hostAddress;
	private final int port;
	private final BlockingQueue<String> messageQueue;
	private boolean connected = false;
	private SelectionKey currentKey=null;
	private SocketChannel channel = null;
	private Selector selector = null;
	
	MultipleDataChannel(final String name, final String hostAddress, final int port) {
		this.name = name;
		this.hostAddress = hostAddress;
		this.port = port;
		messageQueue = new LinkedBlockingQueue<String>();
	}
	
	@Override
	public void run() {
		try {
			selector = Selector.open();
			channel = connect();
			while(connected) {
				try {
					int count = selector.select();
		            if(count > 0) {
		                for(Iterator<SelectionKey> i = selector.selectedKeys().iterator(); 
		                									connected && i.hasNext();) {
		                	currentKey = i.next();
		                    if(currentKey.isValid()) {
			                    if(currentKey.isReadable()) {
			                    	byte[] data = readMessage(channel);
			                    	if(data.length != 1) {
			                    		System.out.println("Client [" + name + "]. Received : " + new String(data, ClientAppConstant.DEFAULT_CHARSET));
			                    		channel.register(selector, SelectionKey.OP_WRITE);
			                    	}
			                    }
			                    if(currentKey.isWritable()) {
			                    	final String message = getMessage();
			                    	System.out.println("Client [" + name + "]. Push : " + message);
			                    	writeToChannel(message);
			                    	Thread.sleep(50);
			                    	channel.register(selector, SelectionKey.OP_READ);
			                    }
		                    }
		                    i.remove();
		                }
		            }
				}catch(Exception e) {
					closeChannel();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void pushMessage(final String message) {
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private String getMessage() {
		String message = null;
		try {
			message = messageQueue.take();
			if(Objects.isNull(message)) message = getMessage();
			if(ClientAppConstant.CHANNEL_QUIT_MESSAGE.equalsIgnoreCase(message)) {
				closeChannel();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return message;
	}
	
	private SocketChannel connect() throws IOException {
		SocketAddress address = new InetSocketAddress(hostAddress, port);
		SocketChannel channel = SocketChannel.open(address);
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_WRITE);
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
	
	private void writeToChannel(final String data) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
		channel.write(buffer);
	}
	
	private void closeChannel() throws IOException {
		connected = false;
		currentKey.cancel();
		selector.close();
		channel.close();
	}

}