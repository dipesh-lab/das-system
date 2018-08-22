package org.das.network.server;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;

import org.apache.log4j.Logger;

public class RequestAcceptor extends AbstractRequestProcessor implements RequestProcessor {

	private static Logger LOG = Logger.getLogger(RequestAcceptor.class);
	
	RequestAcceptor(Selector sel) {
		super(sel, SelectionKey.OP_READ);
	}
	
	@Override
	public SocketChannel process(SelectionKey key) {
		SocketChannel channel = null;
		try {
			ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
			channel = serverSocketChannel.accept();
			if(Objects.nonNull(channel)) {
				channel.configureBlocking(false);
				channel = channel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
			}
		}catch(IOException ioe) {
			LOG.error(ioe.getMessage(), ioe);
		}
		return channel;
	}

}