package com.agen.client.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.netty.channel.EventLoopGroup;

@Component("clientShutdownClean")
public class ShutdownClean implements DisposableBean {

	@Autowired
	@Qualifier("clientWorkerGroup")
	private EventLoopGroup workerGroup;

	@Override
	public void destroy() throws Exception {
		workerGroup.shutdownGracefully();
	}

}