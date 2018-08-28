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

import org.das.client.constant.ClientAppConstant;
import org.das.client.exception.ConnectionDataLinkException;

public class SingleDataChannel {

	private final String hostAddress;
	private final int port;
	private SelectionKey currentKey = null;
	private SocketChannel channel = null;
	private Selector selector = null;
	private String message = null;
	private boolean connected = false;

	SingleDataChannel(final String hostAddress, final int port) {
		this.hostAddress = hostAddress;
		this.port = port;
	}

	public String pushMessage(final String message) throws ConnectionDataLinkException {
		this.message = message;
		return publish();
	}

	private String publish() throws ConnectionDataLinkException {
		String result = null;				
		try {
			selector = Selector.open();
			channel = connect();
			while(connected) {
				int count = selector.select();
				if (count > 0) {
					for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
						currentKey = i.next();					
						if (currentKey.isValid()) {
							if (currentKey.isReadable()) {
								byte[] data = readMessage(channel);
								if (data.length > 1) {
									result = new String(data, ClientAppConstant.DEFAULT_CHARSET);
									closeChannel();
									break;
								}
							}
							if (currentKey.isWritable()) {
								writeToChannel(message);
								Thread.sleep(100);
								channel.register(selector, SelectionKey.OP_READ);
							}
						}
						i.remove();
					}
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			throw new ConnectionDataLinkException(e.getMessage());
		} finally {
			closeChannel();
		}
		return result;
	}

	private SocketChannel connect() throws IOException {
		SocketAddress address = new InetSocketAddress(hostAddress, port);
		SocketChannel channel = SocketChannel.open(address);
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_WRITE);
		connected = true;
		return channel;
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

	private void closeChannel() {
		connected = false;
		try {
			if(Objects.nonNull(currentKey)) currentKey.cancel();
			selector.close();
			if(Objects.nonNull(channel) && channel.isOpen()) channel.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

}