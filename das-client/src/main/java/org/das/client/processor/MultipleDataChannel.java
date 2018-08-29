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
import org.das.client.exception.ConnectionDataLinkException;

public class MultipleDataChannel implements Runnable {

	private final String name;
	private final String hostAddress;
	private final int port;
	private BlockingQueue<String> messageQueue = null;
	private boolean connected = false;
	private SelectionKey currentKey = null;
	private SocketChannel channel = null;
	private Selector selector = null;

	MultipleDataChannel(final String name, final String hostAddress,
			final int port) throws ConnectionDataLinkException {
		this.name = name;
		this.hostAddress = hostAddress;
		this.port = port;
	}

	@Override
	public void run() {
		while (connected) {
			try {
				int count = selector.select();
				if (count > 0) {
					for (Iterator<SelectionKey> i = selector.selectedKeys()
							.iterator(); connected && i.hasNext();) {
						currentKey = i.next();
						if (currentKey.isValid()) {
							if (currentKey.isReadable()) {
								byte[] data = readMessage(channel);
								if (data.length != 1) {
									final String result = new String(data, ClientAppConstant.DEFAULT_CHARSET);
									System.out.println("Client [" + name + "]. Received : " + result);
									channel.register(selector, SelectionKey.OP_WRITE);
								}
							}
							if (currentKey.isWritable()) {
								final String message = getMessage();
								if(Objects.isNull(message)) break;
								System.out.println("Client [" + name + "]. Push : " + message);
								writeToChannel(message);
								Thread.sleep(50);
								channel.register(selector, SelectionKey.OP_READ);
							}
						}
						i.remove();
					}
				}
			} catch (IOException e) {
				closeChannel();
				throw new ConnectionDataLinkException(e.getMessage());
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Channel Thread completed its work...");
	}

	void pushMessage(final String message) {
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	void connect() throws IOException {
		selector = Selector.open();
		SocketAddress address = new InetSocketAddress(hostAddress, port);
		channel = SocketChannel.open(address);
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_WRITE);
		messageQueue = new LinkedBlockingQueue<String>();
		connected = true;
	}
	
	void closeChannel() {
		connected = false;
		if (Objects.nonNull(currentKey)) currentKey.cancel();
		try {
			selector.close();
			if (Objects.nonNull(channel) && channel.isOpen()) channel.close();
		}catch(IOException e) {
			closeChannel();
			e.printStackTrace();
		}
	}
	
	boolean isConnected() {
		return connected;
	}
	
	private String getMessage() {
		String message = null;
		try {
			message = messageQueue.take();
			if (ClientAppConstant.CHANNEL_QUIT_MESSAGE.equalsIgnoreCase(message)) {
				closeChannel();
			}
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
		return message;
	}

	private byte[] readMessage(SocketChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(64);
		int state;
		byte[] data = { 1 };
		int position = 0;
		while ((state = channel.read(buffer)) > 0) {
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

}