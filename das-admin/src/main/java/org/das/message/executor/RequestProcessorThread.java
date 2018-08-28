package org.das.message.executor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.das.config.AppConfiguration;
import org.das.constant.AppConstant;

public class RequestProcessorThread implements Runnable {

	private static Logger LOG = Logger.getLogger(RequestProcessorThread.class);

	private final SocketChannel channel;

	private boolean connected = true;
	
	private SelectionKey currentKey = null;
	
	private ByteBuffer reqAccptBuffer = ByteBuffer.wrap(AppConfiguration.getInstance().getProperty("request.accepted.message").getBytes());

	public RequestProcessorThread(final SocketChannel channel) {
		this.channel = channel;
	}

	@Override
	public void run() {
		if (!connected)
			return;
		LOG.debug("New Client found and started the Listener");
		try {
			Selector selector = Selector.open();
			channel.register(selector, SelectionKey.OP_READ);
			while (connected) {
				selector.selectNow();
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				while (keys.hasNext()) {
					currentKey = keys.next();
					if (currentKey.isReadable()) {
						doRead();
						channel.register(selector, SelectionKey.OP_WRITE);
					}
					if (currentKey.isWritable()) {
						doWrite();
						channel.register(selector, SelectionKey.OP_READ);
					}
					keys.remove();
				}
			}
		} catch (IOException e) {
			connected = false;
			LOG.error(e.getMessage());
			closeConnection();
		}
	}

	private void doRead() throws IOException {
		byte[] data = readMessage();
		if (data.length > 1) {
			String str = new String(data, AppConstant.SOCKET_CHAR_SET);
			LOG.debug("New Data available : " + str);
			if("quit".equalsIgnoreCase(str)) {
				connected = false;
				LOG.debug("Client Request shutdown. Now terminate the connection.");
			}
		}
	}

	private void doWrite() throws IOException {
		channel.write(reqAccptBuffer);
		reqAccptBuffer.rewind();
	}

	private byte[] readMessage() throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(128);
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
	
	private void closeConnection() {
		currentKey.cancel();
		try {
			if(channel.isOpen()) channel.close();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}