package org.das.client.processor;

import java.io.IOException;
import java.util.Objects;

import org.das.client.core.Publisher;
import org.das.client.exception.ConnectionDataLinkException;

public class MultipleMessagePublisherImpl implements Publisher {

	private final String name;
	private final String hostAddress;
	private final int port;
	private MultipleDataChannel dataChannel = null;
	private Thread channelThread = null;
	
	public MultipleMessagePublisherImpl(final String name, final String hostAddress, final int port) {
		this.name = name;
		this.hostAddress = hostAddress;
		this.port = port;
	}
	
	@Override
	public String pushMessage(final String message)	throws ConnectionDataLinkException {
		if(Objects.isNull(dataChannel) || !dataChannel.isConnected()) 
			throw new ConnectionDataLinkException("Connection is not established yet.");
		dataChannel.pushMessage(message);
		return null;
	}

	@Override
	public void close() {
		if(Objects.nonNull(dataChannel) && dataChannel.isConnected()) {
			dataChannel.closeChannel();
			channelThread.interrupt();
		}
	}

	@Override
	public void createSession() throws ConnectionDataLinkException {
		dataChannel = new MultipleDataChannel(name, hostAddress, port);
		try {
			dataChannel.connect();
			channelThread = new Thread(dataChannel);
			channelThread.start();
		} catch(IOException e) {
			throw new ConnectionDataLinkException(e.getMessage());
		}
	}

} 