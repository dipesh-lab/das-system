package org.das.client.processor;

import java.io.IOException;
import java.util.Objects;

import org.das.client.core.Publisher;
import org.das.client.exception.ConnectionDataLinkException;

public class SingleMessagePublisherImpl implements Publisher {

	private final String hostAddress;
	private final int port;
	private SingleDataChannel dataChannel = null;
	
	public SingleMessagePublisherImpl(final String hostAddress, final int port) {
		this.hostAddress = hostAddress;
		this.port = port;
	}

	@Override
	public String pushMessage(final String message) throws ConnectionDataLinkException {
		if(Objects.isNull(dataChannel) || !dataChannel.isConnected())
			throw new ConnectionDataLinkException("Connection is not established yet.");
		return dataChannel.pushMessage((String)message);
	}

	@Override
	public void close() {
		if(Objects.nonNull(dataChannel) && dataChannel.isConnected())
			dataChannel.closeChannel();
	}

	@Override
	public void createSession() throws ConnectionDataLinkException {
		dataChannel =  new SingleDataChannel(hostAddress, port);
		try {
			dataChannel.connect();
		} catch (IOException e) {
			throw new ConnectionDataLinkException(e.getMessage());
		}
	}

}