package org.das.client;

import org.das.client.exception.ConnectionDataLinkException;

public interface MessagePublisher {

	String pushMessage(final String message, final String hostAddress, final int port) throws ConnectionDataLinkException;

}