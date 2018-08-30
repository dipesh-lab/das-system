package org.das.worker.network.connector;

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

import org.apache.log4j.Logger;
import org.das.worker.constant.WorkerAppConstant;
import org.das.worker.exception.ConnectionDataLinkException;

public class NetworkChannelConnector implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(NetworkChannelConnector.class);

	private final String name;
	private final String hostAddress;
	private final int port;
	private final BlockingQueue<String> messageQueue;
	private boolean connected = false;
	private SelectionKey currentKey = null;
	private SocketChannel channel = null;
	private Selector selector = null;

	public NetworkChannelConnector(final String name, final String hostAddress,
			final int port, final BlockingQueue<String> messageQueue) throws ConnectionDataLinkException {
		this.name = name;
		this.hostAddress = hostAddress;
		this.port = port;
		this.messageQueue = messageQueue;
	}

	@Override
	public void run() {
		LOG.debug("Now start listening for imcoming request");
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
									final String result = new String(data, WorkerAppConstant.DEFAULT_CHARSET);
									LOG.debug("Client [" + name + "]. Received : " + result);
									messageQueue.put(result);
									Thread.sleep(50);
								}
							}
						}
						i.remove();
					}
				}
			} catch (IOException e) {
				closeChannel();
				throw new ConnectionDataLinkException(e.getMessage());
			} catch(InterruptedException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		LOG.debug("Network Channel Thread exist.");
	}

	void pushMessage(final String message) {
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	void connect() throws IOException {
		selector = Selector.open();
		SocketAddress address = new InetSocketAddress(hostAddress, port);
		channel = SocketChannel.open(address);
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
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
			LOG.error(e.getMessage(), e);
		}
	}
	
	boolean isConnected() {
		return connected;
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

}