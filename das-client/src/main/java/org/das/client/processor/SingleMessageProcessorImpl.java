package org.das.client.processor;

import java.util.Objects;

import org.das.client.MessagePublisher;
import org.das.client.exception.ConnectionDataLinkException;

public class SingleMessageProcessorImpl implements MessagePublisher {

	
	@Override
	public String pushMessage(final String message, final String hostAddress, final int port) 
			throws ConnectionDataLinkException {
		if(Objects.isNull(message)) return null;
		
		SingleDataChannel dataChannel = new SingleDataChannel(hostAddress, port);
		return dataChannel.pushMessage((String)message);
	}

}