package org.das.network.server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Objects;

import org.apache.log4j.Logger;

public class RequestWriter extends AbstractRequestProcessor implements RequestProcessor {

	private static final Logger LOG = Logger.getLogger(RequestWriter.class);
	
	public RequestWriter(Selector sel) {
		super(sel, SelectionKey.OP_READ);
	}
	
	@Override
	public SocketChannel process(SelectionKey key) {
		Object object = key.attachment();
		SocketChannel channel = (SocketChannel) key.channel();
		if(Objects.nonNull(object)) {
			try {
				final String data = object.toString();
				ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
				channel.write(buffer);
				LOG.debug("Writing Complete. " + data);			
			} catch(Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return channel;
	}

}